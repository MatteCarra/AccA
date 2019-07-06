package mattecarra.accapp.acc.service

import android.content.Context
import androidx.core.app.JobIntentService
import android.content.Intent
import androidx.annotation.NonNull
import com.topjohnwu.superuser.Shell
import mattecarra.accapp.acc.Acc
import java.io.File

class AccBootService: JobIntentService() {

    companion object {
        const val JOB_ID = 0x01

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, AccBootService::class.java, JOB_ID, work)
        }
    }


    override fun onHandleWork(intent: Intent) {
        val file = File(baseContext.filesDir, "acc/acc-init.sh")
        if(file.exists() && Shell.rootAccess()) {
            Shell.su(file.absolutePath)
        }
    }
}