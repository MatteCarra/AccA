package mattecarra.accapp.acc.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AccBootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            AccBootService.enqueueWork(context, Intent())
        }
    }
}