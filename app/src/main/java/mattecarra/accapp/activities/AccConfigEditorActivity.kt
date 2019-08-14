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
import mattecarra.accapp.R
import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip
import kotlinx.coroutines.runBlocking
import mattecarra.accapp.Preferences
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.utils.Constants
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class AccConfigEditorActivity : AppCompatActivity(), NumberPicker.OnValueChangeListener {
    private lateinit var viewModel: AccConfigEditorViewModel
    private lateinit var mUndoMenuItem: MenuItem
    private lateinit var mPreferences: Preferences

    private fun returnResults() {
        val returnIntent = Intent()
        returnIntent.putExtra(Constants.DATA_KEY, intent.getBundleExtra("data"))
        returnIntent.putExtra(Constants.ACC_HAS_CHANGES, viewModel.unsavedChanges)
        returnIntent.putExtra(Constants.ACC_CONFIG_KEY, viewModel.accConfig)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acc_config_editor)

        // Load preferences
        mPreferences = Preferences(this)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = intent?.getStringExtra("titleTv") ?: getString(R.string.acc_config_editor)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val config =
            when {
                savedInstanceState?.containsKey("mAccConfig") == true ->
                    savedInstanceState.getParcelable("mAccConfig")!!

                intent.hasExtra(Constants.ACC_CONFIG_KEY) ->
                    intent.getParcelableExtra(Constants.ACC_CONFIG_KEY)!!

                else ->
                    try {
                        runBlocking { Acc.instance.readConfig() }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        showConfigReadError()
                        Acc.instance.defaultConfig //if mAccConfig is null I use default mAccConfig values.
                    }
            }

        viewModel = ViewModelProviders.of(this, AccConfigEditorViewModelFactory(application, config)).get(AccConfigEditorViewModel::class.java)

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

        mUndoMenuItem = menu.findItem(R.id.action_undo)

        viewModel.undoOperationAvailableLiveData.observe(this, Observer {
            mUndoMenuItem.isEnabled = it
        })

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
            R.id.action_restore -> {
                viewModel.accConfig = Acc.instance.defaultConfig
            }
            R.id.action_undo -> {
                viewModel.undoLastConfigOperation()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if(viewModel.unsavedChanges) {
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

    private fun updateCapacityCard(configCapacity: AccConfig.ConfigCapacity) {
        shutdown_capacity_picker.minValue = 0
        shutdown_capacity_picker.maxValue = 20
        shutdown_capacity_picker.value = configCapacity.shutdown

        resume_capacity_picker.minValue = configCapacity.shutdown
        resume_capacity_picker.maxValue = if(configCapacity.pause == 101) 101 else configCapacity.pause - 1
        resume_capacity_picker.value = configCapacity.resume

        pause_capacity_picker.minValue = if(configCapacity.resume == 101) 101 else configCapacity.resume + 1
        pause_capacity_picker.maxValue = 101
        pause_capacity_picker.value = configCapacity.pause
    }

    private fun updateChargeSwitch(configChargeSwitch: String?) {
        charging_switch_textview.text = configChargeSwitch ?: getString(R.string.automatic)
    }

    private fun updateTemperatureCard(configTemperature: AccConfig.ConfigTemperature) {
        if(configTemperature.coolDownTemperature >= 90 && configTemperature.maxTemperature >= 95) {
            temp_switch.isChecked = false
            temperature_cooldown_picker.isEnabled = false
            temperature_max_picker.isEnabled = false
            temperature_max_pause_seconds_picker.isEnabled = false
        } else {
            temp_switch.isChecked = true
            temperature_cooldown_picker.isEnabled = true
            temperature_max_picker.isEnabled = true
            temperature_max_pause_seconds_picker.isEnabled = true
        }

        temperature_cooldown_picker.minValue = 20
        temperature_cooldown_picker.maxValue = 90
        temperature_cooldown_picker.value = configTemperature.coolDownTemperature


        temperature_max_picker.minValue = 20
        temperature_max_picker.maxValue = 95
        temperature_max_picker.value = configTemperature.maxTemperature


        temperature_max_pause_seconds_picker.minValue = 10
        temperature_max_pause_seconds_picker.maxValue = 120
        temperature_max_pause_seconds_picker.value = configTemperature.pause
    }

    private fun updateCoolDownCard(configCoolDown: AccConfig.ConfigCoolDown?) {
        if(configCoolDown == null || configCoolDown.atPercent > 100) {
            cooldown_switch.isChecked = false
            cooldown_percentage_picker.isEnabled = false
            charge_ratio_picker.isEnabled = false
            pause_ratio_picker.isEnabled = false
        } else {
            cooldown_switch.isChecked = true
            cooldown_percentage_picker.isEnabled = true
            charge_ratio_picker.isEnabled = true
            pause_ratio_picker.isEnabled = true
        }

        cooldown_percentage_picker.minValue = 0
        cooldown_percentage_picker.maxValue = 100 //if someone wants to disable it should use the switch but I'm gonna leave it there
        cooldown_percentage_picker.value = configCoolDown?.atPercent ?: 60

        charge_ratio_picker.minValue = 1
        charge_ratio_picker.maxValue = 120 //no reason behind this value
        charge_ratio_picker.value = configCoolDown?.charge ?: 50

        pause_ratio_picker.minValue = 1
        pause_ratio_picker.maxValue = 120 //no reason behind this value
        pause_ratio_picker.value = configCoolDown?.pause ?: 10
    }

    private fun updateVoltageControlCard(configVoltage: AccConfig.ConfigVoltage) {
        voltage_control_file.text = configVoltage.controlFile ?: "Not supported"
        voltage_max.text = configVoltage.max?.let { "$it mV" } ?: getString(R.string.disabled)
    }


    private fun initUi() {
        viewModel.observeCapacity(this, Observer {
            updateCapacityCard(it)
        })

        viewModel.observeChargeSwitch(this, Observer {
            updateChargeSwitch(it)
        })

        viewModel.observeTemperature(this, Observer {
            updateTemperatureCard(it)
        })

        viewModel.observeCoolDown(this, Observer {
            updateCoolDownCard(it)
        })

        viewModel.observeVoltageLimit(this, Observer {
            updateVoltageControlCard(it)
        })

        viewModel.observeOnPlug(this, Observer { configOnPlug ->
            config_on_plugged_textview.text = configOnPlug?.let { if(it.isBlank()) getString(R.string.not_set) else it } ?: getString(R.string.not_set)
        })

        viewModel.observeOnBoot(this, Observer { configOnBoot ->
            tv_config_on_boot.text = configOnBoot?.let { if(it.isBlank()) getString(R.string.not_set) else it } ?: getString(R.string.not_set)
        })

        //capacity card
        shutdown_capacity_picker.setOnValueChangedListener(this)
        resume_capacity_picker.setOnValueChangedListener(this)
        pause_capacity_picker.setOnValueChangedListener(this)

        //temps
        temp_switch.setOnClickListener {
            if(temp_switch.isChecked) {
                viewModel.temperature = viewModel.temperature.copy(coolDownTemperature = 40, maxTemperature = 45)
            } else {
                viewModel.temperature = viewModel.temperature.copy(coolDownTemperature = 90, maxTemperature = 95)
            }
        }
        temperature_cooldown_picker.setOnValueChangedListener(this)
        temperature_max_picker.setOnValueChangedListener(this)
        temperature_max_pause_seconds_picker.setOnValueChangedListener(this)

        //coolDown
        cooldown_switch.setOnClickListener {
            if(cooldown_switch.isChecked) {
                viewModel.coolDown = AccConfig.ConfigCoolDown(60, 50, 10)
            } else {
                viewModel.coolDown = null
            }
        }
        cooldown_percentage_picker.setOnValueChangedListener(this)
        charge_ratio_picker.setOnValueChangedListener(this)
        pause_ratio_picker.setOnValueChangedListener(this)
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        if(picker == null) return

        when(picker.id) {
            //capacity
            R.id.shutdown_capacity_picker ->
                viewModel.capacity = viewModel.capacity.copy(shutdown = newVal)

            R.id.resume_capacity_picker ->
                viewModel.capacity = viewModel.capacity.copy(resume = newVal)

            R.id.pause_capacity_picker ->
                viewModel.capacity = viewModel.capacity.copy(pause = newVal)

            //temp
            R.id.temperature_cooldown_picker ->
                viewModel.temperature = viewModel.temperature.copy(coolDownTemperature = newVal)

            R.id.temperature_max_picker ->
                viewModel.temperature = viewModel.temperature.copy(maxTemperature = newVal)

            R.id.temperature_max_pause_seconds_picker ->
                viewModel.temperature = viewModel.temperature.copy(pause = newVal)

            //coolDown
            R.id.cooldown_percentage_picker ->
                viewModel.coolDown = viewModel.coolDown?.copy(atPercent = newVal)

            R.id.charge_ratio_picker -> {
                viewModel.coolDown = viewModel.coolDown?.copy(charge = newVal)
            }

            R.id.pause_ratio_picker -> {
                viewModel.coolDown = viewModel.coolDown?.copy(pause = newVal)
            }

            else -> {
                return //This allows to skip unsavedChanges = true
            }
        }
    }

    /**
     * Function for On Boot ImageView OnClick.
     * Opens the dialog to edit the On Boot mAccConfig parameter.
     */
    fun editOnBootOnClick(view: View) {
        MaterialDialog(this@AccConfigEditorActivity).show {
            title(R.string.edit_on_boot)
            message(R.string.edit_on_boot_dialog_message)
            input(prefill = viewModel.onBoot ?: "", allowEmpty = true, hintRes = R.string.edit_on_boot_dialog_hint) { _, text ->
                viewModel.onBoot = text.toString()
            }
            positiveButton(R.string.save)
            negativeButton(android.R.string.cancel)
        }
    }

    fun editOnPluggedOnClick(v: View) {
        MaterialDialog(this@AccConfigEditorActivity).show {
            title(R.string.edit_on_plugged)
            message(R.string.edit_on_plugged_dialog_message)
            input(prefill = viewModel.onPlug ?: "", allowEmpty = true, hintRes = R.string.edit_on_boot_dialog_hint) { _, text ->
                viewModel.onPlug = text.toString()
            }
            positiveButton(R.string.save)
            negativeButton(android.R.string.cancel)
        }
    }

    fun editChargingSwitchOnClick(v: View) {
        val automaticString = getString(R.string.automatic)
        val chargingSwitches = listOf(automaticString, *Acc.instance.listChargingSwitches().toTypedArray())
        val initialSwitch = viewModel.chargeSwitch
        var currentIndex = chargingSwitches.indexOf(initialSwitch ?: automaticString)

        MaterialDialog(this).show {
            title(R.string.edit_charging_switch)
            noAutoDismiss()

            setActionButtonEnabled(WhichButton.POSITIVE, currentIndex != -1)
            setActionButtonEnabled(WhichButton.NEUTRAL, currentIndex != -1)

            listItemsSingleChoice(items = chargingSwitches, initialSelection = currentIndex, waitForPositiveButton = false)  { _, index, text ->
                currentIndex = index

                setActionButtonEnabled(WhichButton.POSITIVE, index != -1)
                setActionButtonEnabled(WhichButton.NEUTRAL, index != -1)
            }

            positiveButton(R.string.save) {
                val index = currentIndex
                val switch = chargingSwitches[index]

                viewModel.chargeSwitch = if(index == 0) null else switch

                dismiss()
            }

            neutralButton(R.string.test_switch) {
                val switch = if(currentIndex == 0) null else chargingSwitches[currentIndex]

                Toast.makeText(this@AccConfigEditorActivity, R.string.wait, Toast.LENGTH_LONG).show()
                doAsync {
                    val description =
                        when(Acc.instance.testChargingSwitch(switch)) {
                            0 -> R.string.charging_switch_works
                            1 -> R.string.charging_switch_does_not_work
                            2 -> R.string.plug_battery_to_test
                            else -> R.string.error_occurred
                        }

                    uiThread {
                        MaterialDialog(this@AccConfigEditorActivity).show {
                            title(R.string.test_switch)
                            message(description)
                            positiveButton(android.R.string.ok)
                        }
                    }
                }
            }

            negativeButton(android.R.string.cancel) {
                dismiss()
            }
        }
    }

    fun editVoltageOnClick(v: View) {
        val dialog = MaterialDialog(this@AccConfigEditorActivity).show {
            customView(R.layout.voltage_control_editor_dialog)
            positiveButton(android.R.string.ok) { dialog ->
                val view = dialog.getCustomView()
                val voltageControl = view.findViewById<Spinner>(R.id.voltage_control_file)
                val voltageMax = view.findViewById<EditText>(R.id.voltage_max)
                val checkBox = dialog.findViewById<CheckBox>(R.id.enable_voltage_max)

                val voltageMaxInt = voltageMax.text.toString().toIntOrNull()
                if(checkBox.isChecked && voltageMaxInt != null) {
                    viewModel.voltageLimit = AccConfig.ConfigVoltage(
                        voltageControl.selectedItem as String,
                        voltageMaxInt
                    )
                } else {
                    viewModel.voltageLimit = viewModel.voltageLimit.copy(max = null)
                }
            }
            negativeButton(android.R.string.cancel)
        }

        //initialize dialog custom view:
        val view = dialog.getCustomView()
        val voltageMax = view.findViewById<EditText>(R.id.voltage_max)
        val checkBox = dialog.findViewById<CheckBox>(R.id.enable_voltage_max)
        val voltageControl = view.findViewById<Spinner>(R.id.voltage_control_file)

        val configVoltage = viewModel.voltageLimit

        voltageMax.setText(configVoltage.max?.toString() ?: "", TextView.BufferType.EDITABLE)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            voltageMax.isEnabled = isChecked

            val voltageMaxVal = voltageMax.text?.toString()?.toIntOrNull()
            val isValid = voltageMaxVal != null && voltageMaxVal >= 3500 && voltageMaxVal <= 4350
            voltageMax.error = if (isValid) null else getString(R.string.invalid_voltage_max)
            dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid  && voltageControl.selectedItemPosition != -1)
        }
        checkBox.isChecked = configVoltage.max != null
        voltageMax.isEnabled = checkBox.isChecked
        voltageMax.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val voltageMaxVal = s?.toString()?.toIntOrNull()
                val isValid = voltageMaxVal != null && voltageMaxVal >= 3500 && voltageMaxVal <= 4350
                voltageMax.error = if(isValid) null else getString(R.string.invalid_voltage_max)
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid  && voltageControl.selectedItemPosition != -1)
            }
        })

        val supportedVoltageControlFiles = ArrayList(Acc.instance.listVoltageSupportedControlFiles())
        val currentVoltageFile = configVoltage.controlFile?.let { currentVoltFile ->
            val currentVoltFileRegex = currentVoltFile.replace("/", """\/""").replace(".", """\.""").replace("?", ".").toRegex()
            val match = supportedVoltageControlFiles.find { currentVoltFileRegex.matches(it) }
            if(match == null) {
                supportedVoltageControlFiles.add(currentVoltFile)
                currentVoltFile
            } else {
                match
            }
        }
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, supportedVoltageControlFiles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        voltageControl.adapter = adapter
        currentVoltageFile?.let {
            voltageControl.setSelection(supportedVoltageControlFiles.indexOf(currentVoltageFile))
        }
        if(voltageControl.selectedItemPosition == -1) {
            dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
        }
        voltageControl.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val voltageMaxVal = voltageMax.text?.toString()?.toIntOrNull()
                val isValid = voltageMaxVal != null && voltageMaxVal >= 3500 && voltageMaxVal <= 4350
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid && position != -1)
            }
        }
    }

    fun onInfoClick(v: View) {
        when(v.id) {
            R.id.capacity_control_info -> R.string.capacity_control_info
            R.id.voltage_control_info -> R.string.voltage_control_info
            R.id.temperature_control_info -> R.string.temperature_control_info
            R.id.exit_on_boot_info -> R.string.description_exit_on_boot
            R.id.cooldown_info -> R.string.cooldown_info
            R.id.on_plugged_info -> R.string.on_plugged_info
            else -> null
        }?.let {
            Tooltip.Builder(this)
                .anchor(v, 0, 0, false)
                .text(it)
                .arrow(true)
                .closePolicy(ClosePolicy.TOUCH_ANYWHERE_CONSUME)
                .showDuration(-1)
                .overlay(false)
                .maxWidth((resources.displayMetrics.widthPixels / 1.3).toInt())
                .styleId(R.style.ToolTipAltStyle)
                .create()
                .show(v, Tooltip.Gravity.LEFT, true)
        }
    }

    fun onPluggedRestore(view: View) {
        viewModel.onPlug = Acc.instance.defaultConfig.configOnPlug
    }

    fun onCooldownRestore(view: View) {
        viewModel.coolDown = Acc.instance.defaultConfig.configCoolDown
    }

    fun onCapacityRestore(view: View) {
        viewModel.accConfig = viewModel.accConfig.copy(configCapacity = Acc.instance.defaultConfig.configCapacity, configChargeSwitch = Acc.instance.defaultConfig.configChargeSwitch)
    }

    fun onVoltageControlRestore(view: View) {
        viewModel.voltageLimit = Acc.instance.defaultConfig.configVoltage
    }

    fun onTemperatureControlRestore(view: View) {
        viewModel.temperature = Acc.instance.defaultConfig.configTemperature
    }

    fun onBootRestoreClick(view: View) {
        viewModel.onBoot = Acc.instance.defaultConfig.configOnBoot
    }
}
