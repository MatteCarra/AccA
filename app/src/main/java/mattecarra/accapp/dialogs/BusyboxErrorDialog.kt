package mattecarra.accapp.dialogs

import com.afollestad.materialdialogs.MaterialDialog
import mattecarra.accapp.R

fun MaterialDialog.busyBoxError(): MaterialDialog {
    return title(R.string.installation_failed_busybox_title)
        .message(R.string.installation_failed_busybox)
}