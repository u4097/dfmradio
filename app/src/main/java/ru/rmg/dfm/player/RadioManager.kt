package ru.rmg.dfm.player

import android.content.Context

class RadioManager(val context: Context) {

    private var serviceBound: Boolean = false
    private var instance: RadioManager? = null
    private var service: RadioService? = null

    init {
        serviceBound = false
    }

    fun with(context: Context): RadioManager {

        if (instance == null)
            instance = RadioManager(context)

        return instance as RadioManager
    }


    fun getService(): RadioService? {
        return service
    }

    fun playOrPause(streamUrl: String) {

        service?.playOrPause(streamUrl)
    }


}