package mattecarra.accapp.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.content_acc_config_editor.*
import mattecarra.accapp.utils.AccUtils
import mattecarra.accapp.R
import mattecarra.accapp.data.AccConfig
import mattecarra.accapp.data.Cooldown
import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

class AccConfigEditorActivity : AppCompatActivity(), NumberPicker.OnValueChangeListener, CompoundButton.OnCheckedChangeListener {
    private var unsavedChanges = false
    private lateinit var config: AccConfig

    private fun returnResults() {
        val returnIntent = Intent()
        returnIntent.putExtra("data", intent.getBundleExtra("data"))
        returnIntent.putExtra("hasChanges", unsavedChanges)
        returnIntent.putExtra("config", config)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acc_config_editor)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if(intent.hasExtra("config")) {
            this.config = intent.getParcelableExtra("config")
        } else {
            try {
                this.config = AccUtils.readConfig()
            } catch (ex: Exception) {
                ex.printStackTrace()
                showConfigReadError()
                this.config = AccUtils.defaultConfig //if config is null I use default config values.
            }
        }

        initUi()
    }

    private fun showConfigReadError() {
        MaterialDialog(this).show {
            title(R.string.config_error_title)
            message(R.string.config_error_dialog)
            positiveButton(android.R.string.ok)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.acc_config_editor_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_save -> {
                returnResults()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if(unsavedChanges) {
            MaterialDialog(this)
                .show {
                    title(R.string.unsaved_changes)
                    message(R.string.unsaved_changes_message)
                    positiveButton(R.string.save) {
                        returnResults()
                    }
                    negativeButton(R.string.close_without_saving) {
                        finish()
                    }
                    neutralButton(android.R.string.cancel)
                }
        } else {
            super.onBackPressed()
        }
    }

    private fun initUi() {
        on_boot_text.text = config.onBoot?.let { if(it.isBlank()) getString(R.string.not_set) else it } ?: getString(R.string.not_set)
        exit_on_boot_switch.isChecked = config.onBootExit
        exit_on_boot_switch.setOnCheckedChangeListener { _, isChecked ->
            config.onBootExit = isChecked
            unsavedChanges = true
        }
        edit_on_boot.setOnClickListener {
            MaterialDialog(this@AccConfigEditorActivity).show {
                title(R.string.edit_on_boot)
                message(R.string.edit_on_boot_dialog_message)
                input(prefill = this@AccConfigEditorActivity.config.onBoot ?: "", allowEmpty = true, hintRes = R.string.edit_on_boot_dialog_hint) { _, text ->
                    this@AccConfigEditorActivity.config.onBoot = text.toString()
                    this@AccConfigEditorActivity.on_boot_text.text = if(text.isBlank()) getString(R.string.not_set) else text

                    unsavedChanges = true
                }
                positiveButton(R.string.save)
                negativeButton(android.R.string.cancel)
            }
        }

        shutdown_capacity_picker.minValue = 0
        shutdown_capacity_picker.maxValue = 20
        shutdown_capacity_picker.value = config.capacity.shutdownCapacity
        shutdown_capacity_picker.setOnValueChangedListener(this)

        resume_capacity_picker.minValue = config.capacity.shutdownCapacity
        resume_capacity_picker.maxValue = config.capacity.pauseCapacity - 1
        resume_capacity_picker.value = config.capacity.resumeCapacity
        resume_capacity_picker.setOnValueChangedListener(this)

        pause_capacity_picker.minValue = config.capacity.resumeCapacity + 1
        pause_capacity_picker.maxValue = 100
        pause_capacity_picker.value = config.capacity.pauseCapacity
        pause_capacity_picker.setOnValueChangedListener(this)

        //temps
        if(config.temp.coolDownTemp >= 90 && config.temp.pauseChargingTemp >= 95) {
            temp_switch.isChecked = false
            cooldown_temp_picker.isEnabled = false
            pause_temp_picker.isEnabled = false
            pause_seconds_picker.isEnabled = false
        }
        temp_switch.setOnCheckedChangeListener(this)

        cooldown_temp_picker.minValue = 20
        cooldown_temp_picker.maxValue = 90
        cooldown_temp_picker.value = config.temp.coolDownTemp
        cooldown_temp_picker.setOnValueChangedListener(this)

        pause_temp_picker.minValue = 20
        pause_temp_picker.maxValue = 95
        pause_temp_picker.value = config.temp.pauseChargingTemp
        pause_temp_picker.setOnValueChangedListener(this)

        pause_seconds_picker.minValue = 10
        pause_seconds_picker.maxValue = 120
        pause_seconds_picker.value = config.temp.waitSeconds
        pause_seconds_picker.setOnValueChangedListener(this)

        //cooldown
        if(config.cooldown == null || config.capacity.coolDownCapacity > 100) {
            cooldown_switch.isChecked = false
            cooldown_percentage_picker.isEnabled = false
            charge_ratio_picker.isEnabled = false
            pause_ratio_picker.isEnabled = false
        }
        cooldown_switch.setOnCheckedChangeListener(this)

        cooldown_percentage_picker.minValue = config.capacity.shutdownCapacity
        cooldown_percentage_picker.maxValue = 101 //if someone wants to disable it should use the switch but I'm gonna leave it there
        cooldown_percentage_picker.value = config.capacity.coolDownCapacity
        cooldown_percentage_picker.setOnValueChangedListener(this)

        charge_ratio_picker.minValue = 1
        charge_ratio_picker.maxValue = 120 //no reason behind this value
        charge_ratio_picker.value = config.cooldown?.charge ?: 50
        charge_ratio_picker.setOnValueChangedListener(this)

        pause_ratio_picker.minValue = 1
        pause_ratio_picker.maxValue = 120 //no reason behind this value
        pause_ratio_picker.value = config.cooldown?.pause ?: 10
        pause_ratio_picker.setOnValueChangedListener(this)

        //voltage control
        voltage_control_file.text = config.voltControl.voltFile ?: "Not supported"
        voltage_max.text = config.voltControl.voltMax?.let { "$it mV" } ?: getString(R.string.disabled)

        //Edit voltage dialog
        edit_voltage_limit.setOnClickListener {
            val dialog = MaterialDialog(this@AccConfigEditorActivity).show {
                customView(R.layout.voltage_control_editor_dialog)
                positiveButton(android.R.string.ok) { dialog ->
                    val view = dialog.getCustomView()
                    val voltageControl = view.findViewById<Spinner>(R.id.voltage_control_file)
                    val voltageMax = view.findViewById<EditText>(R.id.voltage_max)
                    val checkBox = dialog.findViewById<CheckBox>(R.id.enable_voltage_max)

                    val voltageMaxInt = voltageMax.text.toString().toIntOrNull()
                    if(checkBox.isChecked && voltageMaxInt != null) {
                        this@AccConfigEditorActivity.config.voltControl.voltMax = voltageMaxInt
                        this@AccConfigEditorActivity.config.voltControl.voltFile = voltageControl.selectedItem as String

                        this@AccConfigEditorActivity.voltage_control_file.text = voltageControl.selectedItem as String
                        this@AccConfigEditorActivity.voltage_max.text = "$voltageMaxInt mV"
                    } else {
                        this@AccConfigEditorActivity.config.voltControl.voltMax = null

                        this@AccConfigEditorActivity.voltage_max.text = getString(R.string.disabled)
                    }

                    unsavedChanges = true
                }
                negativeButton(android.R.string.cancel)
            }

            //initialize dialog custom view:
            val view = dialog.getCustomView()
            val voltageMax = view.findViewById<EditText>(R.id.voltage_max)
            val checkBox = dialog.findViewById<CheckBox>(R.id.enable_voltage_max)
            voltageMax.setText(config.voltControl.voltMax?.toString() ?: "", TextView.BufferType.EDITABLE)
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                voltageMax.isEnabled = isChecked
                val isValid = !voltageMax.text.isEmpty() || !isChecked
                voltageMax.error = if (isValid) null else getString(R.string.invalid_chars)
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
            }
            checkBox.isChecked = config.voltControl.voltMax != null
            voltageMax.isEnabled = checkBox.isChecked
            voltageMax.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val isValid = s?.isEmpty() == false
                    voltageMax.error = if(isValid) null else getString(R.string.invalid_chars)
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                }
            })

            val voltageControl = view.findViewById<Spinner>(R.id.voltage_control_file)
            val supportedVoltageControlFiles = ArrayList(AccUtils.listVoltageSupportedControlFiles())
            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, supportedVoltageControlFiles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            voltageControl.adapter = adapter
            if(config.voltControl.voltFile != null) {
                if(!supportedVoltageControlFiles.contains(config.voltControl.voltFile)) {
                    supportedVoltageControlFiles.add(config.voltControl.voltFile)
                }
                voltageControl.setSelection(supportedVoltageControlFiles.indexOf(config.voltControl.voltFile))
            }
        }
    }

    //Listener to enable/disable temp control and cool down
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(buttonView == null) return
        when(buttonView.id) {
            R.id.temp_switch -> {
                cooldown_temp_picker.isEnabled = isChecked
                pause_temp_picker.isEnabled = isChecked
                pause_seconds_picker.isEnabled = isChecked

                if(isChecked) {
                    cooldown_temp_picker.value = 40
                    pause_temp_picker.value = 45

                    config.temp.coolDownTemp = 40
                    config.temp.pauseChargingTemp = 45
                    unsavedChanges = true
                } else {
                    config.temp.coolDownTemp = 90
                    config.temp.pauseChargingTemp = 95
                    unsavedChanges = true
                }
            }

            R.id.cooldown_switch -> {
                cooldown_percentage_picker.isEnabled = isChecked
                charge_ratio_picker.isEnabled = isChecked
                pause_ratio_picker.isEnabled = isChecked

                if(isChecked) {
                    cooldown_percentage_picker.value = 60
                    config.capacity.coolDownCapacity = 60
                    unsavedChanges = true
                } else {
                    cooldown_percentage_picker.value = 101
                    config.capacity.coolDownCapacity = 101
                    unsavedChanges = true
                }
            }

            else -> {}
        }
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        if(picker == null) return

        when(picker.id) {
            //capacity
            R.id.shutdown_capacity_picker -> {
                config.capacity.shutdownCapacity = newVal

                resume_capacity_picker.minValue = config.capacity.shutdownCapacity
                cooldown_percentage_picker.minValue = config.capacity.shutdownCapacity
            }

            R.id.resume_capacity_picker -> {
                config.capacity.resumeCapacity = newVal

                pause_capacity_picker.minValue = config.capacity.resumeCapacity + 1
            }

            R.id.pause_capacity_picker -> {
                config.capacity.pauseCapacity = newVal

                resume_capacity_picker.maxValue = config.capacity.pauseCapacity - 1
            }

            //temp
            R.id.cooldown_temp_picker ->
                config.temp.coolDownTemp = newVal

            R.id.pause_temp_picker ->
                config.temp.pauseChargingTemp = newVal

            R.id.pause_seconds_picker ->
                config.temp.waitSeconds = newVal

            //coolDown
            R.id.cooldown_percentage_picker ->
                config.capacity.coolDownCapacity = newVal

            R.id.charge_ratio_picker -> {
                if(config.cooldown == null) {
                    config.cooldown = Cooldown(newVal, 10)
                }
                config.cooldown?.charge = newVal
            }

            R.id.pause_ratio_picker -> {
                if(config.cooldown == null) {
                    config.cooldown = Cooldown(50, newVal)
                }
                config.cooldown?.pause = newVal
            }

            else -> {
                return //This allows to skip unsavedChanges = true
            }
        }

        unsavedChanges = true
    }
}
