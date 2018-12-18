package ru.rmg.dfm

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.TimingLogger
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import ru.rmg.dfm.player.PlaybackStatus
import ru.rmg.dfm.player.RadioManager
import ru.rmg.dfm.player.datasource.IcyDataSource
import ru.rmg.dfm.player.entity.Track
import ru.rmg.dfm.util.Shoutcast
import ru.rmg.dfm.util.ShoutcastHelper
import ru.rmg.dfm.util.ShoutcastListAdapter
import timber.log.Timber
import java.util.*


class MainActivity : AppCompatActivity(), IcyDataSource.Listener {


    private lateinit var radioManager: RadioManager

    private lateinit var streamURL: String

    private lateinit var track: Track

    private lateinit var metadata: MediaMetadataCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        radioManager = RadioManager.create(this, this)


        listview.adapter = ShoutcastListAdapter(this, ShoutcastHelper.retrieveShoutcasts(this))

        initView()
    }

    private lateinit var shoutcast: Shoutcast

    private fun initView() {
        playTrigger.setOnClickListener {
            if (!TextUtils.isEmpty(streamURL))

                radioManager.playOrPause(streamURL)
        }

        listview.setOnItemClickListener { parent, _, position, _ ->

            shoutcast = parent.getItemAtPosition(position) as Shoutcast

            name.text = shoutcast.getName()

            sub_player.visibility = View.VISIBLE

            streamURL = shoutcast.getUrl()!!

            radioManager.playOrPause(streamURL)

        }

    }

    public override fun onStart() {

        super.onStart()

        EventBus.getDefault().register(this)
    }

    public override fun onStop() {

        EventBus.getDefault().unregister(this)

        super.onStop()
    }

    override fun onDestroy() {

        radioManager.unbind()

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        radioManager.bind()
    }

    override fun onBackPressed() {

        finish()
    }

    @Subscribe
    fun onEvent(status: String) {


        when (status) {

            PlaybackStatus.LOADING -> {
                name.text = "Загрузка..."
            }

            PlaybackStatus.PLAYING,
            PlaybackStatus.IDLE,
            PlaybackStatus.STOPPED -> {

                updateSongInfo()

            }

            PlaybackStatus.ERROR ->

                Toast.makeText(this, R.string.no_stream, Toast.LENGTH_SHORT).show()
        }

        playTrigger.setImageResource(
            if (status == PlaybackStatus.PLAYING)
                R.drawable.ic_pause_black
            else
                R.drawable.ic_play_arrow_black
        )

    }

    private fun updateSongInfo() {
        runOnUiThread {
            name.text = if (track != null) {
                "${track.artist} - ${track.title}"
            } else {
                shoutcast.getName()
            }

        }
    }

    override fun onMetaData(artist: String, title: String) {
        track = Track(artist, title, true, false)
        updateSongInfo()

    }

    override fun onServerDate(serverDate: Date?) {
        Timber.d("Date: %s", serverDate)
    }


}
