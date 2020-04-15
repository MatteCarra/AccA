package mattecarra.accapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.topjohnwu.superuser.Shell
import mattecarra.accapp.Preferences
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.djs.Djs

class AccBootReceiver: BroadcastReceiver() {
    private val LOG_TAG = "AccBootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            if(Shell.rootAccess()) {
                val preferences = Preferences(context)

                val accInitResult = Acc.initAcc(context.filesDir)
                Log.d(LOG_TAG, "Acc deamon init. Success=$accInitResult")

                if(preferences.djsEnabled) {
                    val djsInitResult = Djs.initDjs(context.filesDir)
                    Log.d(LOG_TAG, "DJS deamon init. Success=$djsInitResult")
                }
            }
        }
    }
}