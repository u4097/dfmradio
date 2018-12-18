package ru.rmg.dfm

import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.list_item.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import ru.rmg.dfm.player.PlaybackStatus
import ru.rmg.dfm.player.RadioManager
import ru.rmg.dfm.util.Shoutcast
import ru.rmg.dfm.util.ShoutcastHelper
import ru.rmg.dfm.util.ShoutcastListAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var radioManager: RadioManager

    private lateinit var streamURL: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        radioManager = RadioManager.create(this)

        listview.adapter = ShoutcastListAdapter(this, ShoutcastHelper.retrieveShoutcasts(this))

        initView()
    }

    private fun initView() {
        playTrigger.setOnClickListener {
            if (!TextUtils.isEmpty(streamURL))

                radioManager.playOrPause(streamURL)
        }

        listview.setOnItemClickListener { parent, view, position, id ->

            val shoutcast = parent.getItemAtPosition(position) as Shoutcast

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
            }

            PlaybackStatus.ERROR ->

                Toast.makeText(this, R.string.no_stream, Toast.LENGTH_SHORT).show()
        }// loading

        playTrigger.setImageResource(
            if (status == PlaybackStatus.PLAYING)
                R.drawable.ic_pause_black
            else
                R.drawable.ic_play_arrow_black
        )

    }


}
