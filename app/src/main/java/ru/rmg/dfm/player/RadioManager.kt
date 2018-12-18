package ru.rmg.dfm.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import org.greenrobot.eventbus.EventBus
import ru.rmg.dfm.player.datasource.IcyDataSource

class RadioManager(
    private val context: Context,
    private val dataListener: IcyDataSource.Listener
) {

    private var serviceBound: Boolean = false
    private var service: RadioService? = null

    interface Factory<T> {
        fun create(context: Context, dataListener: IcyDataSource.Listener): T
    }


    companion object : Factory<RadioManager> {
        override fun create(context: Context, dataListener: IcyDataSource.Listener): RadioManager =
            RadioManager(context, dataListener)

    }


    fun playOrPause(streamUrl: String) {

        service?.playOrPause(streamUrl)
    }

    fun bind() {

        val intent = Intent(context, RadioService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        if (service != null)
            EventBus.getDefault().post(service!!.status)
    }

    fun unbind() {

        context.unbindService(serviceConnection)
    }

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(arg0: ComponentName, binder: IBinder) {

            service = (binder as RadioService.LocalBinder).service
            service!!.dataListener = dataListener
            serviceBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {

            serviceBound = false
        }
    }


}