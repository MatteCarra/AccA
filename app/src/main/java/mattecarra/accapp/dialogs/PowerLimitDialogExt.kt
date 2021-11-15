package mattecarra.accapp.dialogs

import android.renderscript.ScriptGroup
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
import mattecarra.accapp.databinding.VoltageControlEditorDialogBinding
import mattecarra.accapp.models.AccConfig

typealias PowerLimitSelectionListener = ((voltageControlFile: String?, voltageLimitEnabled: Boolean,
                                          voltageMax: Int?, currentLimitEnabled: Boolean,
                                          currentMax: Int?) -> Unit)

fun MaterialDialog.powerLimitDialog(
    configVoltage: AccConfig.ConfigVoltage,
    configCurrentMax: Int?,
    coroutineScope: CoroutineScope,
    listener: PowerLimitSelectionListener
): MaterialDialog {

    val binding = VoltageControlEditorDialogBinding.inflate(layoutInflater)
    customView(view = binding.root, scrollable = true)
    title(R.string.edit_power_limit)

    var inputVoltageMaxOk = true
    var inputCurrentMaxOK = true
    var inputVoltageControlFileOk = true

    val voltageControlFileLayout = binding.voltageControlFileDialogLl
    val voltageControlSpinner = binding.voltageControlFileSpinner

    val enableVoltageLimitCheckBox = binding.enableVoltageMaxCheckBox
    val voltageMaxEditText = binding.voltageMaxEditText

    val currentMaxLayout = binding.currentMaxDialogLl
    val enableCurrentLimitCheckBox = binding.enableCurrentMaxCheckBox
    val currentMaxEditText = binding.currentMaxEditText

    positiveButton(android.R.string.ok) { dialog ->

        val voltageMaxInt = voltageMaxEditText.text.toString().toIntOrNull()
        val currentMaxInt = currentMaxEditText.text.toString().toIntOrNull()

        listener(voltageControlSpinner.selectedItem as String?, enableVoltageLimitCheckBox.isChecked,
            voltageMaxInt, enableCurrentLimitCheckBox.isChecked, currentMaxInt)
    }

    // VOLTAGE MAX SELECTION --------------------------------------------------------------------

    fun hideHintErrVolt(hide: Boolean)
    {
        voltageMaxEditText.error = if (hide) null else context.getString(R.string.invalid_voltage_max)
    }

    fun checkVolt(value: String?)
    {
        inputVoltageMaxOk = !value.isNullOrEmpty() && value.toInt() in 3700..4300
        hideHintErrVolt(inputVoltageMaxOk)
        setActionButtonEnabled(WhichButton.POSITIVE, inputCurrentMaxOK && inputVoltageMaxOk && inputVoltageControlFileOk)
    }

    voltageMaxEditText.setText(configVoltage.max?.toString() ?: "", TextView.BufferType.EDITABLE) //Initial value

    enableVoltageLimitCheckBox.setOnCheckedChangeListener { _, isChecked ->

        if (isChecked.also { voltageMaxEditText.isEnabled = it })
        {
            if (!voltageMaxEditText.text.isNullOrEmpty()) checkVolt(voltageMaxEditText.text?.toString())
            voltageMaxEditText.hasFocusable()
        }
        else { hideHintErrVolt(true) ;  }
    }

    enableVoltageLimitCheckBox.isChecked = configVoltage.max != null
    voltageMaxEditText.isEnabled = configVoltage.max != null

    voltageMaxEditText.addTextChangedListener(object : TextWatcher
    {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
        {
            checkVolt(s.toString())
        }
    })

    //END -----------------------------------------------------------------------------------

    fun hideHintErrCurrent(hide: Boolean)
    {
        currentMaxEditText.error = if (hide) null else context.getString(R.string.invalid_current_max)
    }

    fun checkCurrent(value: String?)
    {
        inputCurrentMaxOK = !value.isNullOrEmpty() && value.toInt() > 0
        hideHintErrCurrent(inputCurrentMaxOK)
        setActionButtonEnabled(WhichButton.POSITIVE, inputCurrentMaxOK && inputVoltageMaxOk && inputVoltageControlFileOk)
    }

    if(Acc.instance.version >= 202002170)
    {
        voltageControlFileLayout.visibility = View.GONE

        //CURRENT MAX SELECTION
        currentMaxEditText.setText(configCurrentMax?.toString() ?: "", TextView.BufferType.EDITABLE)

        enableCurrentLimitCheckBox.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked.also { currentMaxEditText.isEnabled = it })
            {
                if (!currentMaxEditText.text.isNullOrEmpty()) checkCurrent(currentMaxEditText.text?.toString())
                currentMaxEditText.hasFocusable()
            }
            else hideHintErrCurrent(true)
        }

        enableCurrentLimitCheckBox.isChecked = configCurrentMax != null
        currentMaxEditText.isEnabled = configCurrentMax != null

        currentMaxEditText.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                checkCurrent(s.toString())
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
                } else match

            }?.let {
                voltageControlSpinner.setSelection(supportedVoltageControlFiles.indexOf(it))
            }

            //if no item is selected disable the button
            if(voltageControlSpinner.selectedItemPosition == -1)
            {
                inputVoltageControlFileOk = false
                setActionButtonEnabled(WhichButton.POSITIVE, false)
            }
            else {
                inputVoltageControlFileOk = true
                setActionButtonEnabled(WhichButton.POSITIVE, inputCurrentMaxOK && inputVoltageMaxOk && inputVoltageControlFileOk)
            }

            voltageControlSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
            {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    inputVoltageControlFileOk = false
                    setActionButtonEnabled(WhichButton.POSITIVE, false)
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
                {
                    val voltageMaxVal = voltageMaxEditText.text?.toString()?.toIntOrNull()
                    inputVoltageControlFileOk = voltageMaxVal != null && voltageMaxVal in 3700..4300
                    setActionButtonEnabled(WhichButton.POSITIVE, inputCurrentMaxOK && inputVoltageMaxOk && inputVoltageControlFileOk)
                }
            }
        }
    }

    return this
}
