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

typealias PowerLimitSelectionListener =
        ((voltageControlFile: String?, voltageLimitEnabled: Boolean, voltageMax: Int?, currentLimitEnabled: Boolean, currentMax: Int?) -> Unit)

fun MaterialDialog.powerLimitDialog(
    configVoltage: AccConfig.ConfigVoltage,
    configCurrentMax: Int?,
    coroutineScope: CoroutineScope,
    listener: PowerLimitSelectionListener
): MaterialDialog {
    val dialog = customView(R.layout.voltage_control_editor_dialog)
        .positiveButton(android.R.string.ok) { dialog ->
            val view = dialog.getCustomView()
            val voltageControl = view.findViewById<Spinner>(R.id.voltage_control_file_spinner)

            val voltageMax = view.findViewById<EditText>(R.id.voltage_max_edit_text)
            val voltageMaxCheckBox = dialog.findViewById<CheckBox>(R.id.enable_voltage_max_check_box)

            val currentMax = view.findViewById<EditText>(R.id.current_max_edit_text)
            val currentMaxCheckBox = dialog.findViewById<CheckBox>(R.id.enable_current_max_check_box)

            val voltageMaxInt = voltageMax.text.toString().toIntOrNull()
            val currentMaxInt = currentMax.text.toString().toIntOrNull()

            listener(voltageControl.selectedItem as String?, voltageMaxCheckBox.isChecked, voltageMaxInt, currentMaxCheckBox.isChecked, currentMaxInt)
        }

    //initialize dialog custom view:
    val view = dialog.getCustomView()

    var inputVoltageMaxOk = true
    var inputCurrentMaxOK = true
    var inputVoltageControlFileOk = true

    val voltageControlFileLayout = view.findViewById<LinearLayout>(R.id.voltage_control_file_dialog_ll)
    val voltageControlSpinner = view.findViewById<Spinner>(R.id.voltage_control_file_spinner)

    val enableVoltageLimitCheckBox = dialog.findViewById<CheckBox>(R.id.enable_voltage_max_check_box)
    val voltageMaxEditText = view.findViewById<EditText>(R.id.voltage_max_edit_text)

    val currentMaxLayout = view.findViewById<LinearLayout>(R.id.current_max_dialog_ll)
    val enableCurrentLimitCheckBox = dialog.findViewById<CheckBox>(R.id.enable_current_max_check_box)
    val currentMaxEditText = view.findViewById<EditText>(R.id.current_max_edit_text)

    //VOLTAGE MAX SELECTION
    voltageMaxEditText.setText(configVoltage.max?.toString() ?: "", TextView.BufferType.EDITABLE) //Initial value

    enableVoltageLimitCheckBox.setOnCheckedChangeListener { _, isChecked ->
        voltageMaxEditText.isEnabled = isChecked

        val voltageMaxVal = voltageMaxEditText.text?.toString()?.toIntOrNull()
        inputVoltageMaxOk = voltageMaxVal != null && voltageMaxVal >= 3700 && voltageMaxVal <= 4200
        voltageMaxEditText.error = if (inputVoltageMaxOk) null else context.getString(R.string.invalid_voltage_max)
        dialog.setActionButtonEnabled(WhichButton.POSITIVE, inputCurrentMaxOK && inputVoltageMaxOk && inputVoltageControlFileOk)
    }

    enableVoltageLimitCheckBox.isChecked = configVoltage.max != null

    voltageMaxEditText.isEnabled = enableVoltageLimitCheckBox.isChecked

    voltageMaxEditText.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val voltageMaxVal = s?.toString()?.toIntOrNull()
            val isValid =
                voltageMaxVal != null && voltageMaxVal >= 3700 && voltageMaxVal <= 4200
            voltageMaxEditText.error =
                if (isValid) null else context.getString(R.string.invalid_voltage_max)

            inputVoltageMaxOk = isValid
            dialog.setActionButtonEnabled(WhichButton.POSITIVE, inputCurrentMaxOK && inputVoltageMaxOk && inputVoltageControlFileOk)
        }
    })
    //END

    if(Acc.getAccVersion() >= 202002170) {
        voltageControlFileLayout.visibility = View.GONE

        //CURRENT MAX SELECTION
        currentMaxEditText.setText(configCurrentMax?.toString() ?: "", TextView.BufferType.EDITABLE)

        enableCurrentLimitCheckBox.setOnCheckedChangeListener { _, isChecked ->
            currentMaxEditText.isEnabled = isChecked

            val currentMaxVal = currentMaxEditText.text?.toString()?.toIntOrNull()
            inputCurrentMaxOK = currentMaxVal != null && currentMaxVal > 0
            currentMaxEditText.error = if (inputCurrentMaxOK) null else context.getString(R.string.invalid_current_max)
            dialog.setActionButtonEnabled(WhichButton.POSITIVE, inputCurrentMaxOK && inputVoltageMaxOk && inputVoltageControlFileOk)
        }

        enableCurrentLimitCheckBox.isChecked = configCurrentMax != null

        currentMaxEditText.isEnabled = enableCurrentLimitCheckBox.isChecked

        currentMaxEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentMaxVal = s?.toString()?.toIntOrNull()

                inputCurrentMaxOK = currentMaxVal != null && currentMaxVal > 0
                currentMaxEditText.error = if (inputCurrentMaxOK) null else context.getString(R.string.invalid_current_max)

                dialog.setActionButtonEnabled(WhichButton.POSITIVE, inputCurrentMaxOK && inputVoltageMaxOk && inputVoltageControlFileOk)
            }
        })
        //END

    } else {
        currentMaxLayout.visibility = View.GONE

        //Voltage control files are loaded asynchronously, do it now
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
                inputVoltageControlFileOk = false
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
            } else {
                inputVoltageControlFileOk = true
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, inputCurrentMaxOK && inputVoltageMaxOk && inputVoltageControlFileOk)
            }

            voltageControlSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    inputVoltageControlFileOk = false
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val voltageMaxVal = voltageMaxEditText.text?.toString()?.toIntOrNull()
                    val isValid = voltageMaxVal != null && voltageMaxVal >= 3700 && voltageMaxVal <= 4200

                    inputVoltageControlFileOk = isValid
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, inputCurrentMaxOK && inputVoltageMaxOk && inputVoltageControlFileOk)
                }
            }
        }
    }

    return dialog
}