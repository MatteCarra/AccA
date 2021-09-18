package mattecarra.accapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.afollestad.materialdialogs.list.toggleItemChecked
import com.afollestad.materialdialogs.list.updateListItemsSingleChoice
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mattecarra.accapp.Preferences
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.databinding.ActivityAccConfigEditorBinding
import mattecarra.accapp.databinding.AddChargingSwitchDialogBinding
import mattecarra.accapp.databinding.ContentAccConfigEditorBinding
import mattecarra.accapp.dialogs.powerLimitDialog
import mattecarra.accapp.dialogs.progress
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ProfileEnables
import mattecarra.accapp.utils.Constants
import mattecarra.accapp.utils.ScopedAppActivity
import mattecarra.accapp.viewmodel.AccConfigEditorViewModel
import mattecarra.accapp.viewmodel.AccConfigEditorViewModelFactory

class AccConfigEditorActivity : ScopedAppActivity(),
    NumberPicker.OnValueChangeListener, CompoundButton.OnCheckedChangeListener
{
    private lateinit var content: ContentAccConfigEditorBinding
    private lateinit var viewModel: AccConfigEditorViewModel
    private lateinit var mUndoMenuItem: MenuItem
    private lateinit var mPreferences: Preferences
    private lateinit var initConfig: AccConfig
    private var accConfigOnly: Boolean = false

    private fun returnResults()
    {
        if (accConfigOnly)  // FIX OUT if load ONLY ACC Config
        {
            if (!viewModel.enables.eCoolDown) viewModel.coolDown = null
            if (!viewModel.enables.eVoltage) viewModel.voltageLimit = AccConfig.ConfigVoltage(null, null)
            if (!viewModel.enables.eRunOnBoot) viewModel.onBoot = null
            if (!viewModel.enables.eRunOnPlug) viewModel.onPlug = null
        }

        val returnIntent = Intent()
        returnIntent.putExtra(Constants.PROFILE_ID_KEY, intent.getIntExtra(Constants.PROFILE_ID_KEY, -1))
        returnIntent.putExtra(Constants.ACC_HAS_CHANGES, viewModel.unsavedChanges)
        returnIntent.putExtra(Constants.ACC_CONFIG_KEY, viewModel.profile.accConfig)
        returnIntent.putExtra(Constants.PROFILE_CONFIG_KEY, viewModel.profile)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val binding = ActivityAccConfigEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        content = binding.contentAccConfigEditor

        // Load preferences
        mPreferences = Preferences(this)

        setSupportActionBar(binding.accConfEditorToolbar)
        supportActionBar?.title = intent?.getStringExtra(Constants.TITLE_KEY) ?: getString(R.string.acc_config_editor)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val profile = when // load profile from intent
        {
            savedInstanceState?.containsKey(Constants.PROFILE_CONFIG_KEY) == true ->
                savedInstanceState.getSerializable(Constants.PROFILE_CONFIG_KEY) as AccaProfile

            intent.hasExtra(Constants.PROFILE_CONFIG_KEY) ->
                intent.getSerializableExtra(Constants.PROFILE_CONFIG_KEY) as AccaProfile

            else ->
            {
                accConfigOnly = true
                AccaProfile(-1,"", AccConfig(), ProfileEnables())
            }
        }

        val config = when // load config from intent or current config
        {
            savedInstanceState?.containsKey(Constants.ACC_CONFIG_KEY) == true ->
                savedInstanceState.getSerializable(Constants.ACC_CONFIG_KEY) as AccConfig

            intent.hasExtra(Constants.ACC_CONFIG_KEY) ->
                intent.getSerializableExtra(Constants.ACC_CONFIG_KEY) as AccConfig

            else -> try
            {
                runBlocking { Acc.instance.readConfig() }
            }
            catch (ex: Exception)
            {
                ex.printStackTrace()
                showConfigReadError()
                runBlocking { Acc.instance.readDefaultConfig() } //if mAccConfig is null I use default mAccConfig values.
            }
        }

        if (accConfigOnly) profile.accConfig = config
        initConfig = profile.accConfig.copy()

        viewModel = ViewModelProvider(this, AccConfigEditorViewModelFactory(application, profile))
            .get(AccConfigEditorViewModel::class.java)

        initUi()

        viewModel.clearHistory()
    }

    private fun initUi()
    {
        viewModel.observeEnables(this, Observer
        {
            content.capacitySwitchEnabled.isChecked = it.eCapacity
            content.voltcontrolSwitchEnabled.isChecked = it.eVoltage
            content.tempSwitchEnabled.isChecked = it.eTemperature
            content.cooldownSwitchEnabled.isChecked = it.eCoolDown
            content.applyOnBootSwitchEnabled.isChecked = it.eRunOnBoot
            content.onPluggedSwitchEnabled.isChecked = it.eRunOnPlug
        })

        viewModel.observePrioritizeBatteryIdleMode(this, Observer { content.batteryPrioritizeIdleSwitchEnabled.isChecked = it })
        viewModel.observeResetBSOnUnplug(this, Observer { content.resetStatusUnplugSwitch.isChecked = it })
        viewModel.observeResetBSOnPause(this, Observer { content.resetBSOnPauseSwitch.isChecked = it })
        viewModel.observeIsAutomaticSwitchEnabled(this, Observer { content.automaticSwitchEnabled.isChecked = it })

        viewModel.observeCapacity(this, Observer
        {
            content.shutdownCapacityPicker.minValue = 2
            content.shutdownCapacityPicker.maxValue = 20
            content.shutdownCapacityPicker.value = it.shutdown

            content.resumeCapacityPicker.minValue = it.shutdown
            content.resumeCapacityPicker.maxValue = if (it.pause == 101) 101 else it.pause - 1
            content.resumeCapacityPicker.value = it.resume

            content.pauseCapacityPicker.minValue = if (it.resume == 101) 101 else it.resume + 1
            content.pauseCapacityPicker.maxValue = 101
            content.pauseCapacityPicker.value = it.pause
        })

        viewModel.observeChargeSwitch(this, Observer
        {
            content.chargingSwitchTextview.text = it ?: getString(R.string.automatic)
            content.automaticSwitchEnabled.isEnabled = it != null
            if (it == null) content.automaticSwitchEnabled.isChecked = true
        })

        viewModel.observeTemperature(this, Observer
        {
            content.temperatureCooldownPicker.minValue = 20
            content.temperatureCooldownPicker.maxValue = 90
            content.temperatureCooldownPicker.value = it.coolDownTemperature

            content.temperatureMaxPicker.minValue = 20
            content.temperatureMaxPicker.maxValue = 95
            content.temperatureMaxPicker.value = it.maxTemperature

            content.temperatureMaxPauseSecondsPicker.minValue = 10
            content.temperatureMaxPauseSecondsPicker.maxValue = 120
            content.temperatureMaxPauseSecondsPicker.value = it.pause
        })

        viewModel.observeCoolDown(this, Observer
        {
            content.cooldownPercentagePicker.minValue = 0
            content.cooldownPercentagePicker.maxValue = 100
            content.cooldownPercentagePicker.value = it?.atPercent ?: 60

            content.cooldownChargeRatioPicker.minValue = 1
            content.cooldownChargeRatioPicker.maxValue = 120 //no reason behind this value
            content.cooldownChargeRatioPicker.value = it?.charge ?: 50

            content.cooldownPauseRatioPicker.minValue = 1
            content.cooldownPauseRatioPicker.maxValue = 120 //no reason behind this value
            content.cooldownPauseRatioPicker.value = it?.pause ?: 10
        })

        viewModel.observeVoltageLimit(this, Observer
        {
            content.voltageControlFileSpinner.text = it.controlFile ?: "Not supported"
            content.voltageMaxEditText.text = it.max?.let { "$it mV" } ?: getString(R.string.disabled)
        })

        viewModel.observeCurrentMax(this, Observer
        {
            content.currentMaxEditText.text = it?.let { "$it mA" } ?: getString(R.string.disabled)
        })

        viewModel.observeOnPlug(this, Observer
        { configOnPlug ->
            content.tvConfigOnPlugged.text = configOnPlug?.let { if(it.isBlank()) getString(R.string.voltage_control_file_not_set) else it } ?: getString(R.string.voltage_control_file_not_set)
        })

        viewModel.observeOnBoot(this, Observer
        { configOnBoot ->
            content.tvConfigOnBoot.text = configOnBoot?.let { if(it.isBlank()) getString(R.string.voltage_control_file_not_set) else it } ?: getString(R.string.voltage_control_file_not_set)
        })

        //--------------------------------------------------------------------------

        // InfoClick
        content.capacityControlInfo.setOnClickListener { onInfoClick(it) }
        content.powerControlInfo.setOnClickListener { onInfoClick(it) }
        content.temperatureControlInfo.setOnClickListener { onInfoClick(it) }
        content.exitOnBootInfo.setOnClickListener { onInfoClick(it) }
        content.cooldownInfo.setOnClickListener { onInfoClick(it) }
        content.onPluggedInfo.setOnClickListener { onInfoClick(it) }
        content.batteryIdleControlInfo.setOnClickListener { onInfoClick(it) }
        content.miscellaneousInfo.setOnClickListener { onInfoClick(it) }

        //capacity card
        content.shutdownCapacityPicker.setOnValueChangedListener(this)
        content.resumeCapacityPicker.setOnValueChangedListener(this)
        content.pauseCapacityPicker.setOnValueChangedListener(this)

        //temps
        content.temperatureCooldownPicker.setOnValueChangedListener(this)
        content.temperatureMaxPicker.setOnValueChangedListener(this)
        content.temperatureMaxPauseSecondsPicker.setOnValueChangedListener(this)

        //coolDown
        content.cooldownPercentagePicker.setOnValueChangedListener(this)
        content.cooldownChargeRatioPicker.setOnValueChangedListener(this)
        content.cooldownPauseRatioPicker.setOnValueChangedListener(this)

        //power card
        if (Acc.instance.version >= 202002170) content.voltageControlFileLl.visibility = View.GONE
        else content.currentMaxLl.visibility = View.GONE

        //SwitchEnabled
        content.capacitySwitchEnabled.setOnCheckedChangeListener(this)
        if (Acc.instance.version < 202007220) content.automaticSwitchEnabled.visibility = View.GONE
        content.automaticSwitchEnabled.setOnCheckedChangeListener(this)
        content.voltcontrolSwitchEnabled.setOnCheckedChangeListener(this)
        content.batteryPrioritizeIdleSwitchEnabled.setOnCheckedChangeListener(this)
        content.tempSwitchEnabled.setOnCheckedChangeListener(this)
        content.cooldownSwitchEnabled.setOnCheckedChangeListener(this)
        content.applyOnBootSwitchEnabled.setOnCheckedChangeListener(this)
        content.onPluggedSwitchEnabled.setOnCheckedChangeListener(this)
        content.resetStatusUnplugSwitch.setOnCheckedChangeListener (this)
        content.resetBSOnPauseSwitch.setOnCheckedChangeListener(this)

        if (accConfigOnly) // FIX Checks and Visibility if loaded ONLY ACC Config
        {
            content.capacitySwitchEnabled.visibility = View.GONE  // can't disabled
            content.tempSwitchEnabled.visibility = View.GONE

            viewModel.enables.eVoltage =
                (viewModel.voltageLimit.controlFile != null || viewModel.voltageLimit.max != null)

            viewModel.enables.eCapacity = true
            viewModel.enables.eTemperature = true
            viewModel.enables.eCoolDown = viewModel.coolDown != null
            viewModel.enables.eRunOnBoot = true
            viewModel.enables.eRunOnPlug = true
        }
    }

    private fun showConfigReadError()
    {
        MaterialDialog(this).show {
            title(R.string.config_error_title)
            message(R.string.config_error_dialog)
            positiveButton(android.R.string.ok)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.acc_config_editor_menu, menu)
        mUndoMenuItem = menu.findItem(R.id.action_undo)
        viewModel.undoOperationAvailableLiveData.observe(this, Observer { mUndoMenuItem.isEnabled = it })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.action_save -> returnResults()
            R.id.action_restore -> viewModel.profile.accConfig = initConfig.copy()
            R.id.action_undo -> viewModel.undoLastConfigOperation()
            android.R.id.home -> { onBackPressed(); return true }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed()
    {
        if (viewModel.unsavedChanges)
        {
            MaterialDialog(this).show {
                    title(R.string.unsaved_changes)
                    message(R.string.unsaved_changes_message)
                    positiveButton(R.string.save) { returnResults() }
                    negativeButton(R.string.close_without_saving) { finish() }
                    neutralButton(android.R.string.cancel)
                }
        }
        else super.onBackPressed()
    }

//    private fun updateAccSwitchCard(config: AccConfig)
//    {
//        content.automaticSwitchEnabled.isChecked = config.configIsAutomaticSwitchingEnabled
//        content.batteryPrioritizeIdleSwitchEnabled.isChecked = config.prioritizeBatteryIdleMode
//        content.resetBSOnPauseSwitch.isChecked = config.configResetBsOnPause
//        content.resetStatusUnplugSwitch.isChecked = config.configResetUnplugged
//    }

    private fun updateProfileSwitchCard(profileEnables: ProfileEnables)
    {}

    private fun updateCapacityCard(configCapacity: AccConfig.ConfigCapacity)
    {}

    private fun updateChargeSwitch(configChargeSwitch: String?)
    {}

    private fun updateTemperatureCard(configTemperature: AccConfig.ConfigTemperature)
    {}

    private fun updateCoolDownCard(configCoolDown: AccConfig.ConfigCoolDown?)
    {}

    private fun updateVoltageControlCard(configVoltage: AccConfig.ConfigVoltage)
    {}

    private fun updateCurrentMaxControlCard(currentMax: Int?)
    {}

    //-------------------------------------------------------------------------------------

    fun onBatteryIdleTestButtonClick(v: View)
    {
        launch {
            val dialog = MaterialDialog(this@AccConfigEditorActivity).show {
                title(R.string.test_battery_idle)
                progress(R.string.wait)
            }

            val (exitCode, supported) = Acc.instance.isBatteryIdleSupported()
            if (dialog.isShowing)
            {
                dialog.cancel()

                if (exitCode == 2)
                { //battery is not charging -> can not test
                    MaterialDialog(this@AccConfigEditorActivity).show {
                        title(R.string.test_battery_idle)
                        message(R.string.plug_battery_to_test)
                        positiveButton(R.string.retry) {
                            onBatteryIdleTestButtonClick(v)
                        }
                        negativeButton(android.R.string.cancel)
                    }
                }
                else
                {
                    if (!supported) viewModel.prioritizeBatteryIdleMode = false
                    content.batteryPrioritizeIdleSwitchEnabled.isEnabled = supported

                    MaterialDialog(this@AccConfigEditorActivity).show {
                        title(R.string.test_battery_idle)

                        if (!supported)
                        {
                            content.batteryPrioritizeIdleSwitchEnabled.isChecked = false
                            message(R.string.test_battery_idle_unsupported_result)
                        }
                        else
                        {
                            message(R.string.test_battery_idle_supported_result)
                        }
                        positiveButton(android.R.string.ok)
                    }
                }

            }
        }
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean)
    {
        when (p0)
        {
            content.capacitySwitchEnabled ->
            {
                viewModel.enables = viewModel.enables.copy(eCapacity = p1)
                content.shutdownCapacityPicker.isEnabled = p1
                content.resumeCapacityPicker.isEnabled = p1
                content.pauseCapacityPicker.isEnabled = p1
            }

            content.automaticSwitchEnabled ->
            {
                viewModel.isAutomaticSwitchEanbled = p1
                viewModel.profile.accConfig.configIsAutomaticSwitchingEnabled = p1
            }

            content.voltcontrolSwitchEnabled ->
            {
                viewModel.enables = viewModel.enables.copy(eVoltage = p1)
                content.editVoltageLimit.isEnabled = p1
            }

            content.batteryPrioritizeIdleSwitchEnabled ->
            {
                viewModel.prioritizeBatteryIdleMode = p1
                viewModel.profile.accConfig.prioritizeBatteryIdleMode = p1
                content.batteryIdleTestButton.isEnabled = p1
            }

            content.tempSwitchEnabled ->
            {
                viewModel.enables = viewModel.enables.copy(eTemperature = p1)
                content.temperatureCooldownPicker.isEnabled = p1
                content.temperatureMaxPicker.isEnabled = p1
                content.temperatureMaxPauseSecondsPicker.isEnabled = p1
            }

            content.cooldownSwitchEnabled ->
            {
                viewModel.enables = viewModel.enables.copy(eCoolDown = p1)
                content.cooldownPercentagePicker.isEnabled = p1
                content.cooldownChargeRatioPicker.isEnabled = p1
                content.cooldownPauseRatioPicker.isEnabled = p1
            }

            content.applyOnBootSwitchEnabled ->
            {
                viewModel.enables = viewModel.enables.copy(eRunOnBoot = p1)
                content.tvConfigOnBoot.isEnabled = p1
            }

            content.onPluggedSwitchEnabled ->
            {
                viewModel.enables = viewModel.enables.copy(eRunOnPlug = p1)
                content.tvConfigOnPlugged.isEnabled = p1
            }

            content.resetStatusUnplugSwitch ->
            {
                viewModel.resetBSOnUnplug = p1
                viewModel.profile.accConfig.configResetUnplugged = p1
            }

            content.resetBSOnPauseSwitch ->
            {
                viewModel.resetBSOnPause = p1
                viewModel.profile.accConfig.configResetBsOnPause = p1
            }
        }
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int)
    {
        when (picker)
        {
            //capacity
            content.shutdownCapacityPicker -> viewModel.capacity = viewModel.capacity.copy(shutdown = newVal)
            content.resumeCapacityPicker -> viewModel.capacity = viewModel.capacity.copy(resume = newVal)
            content.pauseCapacityPicker -> viewModel.capacity = viewModel.capacity.copy(pause = newVal)
            content.temperatureCooldownPicker -> viewModel.temperature = viewModel.temperature.copy(coolDownTemperature = newVal)
            content.temperatureMaxPicker -> viewModel.temperature = viewModel.temperature.copy(maxTemperature = newVal)
            content.temperatureMaxPauseSecondsPicker -> viewModel.temperature = viewModel.temperature.copy(pause = newVal)

            //coolDown
            content.cooldownPercentagePicker, content.cooldownChargeRatioPicker,
            content.cooldownPauseRatioPicker -> viewModel.coolDown =
                AccConfig.ConfigCoolDown(
                    content.cooldownPercentagePicker.value,
                    content.cooldownChargeRatioPicker.value,
                    content.cooldownPauseRatioPicker.value)

            else -> return
        }
    }

    /**
     * Function for On Boot ImageView OnClick.
     * Opens the dialog to edit the On Boot mAccConfig parameter.
     */

    @SuppressLint("CheckResult")
    fun editOnBootOnClick(view: View)
    {
        MaterialDialog(this@AccConfigEditorActivity).show {
            title(R.string.edit_on_boot)
            message(R.string.edit_on_boot_dialog_message)
            input(
                prefill = viewModel.onBoot ?: "",
                allowEmpty = true,
                hintRes = R.string.edit_on_boot_dialog_hint
            ) { _, text -> viewModel.onBoot = if (text.isNotBlank()) text.toString() else null }
            positiveButton(R.string.save)
            negativeButton(android.R.string.cancel)
            neutralButton(text = "clear", click = { viewModel.onBoot = null }  )
        }
    }

    @SuppressLint("CheckResult")
    fun editOnPluggedOnClick(v: View)
    {
        MaterialDialog(this@AccConfigEditorActivity).show {
            title(R.string.edit_on_plugged)
            message(R.string.edit_on_plugged_dialog_message)
            input(
                prefill = viewModel.onPlug ?: "",
                allowEmpty = true,
                hintRes = R.string.edit_on_boot_dialog_hint
            ) { _, text -> viewModel.onPlug = if (text.trim().isNotEmpty()) text.toString() else null }
            positiveButton(R.string.save)
            negativeButton(android.R.string.cancel)
            neutralButton(text = "clear", click = { viewModel.onPlug = null }  )

        }
    }

    @SuppressLint("CheckResult")
    fun editChargingSwitchOnClick(v: View)
    {
        val automaticString = getString(R.string.automatic)
        val addNewChargingSwitchString = getString(R.string.add_charging_switch)
        val initialSwitch = viewModel.chargeSwitch

        MaterialDialog(this).show {
            title(R.string.edit_charging_switch)
            noAutoDismiss()

            launch {
                var chargingSwitches = listOf(
                    automaticString,
                    addNewChargingSwitchString,
                    *Acc.instance.listChargingSwitches().toTypedArray()
                )

                var currentIndex = chargingSwitches.indexOf(initialSwitch ?: automaticString)

                setActionButtonEnabled(WhichButton.POSITIVE, currentIndex != -1)
                setActionButtonEnabled(WhichButton.NEUTRAL, currentIndex != -1)

                listItemsSingleChoice(
                    items = chargingSwitches,
                    initialSelection = currentIndex,
                    waitForPositiveButton = false
                ) { _, index, text ->
                    if (index == 1)
                    { //Add new charging switch
                        val previousDialog =
                            this@show //I need to keep a reference of the listItems dialog, to update the list of items
                        MaterialDialog(this@AccConfigEditorActivity).show {
                            noAutoDismiss()
                            title(text = addNewChargingSwitchString)
//                            customView(R.layout.add_charging_switch_dialog)
                            val binding = AddChargingSwitchDialogBinding.inflate(layoutInflater)
                            customView(view = binding.root)
                            positiveButton { dialog ->
                                val progressDialog =
                                    MaterialDialog(this@AccConfigEditorActivity).show {
                                        title(R.string.test_switch)
                                        progress(R.string.wait)
                                    }

//                                val view = dialog.getCustomView()
//                                val switch = "${view.charging_switch_edit_text.text} ${view.charging_switch_on_value_edit_text.text} ${view.charging_switch_off_value_edit_text.text}"
                                val switch = "${binding.chargingSwitchEditText.text} ${binding.chargingSwitchOnValueEditText.text} ${binding.chargingSwitchOffValueEditText.text}"
                                this@AccConfigEditorActivity.launch {
                                    var success = true

                                    if (Acc.instance.isBatteryCharging())
                                    { //If battery is charging the switch is tested
                                        if (Acc.instance.testChargingSwitch(switch) != 0)
                                        {
                                            success = false
                                            Toast.makeText(
                                                this@AccConfigEditorActivity,
                                                R.string.charging_switch_does_not_work,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    if (success)
                                    {
                                        chargingSwitches = listOf(*chargingSwitches.toTypedArray(), switch) //update the list of switches with the new switch

                                        if (Acc.instance.addChargingSwitch(switch))
                                        {
                                            previousDialog.updateListItemsSingleChoice(items = chargingSwitches)
                                            currentIndex = chargingSwitches.size - 1
                                        }
                                        else Toast.makeText(this@AccConfigEditorActivity, R.string.error_occurred, Toast.LENGTH_SHORT).show()
                                    }

                                    progressDialog.dismiss()
                                    dismiss()
                                }
                            }
                            negativeButton { dismiss() }
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
                    viewModel.chargeSwitch = if (currentIndex == 0) null else chargingSwitches[currentIndex]
                    dismiss()
                }

                neutralButton(R.string.test_switch) {
                    val switch = if (currentIndex == 0) null else chargingSwitches[currentIndex]

                    val dialog = MaterialDialog(this@AccConfigEditorActivity).show {
                        title(R.string.test_switch)
                        progress(R.string.wait)
                    }

                    this@AccConfigEditorActivity.launch {
                        val description = when (Acc.instance.testChargingSwitch(switch))
                        {
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

            negativeButton(android.R.string.cancel) { dismiss() }
        }
    }

    fun editPowerOnClick(v: View)
    {
        MaterialDialog(this@AccConfigEditorActivity).show {
            powerLimitDialog(viewModel.voltageLimit, viewModel.currentMaxLimit, this@AccConfigEditorActivity)
            { controlFile, voltageMaxEnabled, voltageMax, currentMaxEnabled, currentMax ->

                if (voltageMaxEnabled && voltageMax != null)
                {
                    viewModel.voltageLimit = AccConfig.ConfigVoltage(controlFile, voltageMax)
                }
                else
                {
                    viewModel.voltageLimit = viewModel.voltageLimit.copy(max = null)
                }

                viewModel.currentMaxLimit = if (currentMaxEnabled) currentMax else null
            }
            negativeButton(android.R.string.cancel)
        }
    }

    fun onInfoClick(v: View)
    {
        when (v)
        {
            content.capacityControlInfo -> R.string.capacity_control_info
            content.powerControlInfo -> R.string.power_control_info
            content.temperatureControlInfo -> R.string.temperature_control_info
            content.exitOnBootInfo -> R.string.description_exit_on_boot
            content.cooldownInfo -> R.string.cooldown_info
            content.onPluggedInfo -> R.string.on_plugged_info
            content.batteryIdleControlInfo -> R.string.battery_idle_info_label
            content.miscellaneousInfo -> R.string.miscellaneous_info_label
            else -> null

        }?.let {
            Tooltip.Builder(this).anchor(v, 0, 0, false).text(it).arrow(true)
                .closePolicy(ClosePolicy.TOUCH_ANYWHERE_CONSUME).showDuration(-1).overlay(false)
                .maxWidth((resources.displayMetrics.widthPixels / 1.3).toInt())
                .styleId(R.style.ToolTipAltStyle).create().show(v, Tooltip.Gravity.LEFT, true)
        }
    }

    fun onCapacityRestore(view: View)
    {
        viewModel.capacity = initConfig.configCapacity
        viewModel.chargeSwitch = initConfig.configChargeSwitch
    }

    fun onPowerControlRestore(view: View)
    {
        viewModel.voltageLimit = initConfig.configVoltage
        viewModel.currentMaxLimit = initConfig.configCurrMax
    }

    fun onTemperatureControlRestore(view: View)
    {
        viewModel.temperature = initConfig.configTemperature
    }

    fun onBootRestoreClick(view: View)
    {
        viewModel.onBoot = initConfig.configOnBoot
    }

    fun onPluggedRestore(view: View)
    {
        viewModel.onPlug = initConfig.configOnPlug
    }

    fun onCooldownRestore(view: View)
    {
        viewModel.coolDown = initConfig.configCoolDown
    }

    fun onBatteryIdleRestore(v: View)
    {
        viewModel.prioritizeBatteryIdleMode = initConfig.prioritizeBatteryIdleMode
    }

    fun onMiscRestore(v: View)
    {
        viewModel.resetBSOnUnplug = initConfig.configResetUnplugged
    }
}
