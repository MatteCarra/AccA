package mattecarra.accapp.dialogs

import android.content.Intent
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import java.io.File

@CheckResult
fun MaterialDialog.shareLogsNeutralButton(
    file: File,
    extraTextRes: Int
): MaterialDialog {
    return neutralButton(R.string.share) {
        if(file.exists()) {
            val intentShareFile = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(this.context.applicationContext, "mattecarra.accapp.fileprovider", file)
                )
                .putExtra(Intent.EXTRA_TEXT, context.getString(extraTextRes))

            context.startActivity(Intent.createChooser(intentShareFile, context.getString(R.string.share_log)))
        } else {
            Toast.makeText(context, R.string.logs_not_found, Toast.LENGTH_LONG).show()
        }
    }
}
/*
* Return true to normally process the event
* Return false to cancel the event and add your own logic
* */
typealias KeyCodeBackListener =
        (() -> Boolean)

@CheckResult
fun MaterialDialog.onKeyCodeBackPressed(
    callback: KeyCodeBackListener
): MaterialDialog {
    setOnKeyListener { _, keyCode, _ ->
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            callback()
        } else
            true
    }
    return this
}


typealias VersionChoiceListener =
        ((version: String) -> Unit)

@CheckResult
suspend fun MaterialDialog.accVersionSingleChoice(
    accVersion: String,
    callback: VersionChoiceListener
): MaterialDialog {
    val options = context.resources.getStringArray(R.array.acc_version_options).toMutableList()
    options.addAll(Acc.listAccVersions())
    return listItemsSingleChoice(items = options, initialSelection = options.map { it.toLowerCase() }.indexOf(accVersion)) { _, _, text ->
        callback(text.toLowerCase())
    }
}