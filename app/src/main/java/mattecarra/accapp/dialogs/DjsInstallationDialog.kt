package mattecarra.accapp.dialogs

import com.afollestad.materialdialogs.MaterialDialog
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.djs.Djs
import java.io.File

interface DjsInstallationListener {
    fun onInstallationFailed(result: Shell.Result?)
    fun onBusyboxMissing()
    fun onSuccess()
}

fun MaterialDialog.djsInstallation(
    coroutineScope: CoroutineScope,
    listener: DjsInstallationListener
): MaterialDialog {
    val dialog =
        title(R.string.installing_djs)
            .progress(R.string.wait)

    coroutineScope.launch {
        val res = Djs.installBundledAccModule(context)
        cancel()

        if(res?.isSuccess != true) {
            when {
                res?.code == 3 -> //Buysbox is not installed
                    listener.onBusyboxMissing()

                else -> //Generic error
                    listener.onInstallationFailed(res)
            }
        } else {
            listener.onSuccess()
        }
    }

    return dialog
}