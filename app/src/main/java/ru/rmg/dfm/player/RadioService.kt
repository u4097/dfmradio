package ru.rmg.dfm.player

import android.app.Service
import android.content.Intent
import android.os.IBinder

class RadioService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun playOrPause(streamUrl: String) {

    }
}
