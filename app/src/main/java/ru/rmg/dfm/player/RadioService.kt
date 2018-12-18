package ru.rmg.dfm.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.TelephonyManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import ru.rmg.dfm.R

class RadioService : Service(), Player.EventListener, AudioManager.OnAudioFocusChangeListener {

    val ACTION_PLAY = "ru.rmg.dfm.player.ACTION_PLAY"
    val ACTION_PAUSE = "ru.rmg.dfm.player.ACTION_PAUSE"
    val ACTION_STOP = "ru.rmg.dfm.player.ACTION_STOP"

    private val iBinder = LocalBinder()

    private var handler: Handler? = null
    private val BANDWIDTH_METER = DefaultBandwidthMeter()
    private var exoPlayer: SimpleExoPlayer? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    private var onGoingCall = false
    private var telephonyManager: TelephonyManager? = null

    private var wifiLock: WifiManager.WifiLock? = null

    private var audioManager: AudioManager? = null

    private var notificationManager: MediaNotificationManager? = null

    private var status: String? = null

    private var strAppName: String? = null
    private var strLiveBroadcast: String? = null
    private var streamUrl: String? = null

    inner class LocalBinder : Binder() {
        val service: RadioService
            get() = this@RadioService
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


    }

    private val becomingNoisyReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            pause()
        }
    }

    fun pause() {

        exoPlayer?.playWhenReady = false

        audioManager?.abandonAudioFocus(this)
        wifiLockRelease()
    }


    fun getStatus(): String? {
        return status
    }


    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSeekProcessed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPositionDiscontinuity(reason: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAudioFocusChange(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun playOrPause(streamUrl: String) {

    }

    private fun wifiLockRelease() {

        if (wifiLock != null && wifiLock!!.isHeld()) {

            wifiLock!!.release()
        }
    }

}
