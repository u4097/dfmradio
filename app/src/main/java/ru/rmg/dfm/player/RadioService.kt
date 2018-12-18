package ru.rmg.dfm.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import org.greenrobot.eventbus.EventBus
import ru.rmg.dfm.Constants
import ru.rmg.dfm.Constants.ACTION_PAUSE
import ru.rmg.dfm.Constants.ACTION_PLAY
import ru.rmg.dfm.Constants.ACTION_STOP
import ru.rmg.dfm.R
import ru.rmg.dfm.player.datasource.IcyDataSource
import ru.rmg.dfm.player.datasource.IcyDataSourceFactory
import ru.rmg.dfm.player.entity.Track
import java.util.*

class RadioService : Service(), Player.EventListener, AudioManager.OnAudioFocusChangeListener, IcyDataSource.Listener {


    override fun onMetaData(artist: String, title: String) {
        this.track = Track(artist, title, false)
        if (!TextUtils.isEmpty(track.artist))
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, strAppName)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
                    .build()
            )
        dataListener?.onMetaData(artist, title)
    }

    override fun onServerDate(serverDate: Date?) {
        dataListener?.onServerDate(serverDate)
    }



    private val iBinder = LocalBinder()
    private lateinit var track: Track
    private var handler: Handler? = null
    private lateinit var exoPlayer: SimpleExoPlayer
    internal lateinit var mediaSession: MediaSessionCompat
    private var transportControls: MediaControllerCompat.TransportControls? = null
    private var onGoingCall = false
    private var telephonyManager: TelephonyManager? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var audioManager: AudioManager? = null
    private var notificationManager: MediaNotificationManager? = null
    internal var status: String? = null
    private var strAppName: String? = null
    private var strLiveBroadcast: String? = null
    private var streamUrl: String? = null
    var dataListener: IcyDataSource.Listener? = null
    private var audioFocusRequestResult: Int? = null

    inner class LocalBinder : Binder() {
        val service: RadioService
            get() = this@RadioService
    }

    private val phoneStateListener = object : PhoneStateListener() {

        override fun onCallStateChanged(state: Int, incomingNumber: String) {

            if (state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) {

                if (!isPlaying()) {
                    return
                }

                onGoingCall = true
                stop()

            } else if (state == TelephonyManager.CALL_STATE_IDLE) {

                if (!onGoingCall) {
                    return
                }

                onGoingCall = false
                resume()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        strAppName = resources.getString(R.string.app_name)
        strLiveBroadcast = resources.getString(R.string.live_broadcast)

        onGoingCall = false

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        notificationManager = MediaNotificationManager(this)

        wifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL, "mcScPAmpLock")

        mediaSession = MediaSessionCompat(this, javaClass.simpleName)
        transportControls = mediaSession.controller.transportControls
        mediaSession.isActive = true
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        mediaSession.setCallback(mediasSessionCallback)

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager!!.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)

        handler = Handler()

        initExoPlayer()

        registerReceiver(becomingNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))

        status = PlaybackStatus.IDLE

    }

    private fun createDefaultLoadControl(
        minBufferMs: Int = Constants.MIN_BUFFER_MS,
        maxBufferMs: Int = Constants.MAX_BUFFER_MS,
        bufferForPlaybackMs: Int = Constants.BUFFER_PLAYBACK_MS,
        bufferForPlaybackAfterRebufferMs: Int = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
    ): DefaultLoadControl =
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(minBufferMs, maxBufferMs, bufferForPlaybackMs, bufferForPlaybackAfterRebufferMs)
            .createDefaultLoadControl()


    // TODO: Create PlayerManager for init exoPlayer
    private fun initExoPlayer() {
        val bandwidthMeter = DefaultBandwidthMeter()
        val renderersFactory = DefaultRenderersFactory(applicationContext)
        val trackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(trackSelectionFactory)

        exoPlayer = ExoPlayerFactory.newSimpleInstance(
            renderersFactory,
            trackSelector,
            createDefaultLoadControl()
        )
        exoPlayer.addListener(this)
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val action = intent.action

        if (TextUtils.isEmpty(action)) {
            return Service.START_NOT_STICKY
        }

        audioFocusRequestResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager?.requestAudioFocus(AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build())
        } else {
            audioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        if (audioFocusRequestResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            stop()

            return Service.START_NOT_STICKY
        }

        when {
            action.equals(ACTION_PLAY, ignoreCase = true) -> transportControls?.play()
            action.equals(ACTION_PAUSE, ignoreCase = true) -> transportControls?.pause()
            action.equals(ACTION_STOP, ignoreCase = true) -> transportControls?.stop()
        }

        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = iBinder


    override fun onUnbind(intent: Intent): Boolean {

        if (status == PlaybackStatus.IDLE) {
            stopSelf()
        }

        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent) {
    }


    override fun onDestroy() {

        pause()

        exoPlayer.release()
        exoPlayer.removeListener(this)

        if (telephonyManager != null) {
            telephonyManager!!.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }

        notificationManager?.cancelNotify()

        mediaSession.release()

        unregisterReceiver(becomingNoisyReceiver)

        super.onDestroy()
    }


    private val mediasSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPause() {
            super.onPause()

            pause()
        }

        override fun onStop() {
            super.onStop()

            stop()

            notificationManager?.cancelNotify()
        }

        override fun onPlay() {
            super.onPlay()

            resume()
        }
    }

    private val becomingNoisyReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            pause()
        }
    }

    private fun play(streamUrl: String) {
        this.streamUrl = streamUrl

        if (wifiLock != null && !wifiLock!!.isHeld) {
            wifiLock!!.acquire()
        }

        exoPlayer.prepare(setMediaSource(streamUrl))
        exoPlayer.playWhenReady = true
    }

    private fun setMediaSource(streamUrl: String): ExtractorMediaSource? =

        ExtractorMediaSource.Factory(IcyDataSourceFactory(this))
            .setExtractorsFactory(DefaultExtractorsFactory())
            .createMediaSource(Uri.parse(streamUrl))


    fun pause() {
        exoPlayer.playWhenReady = false
        audioManager?.abandonAudioFocus(this)
        wifiLockRelease()
    }

    fun stop() {
        exoPlayer.stop()

        audioManager?.abandonAudioFocus(this)
        wifiLockRelease()
    }

    fun resume() =
        if (streamUrl != null) {
            play(streamUrl!!)
        } else
            pause()


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        status = when (playbackState) {
            Player.STATE_BUFFERING -> PlaybackStatus.LOADING
            Player.STATE_ENDED -> PlaybackStatus.STOPPED
            Player.STATE_IDLE -> PlaybackStatus.IDLE
            Player.STATE_READY -> if (playWhenReady) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
            else -> PlaybackStatus.IDLE
        }

        if (status != PlaybackStatus.IDLE) {
            notificationManager?.startNotify(status!!)
        }

        EventBus.getDefault().post(status)
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        EventBus.getDefault().post(PlaybackStatus.ERROR)
    }


    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onSeekProcessed() {
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
    }


    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {

                exoPlayer.volume = 0.8f

                resume()
            }

            AudioManager.AUDIOFOCUS_LOSS ->

                stop()

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->

                if (isPlaying()) {
                    pause()
                }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->

                if (isPlaying()) {
                    exoPlayer.volume = 0.1f
                }
        }
    }


    fun playOrPause(url: String) {
        if (streamUrl != null && streamUrl == url) {

            if (!isPlaying())
                play(streamUrl!!)
            else
                pause()

        } else {

            if (isPlaying()) {
                pause()
            }

            play(url)
        }

    }

    fun isPlaying(): Boolean =
        this.status == PlaybackStatus.PLAYING

    private fun wifiLockRelease() {

        if (wifiLock != null && wifiLock!!.isHeld) {

            wifiLock!!.release()
        }
    }


}
