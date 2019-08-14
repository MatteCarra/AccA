package mattecarra.accapp.utils

import android.content.Intent
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import mattecarra.accapp.R
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