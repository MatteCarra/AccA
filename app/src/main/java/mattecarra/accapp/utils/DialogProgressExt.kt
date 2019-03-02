package mattecarra.accapp.utils

import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.CheckResult
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.android.synthetic.main.md_dialog_progress_indeterminate.view.*
import mattecarra.accapp.R

/**
 * Gets the input layout for the dialog if it's an input dialog.
 *
 * @throws IllegalStateException if the dialog is not an input dialog.
 */
@CheckResult fun MaterialDialog.getProgressLayout(): LinearLayout {
    return getCustomView() as? LinearLayout ?: throw IllegalStateException(
        "You have not setup this dialog as a LinearLayout."
    )
}

/**
 * Gets the input TextView for the dialog if it's an input dialog.
 *
 * @throws IllegalStateException if the dialog is not an input dialog.
 */
@CheckResult fun MaterialDialog.getTextView(): TextView {
    return getProgressLayout().md_content ?: throw IllegalStateException(
        "You have not setup this dialog as an input dialog."
    )
}

@CheckResult
fun MaterialDialog.progress(
    res: Int? = null,
    text: String? = null
): MaterialDialog {
    customView(R.layout.md_dialog_progress_indeterminate)

    if(res != null || text != null) {
        val resources = windowContext.resources
        val textView = getTextView()
        textView.text = text ?: resources.getString(res!!)
    }

    return this
}