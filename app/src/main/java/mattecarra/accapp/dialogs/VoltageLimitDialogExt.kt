package mattecarra.accapp.dialogs

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.models.AccConfig

typealias VoltageLimitSelectionListener =
        ((isEnabled: Boolean, voltageMax: Int?, controlFile: String) -> Unit)

fun MaterialDialog.voltageLimitDialog(
    configVoltage: AccConfig.ConfigVoltage,
    coroutineScope: CoroutineScope,
    listener: VoltageLimitSelectionListener
): MaterialDialog {
    val dialog = customView(R.layout.voltage_control_editor_dialog)
        .positiveButton(android.R.string.ok) { dialog ->
            val view = dialog.getCustomView()
            val voltageControl = view.findViewById<Spinner>(R.id.voltage_control_file_spinner)
            val voltageMax = view.findViewById<EditText>(R.id.voltage_max_edit_text)
            val checkBox = dialog.findViewById<CheckBox>(R.id.enable_voltage_max_check_box)

            val voltageMaxInt = voltageMax.text.toString().toIntOrNull()

            listener(checkBox.isChecked, voltageMaxInt, voltageControl.selectedItem as String)
        }

    //initialize dialog custom view:
    val view = dialog.getCustomView()
    val voltageControlSpinner = view.findViewById<Spinner>(R.id.voltage_control_file_spinner)
    val enableVoltageLimitCheckBox = dialog.findViewById<CheckBox>(R.id.enable_voltage_max_check_box)
    val voltageMaxEditText = view.findViewById<EditText>(R.id.voltage_max_edit_text)

    enableVoltageLimitCheckBox.setOnCheckedChangeListener { _, isChecked ->
        voltageMaxEditText.isEnabled = isChecked

        val voltageMaxVal = voltageMaxEditText.text?.toString()?.toIntOrNull()
        val isValid = voltageMaxVal != null && voltageMaxVal >= 3500 && voltageMaxVal <= 4350
        voltageMaxEditText.error = if (isValid) null else context.getString(R.string.invalid_voltage_max)
        dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid  && voltageControlSpinner.selectedItemPosition != -1)
    }
    enableVoltageLimitCheckBox.isChecked = configVoltage.max != null

    voltageMaxEditText.setText(configVoltage.max?.toString() ?: "", TextView.BufferType.EDITABLE)
    voltageMaxEditText.isEnabled = enableVoltageLimitCheckBox.isChecked
    voltageMaxEditText.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val voltageMaxVal = s?.toString()?.toIntOrNull()
            val isValid = voltageMaxVal != null && voltageMaxVal >= 3500 && voltageMaxVal <= 4350
            voltageMaxEditText.error = if(isValid) null else context.getString(R.string.invalid_voltage_max)
            dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid  && voltageControlSpinner.selectedItemPosition != -1)
        }
    })

    //Voltage control files are loaded asynchronously
    coroutineScope.launch {
        val supportedVoltageControlFiles = ArrayList(Acc.instance.listVoltageSupportedControlFiles())

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, supportedVoltageControlFiles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        voltageControlSpinner.adapter = adapter

        //Load the selected item and set it
        configVoltage.controlFile?.let { currentVoltFile ->
            val currentVoltFileRegex = currentVoltFile.replace("/", """\/""").replace(".", """\.""").replace("?", ".").toRegex()
            val match = supportedVoltageControlFiles.find { currentVoltFileRegex.matches(it) }
            if(match == null) {
                supportedVoltageControlFiles.add(currentVoltFile)
                currentVoltFile
            } else {
                match
            }
        }?.let {
            voltageControlSpinner.setSelection(supportedVoltageControlFiles.indexOf(it))
        }

        //if no item is selected disable the button
        if(voltageControlSpinner.selectedItemPosition == -1) {
            dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
        }

        voltageControlSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val voltageMaxVal = voltageMaxEditText.text?.toString()?.toIntOrNull()
                val isValid = voltageMaxVal != null && voltageMaxVal >= 3500 && voltageMaxVal <= 4350
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid && position != -1)
            }
        }
    }

    return dialog
}