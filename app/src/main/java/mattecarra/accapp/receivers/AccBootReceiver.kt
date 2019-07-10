package mattecarra.accapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.topjohnwu.superuser.Shell
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import java.io.File

class AccBootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) { //TODO check if app is set in bundle/master/dev mode. Only execute this if acc is bundled.
            val file = File(context.filesDir, "acc/acc-init.sh")
            if(Shell.rootAccess() && Acc.isBundledAccInstalled(context)) {
                Shell.su("sh ${file.absolutePath}").exec() //TODO check the result of this
                Toast.makeText(context, R.string.acc_daemon_status_running, Toast.LENGTH_SHORT).show() //TODO add an option to disable this (I think it should disabled by default)
            }
        }
    }
}