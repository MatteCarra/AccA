package mattecarra.accapp.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.content_acc_config_editor.*
import mattecarra.accapp.R
import android.app.Activity
import android.content.Intent
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.afollestad.materialdialogs.list.toggleItemChecked
import com.afollestad.materialdialogs.list.updateListItemsSingleChoice
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip
import kotlinx.android.synthetic.main.add_charging_switch_dialog.view.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mattecarra.accapp.Preferences
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.dialogs.progress
import mattecarra.accapp.dialogs.powerLimitDialog
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.utils.Constants
import mattecarra.accapp.utils.ScopedAppActivity

class AccConfigEditorActivity : ScopedAppActivity(), NumberPicker.OnValueChangeListener {
    private lateinit var viewModel: AccConfigEditorViewModel
    private lateinit var mUndoMenuItem: MenuItem
    private lateinit var mPreferences: Preferences

    private fun returnResults() {
        val returnIntent = Intent()
        returnIntent.putExtra(Constants.DATA_KEY, intent.getBundleExtra(Constants.DATA_KEY))
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

        val toolbar = findViewById<View>(R.id.acc_conf_editor_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = intent?.getStringExtra(Constants.TITLE_KEY) ?: getString(R.string.acc_config_editor)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val config =
            when {
                savedInstanceState?.containsKey(Constants.ACC_CONFIG_KEY) == true ->
                    savedInstanceState.getSerializable(Constants.ACC_CONFIG_KEY) as AccConfig

                intent.hasExtra(Constants.ACC_CONFIG_KEY) ->
                    intent.getSerializableExtra(Constants.ACC_CONFIG_KEY) as AccConfig

                else ->
                    try {
                        runBlocking { Acc.instance.readConfig() }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        showConfigReadError()
                        runBlocking { Acc.instance.readDefaultConfig() } //if mAccConfig is null I use default mAccConfig values.
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
                launch {
                    viewModel.accConfig = Acc.instance.readDefaultConfig()
                }
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
        automatic_switch_enabled_switch.isEnabled = configChargeSwitch != null
        if(configChargeSwitch == null) {
            automatic_switch_enabled_switch.isChecked = true
        }
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
        voltage_control_file_spinner.text = configVoltage.controlFile ?: "Not supported"
        voltage_max_edit_text.text = configVoltage.max?.let { "$it mV" } ?: getString(R.string.disabled)
    }

    private fun updateCurrentMaxControlCard(currentMax: Int?) {
        current_max_edit_text.text = currentMax?.let { "$it mA" } ?: getString(R.string.disabled)
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

        viewModel.observeCurrentMax(this, Observer {
            updateCurrentMaxControlCard(it)
        })

        viewModel.observeOnPlug(this, Observer { configOnPlug ->
            config_on_plugged_textview.text = configOnPlug?.let { if(it.isBlank()) getString(R.string.voltage_control_file_not_set) else it } ?: getString(R.string.voltage_control_file_not_set)
        })

        viewModel.observeOnBoot(this, Observer { configOnBoot ->
            tv_config_on_boot.text = configOnBoot?.let { if(it.isBlank()) getString(R.string.voltage_control_file_not_set) else it } ?: getString(R.string.voltage_control_file_not_set)
        })

        viewModel.observePrioritizeBatteryIdleMode(this, Observer {
            battery_idle_switch.isChecked = it
        })

        viewModel.observeResetBSOnUnplug(this, Observer {
            dash_resetStatusUnplug_switch.isChecked = it
        })

        viewModel.observeResetBSOnPause(this, Observer {
            dash_resetBSOnPause_switch.isChecked = it
        })

        viewModel.observeIsAutomaticSwitchEnabled(this, Observer {
            automatic_switch_enabled_switch.isChecked = it
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

        //battery idle
        battery_idle_switch.setOnClickListener {
            viewModel.prioritizeBatteryIdleMode = battery_idle_switch.isChecked
        }

        //reset bs on pause
        dash_resetBSOnPause_switch.setOnClickListener {
            viewModel.resetBSOnPause = dash_resetBSOnPause_switch.isChecked
        }

        //reset bs on unplug
        dash_resetStatusUnplug_switch.setOnClickListener {
            viewModel.resetBSOnUnplug = dash_resetStatusUnplug_switch.isChecked
        }

        if(Acc.instance.version < 202007220) {
            automatic_switch_enabled_switch.visibility = View.GONE
        }
        automatic_switch_enabled_switch.setOnClickListener {
            viewModel.isAutomaticSwitchEanbled = automatic_switch_enabled_switch.isChecked
        }

        //power card
        if(Acc.instance.version >= 202002170) {
            voltage_control_file_ll.visibility = View.GONE
        } else {
            current_max_ll.visibility = View.GONE
        }
    }

    fun onBatteryIdleTestButtonClick(v: View) {
        launch {
            val dialog = MaterialDialog(this@AccConfigEditorActivity).show {
                title(R.string.test_battery_idle)
                progress(R.string.wait)
            }

            val (exitCode, supported) = Acc.instance.isBatteryIdleSupported()
            if(dialog.isShowing) {
                dialog.cancel()

                if(exitCode == 2) { //battery is not charging -> can not test
                    MaterialDialog(this@AccConfigEditorActivity).show {
                        title(R.string.test_battery_idle)
                        message(R.string.plug_battery_to_test)
                        positiveButton(R.string.retry) {
                            onBatteryIdleTestButtonClick(v)
                        }
                        negativeButton(android.R.string.cancel)
                    }
                } else {
                    if(!supported)
                        viewModel.prioritizeBatteryIdleMode = false

                    this@AccConfigEditorActivity.battery_idle_switch.isEnabled = supported

                    MaterialDialog(this@AccConfigEditorActivity).show {
                        title(R.string.test_battery_idle)

                        if(!supported) {
                            this@AccConfigEditorActivity.battery_idle_switch.isChecked = false
                            message(R.string.test_battery_idle_unsupported_result)
                        } else {
                            message(R.string.test_battery_idle_supported_result)
                        }

                        positiveButton(android.R.string.ok)
                    }
                }

            }
        }
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
        val addNewChargingSwitchString = getString(R.string.add_charging_switch)
        val initialSwitch = viewModel.chargeSwitch

        MaterialDialog(this).show {
            title(R.string.edit_charging_switch)
            noAutoDismiss()

            launch {
                var chargingSwitches = listOf(automaticString, addNewChargingSwitchString, *Acc.instance.listChargingSwitches().toTypedArray())
                var currentIndex = chargingSwitches.indexOf(initialSwitch ?: automaticString)

                setActionButtonEnabled(WhichButton.POSITIVE, currentIndex != -1)
                setActionButtonEnabled(WhichButton.NEUTRAL, currentIndex != -1)

                listItemsSingleChoice(items = chargingSwitches, initialSelection = currentIndex, waitForPositiveButton = false)  { _, index, text ->
                    if(index == 1) { //Add new charging switch
                        val previousDialog = this@show //I need to keep a reference of the listItems dialog, to update the list of items
                        MaterialDialog(this@AccConfigEditorActivity).show {
                            noAutoDismiss()
                            title(text = addNewChargingSwitchString)
                            customView(R.layout.add_charging_switch_dialog)
                            positiveButton { dialog ->
                                val progressDialog = MaterialDialog(this@AccConfigEditorActivity).show {
                                    title(R.string.test_switch)
                                    progress(R.string.wait)
                                }

                                val view = dialog.getCustomView()
                                val switch = "${view.charging_switch_edit_text.text} ${view.charging_switch_on_value_edit_text.text} ${view.charging_switch_off_value_edit_text.text}"
                                this@AccConfigEditorActivity.launch {
                                    var success = true

                                    if(Acc.instance.isBatteryCharging()) { //If battery is charging the switch is tested
                                        if(Acc.instance.testChargingSwitch(switch) != 0) {
                                            success = false
                                            Toast.makeText(this@AccConfigEditorActivity, R.string.charging_switch_does_not_work, Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    if(success) {
                                        chargingSwitches = listOf(*chargingSwitches.toTypedArray(), switch) //update the list of switches with the new switch

                                        if(Acc.instance.addChargingSwitch(switch)) {
                                            previousDialog.updateListItemsSingleChoice(items = chargingSwitches)
                                            currentIndex = chargingSwitches.size - 1
                                        } else {
                                            Toast.makeText(this@AccConfigEditorActivity, R.string.error_occurred, Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    progressDialog.dismiss()
                                    dismiss()
                                }
                            }
                            negativeButton {
                                dismiss()
                            }
                            onDismiss {
                                previousDialog.toggleItemChecked(currentIndex) //Select the correct item when closing this dialog
                            }
                        }

                        return@listItemsSingleChoice
                    }

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

                    val dialog = MaterialDialog(this@AccConfigEditorActivity).show {
                        title(R.string.test_switch)
                        progress(R.string.wait)
                    }

                    this@AccConfigEditorActivity.launch {
                        val description =
                            when(Acc.instance.testChargingSwitch(switch)) {
                                0 -> R.string.charging_switch_works
                                1 -> R.string.charging_switch_does_not_work
                                2 -> R.string.plug_battery_to_test
                                else -> R.string.error_occurred
                            }

                        dialog.cancel()

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

    fun editPowerOnClick(v: View) {
        MaterialDialog(this@AccConfigEditorActivity).show {
            powerLimitDialog(viewModel.voltageLimit, viewModel.currentMaxLimit, this@AccConfigEditorActivity) { controlFile, voltageMaxEnabled, voltageMax, currentMaxEnabled, currentMax  ->
                if(voltageMaxEnabled && voltageMax != null) {
                    viewModel.voltageLimit = AccConfig.ConfigVoltage(
                        controlFile,
                        voltageMax
                    )
                } else {
                    viewModel.voltageLimit = viewModel.voltageLimit.copy(max = null)
                }

                viewModel.currentMaxLimit = if(currentMaxEnabled) currentMax else null
            }
            negativeButton(android.R.string.cancel)
        }
    }

    fun onInfoClick(v: View) {
        when(v.id) {
            R.id.capacity_control_info -> R.string.capacity_control_info
            R.id.power_control_info -> R.string.power_control_info
            R.id.temperature_control_info -> R.string.temperature_control_info
            R.id.exit_on_boot_info -> R.string.description_exit_on_boot
            R.id.cooldown_info -> R.string.cooldown_info
            R.id.on_plugged_info -> R.string.on_plugged_info
            R.id.battery_idle_control_info -> R.string.battery_idle_info_label
            R.id.miscellaneous_info -> R.string.miscellaneous_info_label
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
        launch {
            viewModel.onPlug = Acc.instance.readDefaultConfig().configOnPlug
        }
    }

    fun onCooldownRestore(view: View) {
        launch {
            viewModel.coolDown = Acc.instance.readDefaultConfig().configCoolDown
        }
    }

    fun onCapacityRestore(view: View) {
        launch {
            val defaultConfig = Acc.instance.readDefaultConfig()
            viewModel.capacity = defaultConfig.configCapacity
            viewModel.chargeSwitch = defaultConfig.configChargeSwitch
        }
    }

    fun onPowerControlRestore(view: View) {
        launch {
            val defaultConfig = Acc.instance.readDefaultConfig()
            viewModel.voltageLimit = defaultConfig.configVoltage
            viewModel.currentMaxLimit = defaultConfig.configCurrMax
        }
    }

    fun onTemperatureControlRestore(view: View) {
        launch {
            viewModel.temperature = Acc.instance.readDefaultConfig().configTemperature
        }
    }

    fun onBootRestoreClick(view: View) {
        launch {
            viewModel.onBoot = Acc.instance.readDefaultConfig().configOnBoot
        }
    }

    fun onBatteryIdleRestore(v: View) {
        launch {
            viewModel.prioritizeBatteryIdleMode = Acc.instance.readDefaultConfig().prioritizeBatteryIdleMode
        }
    }

    fun onMiscRestore(v: View) {
        launch {
            val config = Acc.instance.readDefaultConfig()
            viewModel.resetBSOnPause = config.configResetBsOnPause
            viewModel.resetBSOnUnplug = config.configResetUnplugged
        }
    }
}
