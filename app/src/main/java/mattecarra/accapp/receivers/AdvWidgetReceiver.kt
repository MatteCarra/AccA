package mattecarra.accapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import mattecarra.accapp.services.OnAdvWidgetInterface
import mattecarra.accapp.utils.LogExt

class AdvWidgetReceiver : BroadcastReceiver()
{
    var onEvent: OnAdvWidgetInterface? = null

    override fun onReceive(context: Context, intent: Intent)
    {
        LogExt().d(javaClass.simpleName,".onReceive(): "+intent.action)

        when(intent.action)
        {
            Intent.ACTION_SCREEN_ON -> onEvent?.onScreen(true)
            Intent.ACTION_SCREEN_OFF -> onEvent?.onScreen(false)
            Intent.ACTION_POWER_CONNECTED -> onEvent?.onPowerState(true)
            Intent.ACTION_POWER_DISCONNECTED -> onEvent?.onPowerState(false)
        }
    }

    fun setEventInterface(onEvent: OnAdvWidgetInterface?)
    {
        this.onEvent = onEvent
    }
}