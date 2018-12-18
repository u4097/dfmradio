package ru.rmg.dfm

import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Bundle
import ru.rmg.dfm.player.RadioManager

class MainActivity : AppCompatActivity() {

    internal lateinit var radioManager: RadioManager

    internal lateinit var streamURL: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)

        radioManager = RadioManager(this)

    }
}
