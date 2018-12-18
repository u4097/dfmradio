package ru.rmg.dfm.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.greenrobot.eventbus.EventBus

class RadioManager(val context: Context) {

    private var serviceBound: Boolean = false
    private var service: RadioService? = null

    interface Factory<T> {
        fun create(context: Context): T
    }


    init {
        serviceBound = false
    }


    companion object : Factory<RadioManager> {
        override fun create(context: Context): RadioManager = RadioManager(context)

    }


    fun playOrPause(streamUrl: String) {

        service?.playOrPause(streamUrl)
    }

    fun bind() {

        val intent = Intent(context, RadioService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        if (service != null)
            EventBus.getDefault().post(service!!.getStatus())
    }

    fun unbind() {

        context.unbindService(serviceConnection)
    }

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(arg0: ComponentName, binder: IBinder) {

            service = (binder as RadioService.LocalBinder).service
            serviceBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {

            serviceBound = false
        }
    }


}