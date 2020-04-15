package mattecarra.accapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import mattecarra.accapp.Preferences
import mattecarra.accapp.R
import mattecarra.accapp.SharedViewModel
import mattecarra.accapp._interface.OnProfileClickListener
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.dialogs.*
import mattecarra.accapp.djs.Djs
import mattecarra.accapp.fragments.DashboardFragment
import mattecarra.accapp.fragments.ProfilesFragment
import mattecarra.accapp.fragments.SchedulesFragment
import mattecarra.accapp.fragments.SchedulesViewModel
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.Schedule
import mattecarra.accapp.utils.*
import java.io.File

class MainActivity : ScopedAppActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    OnProfileClickListener {

    private val LOG_TAG = "MainActivity"
    private val ACC_CONFIG_EDITOR_REQUEST = 1
    private val ACC_PROFILE_CREATOR_REQUEST = 2
    private val ACC_PROFILE_EDITOR_REQUEST = 3
    private val ACC_ADD_PROFILE_SCHEDULER_REQUEST = 4
    private val ACC_EDIT_PROFILE_SCHEDULER_REQUEST = 5

    private lateinit var mPreferences: Preferences
    private lateinit var mViewModel: SharedViewModel
    private lateinit var mMainActivityViewModel: MainActivityViewModel
    private lateinit var mSchedulesViewModel: SchedulesViewModel

    val mMainFragment = DashboardFragment.newInstance()
    val mProfilesFragment = ProfilesFragment.newInstance()
    val mSchedulesFragment = SchedulesFragment.newInstance()

    private fun initUi() {
        // Assign ViewModel
        mViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        mMainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        mSchedulesViewModel = ViewModelProviders.of(this).get(SchedulesViewModel::class.java)

        // Set Bottom Navigation Bar Item Selected Listener
        main_bottom_nav.setOnNavigationItemSelectedListener(this)
        setSupportActionBar(main_toolbar)

        // Load in dashboard fragment
        main_bottom_nav.selectedItemId = mMainActivityViewModel.selectedNavBarItem
    }


    private fun showConfigReadError() {
        MaterialDialog(this).show {
            title(R.string.config_error_title)
            message(R.string.config_error_dialog)
            positiveButton(android.R.string.ok)
        }
    }

    /**
     * Function for handing navigation bar clicks
     */
    override fun onNavigationItemSelected(m: MenuItem): Boolean {
        // Record currently selected navigation item
        mMainActivityViewModel.selectedNavBarItem = m.itemId

        when (m.itemId) {
            R.id.botNav_home -> {
                loadFragment(mMainFragment)
                return true
            }
            R.id.botNav_profiles -> {
                loadFragment(mProfilesFragment)
                return true
            }
            R.id.botNav_schedules -> {
                return if (!Djs.isDjsInstalled(filesDir)) {
                    djsInstallationDialog()
                    false
                } else {
                    if (!Djs.initDjs(filesDir) || Djs.isInstalledDjsOutdated()) {
                        installDjs()
                        false
                    } else {
                        loadFragment(mSchedulesFragment)
                        true
                    }
                }
            }
        }

        return false
    }

    fun djsInstallationDialog() {
        MaterialDialog(this).show {
            title(R.string.install_djs_title)
            message(R.string.install_djs_description)
            positiveButton(R.string.install) {
                installDjs()
            }
            negativeButton(android.R.string.no)
        }
    }

    fun installDjs() {
        MaterialDialog(this@MainActivity).show {
            title(R.string.installing_djs)
            cancelOnTouchOutside(false)
            onKeyCodeBackPressed { false }
            djsInstallation(this@MainActivity, object : DjsInstallationListener {
                override fun onInstallationFailed(result: Shell.Result?) {
                    MaterialDialog(this@MainActivity)
                        .show {
                            title(R.string.djs_installation_failed_title)
                            message(R.string.djs_installation_failed)
                            positiveButton(R.string.retry) {
                                installDjs()
                            }
                            negativeButton(android.R.string.cancel) {
                                main_bottom_nav.selectedItemId = R.id.botNav_schedules
                            }
                            if (result != null)
                                shareLogsNeutralButton(
                                    File(
                                        filesDir,
                                        "logs/djs-install.log"
                                    ), R.string.djs_installation_failed_log
                                )
                        }
                }

                override fun onBusyboxMissing() {
                    MaterialDialog(this@MainActivity)
                        .show {
                            busyBoxError()
                            positiveButton(R.string.retry) {
                                installDjs()
                            }
                            negativeButton(android.R.string.cancel) {
                                main_bottom_nav.selectedItemId = R.id.botNav_schedules
                            }
                            cancelOnTouchOutside(false)
                        }
                }

                override fun onSuccess() {
                    mPreferences.djsEnabled = true
                    initUi()
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item!!.itemId) {
        R.id.menu_appbar_logs -> {
            startActivity(Intent(this, LogViewerActivity::class.java))
            true
        }
        R.id.menu_appbar_settings -> {
            SettingsActivity.launch(this)
            true
        }
        R.id.menu_appbar_about -> {
            AboutActivity.launch(this)
            true
        }
        R.id.menu_appbar_import_export -> {
            ImportExportActivity.launch(this)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_framelayout, fragment)
        transaction.commit()
    }

    /**
     * Function for Status Card Settings OnClick (Configuration)
     */
    fun batteryConfigOnClick(view: View) {
        Intent(view.context, AccConfigEditorActivity::class.java).also { intent ->
            startActivityForResult(intent, ACC_CONFIG_EDITOR_REQUEST)
        }
    }

    /**
     * Function for launching the profile creation Activity
     */
    fun accProfilesFabOnClick(view: View) {
        Intent(this@MainActivity, AccConfigEditorActivity::class.java).also { intent ->
            intent.putExtra(
                Constants.TITLE_KEY,
                this@MainActivity.getString(R.string.profile_creator)
            )
            intent.putExtra(Constants.ACC_CONFIG_KEY, Acc.instance.defaultConfig)
            startActivityForResult(intent, ACC_PROFILE_CREATOR_REQUEST)
        }
    }

    /**
     * Override function for handling ProfileOnClicks
     */
    override fun onProfileClick(profile: AccaProfile) {
        // Applies the selected profile

        launch {
            mViewModel.updateAccConfig(profile.accConfig)
            mViewModel.setCurrentSelectedProfile(profile.uid)
        }

        // Display Toast for the user.
        Toast.makeText(
            this,
            getString(R.string.selecting_profile_toast, profile.profileName),
            Toast.LENGTH_LONG
        ).show()

    }

    override fun onProfileLongClick(profile: AccaProfile) {
        val dialog = MaterialDialog(this@MainActivity).customView(
            R.layout.profile_preview_dialog,
            scrollable = true
        )

        val preView = dialog.getCustomView()
        // Set view items and assign values
        val titleTv = preView.findViewById<TextView>(R.id.preview_profile_title_tv)
        val capacityTv = preView.findViewById<TextView>(R.id.preview_profile_capacity_tv)
        val chargingVoltTv =
            preView.findViewById<TextView>(R.id.preview_profile_charging_voltage_tv)
        val temperatureTv = preView.findViewById<TextView>(R.id.preview_profile_temperature_tv)
        val onBootTv = preView.findViewById<TextView>(R.id.preview_profile_on_boot_tv)
        val onPlugTv = preView.findViewById<TextView>(R.id.preview_profile_on_plug_tv)
        val coolDownTv = preView.findViewById<TextView>(R.id.preview_profile_cool_down_tv)

        // Assign the appropriate text values from the profile
        titleTv.text = profile.profileName
        capacityTv.text = profile.accConfig.configCapacity.toString(this)
        chargingVoltTv.text = profile.accConfig.configVoltage.toString()
        temperatureTv.text = profile.accConfig.configTemperature.toString(this)
        onBootTv.text = profile.accConfig.configOnBoot
        onPlugTv.text = profile.accConfig.getOnPlug(this)
        coolDownTv.text = profile.accConfig.configCoolDown?.toString(this) ?: "Cool Down Not Set"

        dialog.show()
    }

    private fun checkAccInstalled(): Boolean {
        val version = mPreferences.accVersion

        if (!Acc.isAccInstalled(filesDir) || !Acc.initAcc(filesDir) || (version == "bundled" && Acc.isInstalledAccOutdated())) {
            val dialog = MaterialDialog(this).show {
                title(R.string.installing_acc)
                progress(R.string.wait)
                cancelOnTouchOutside(false)
                onKeyCodeBackPressed { false }
            }

            launch {
                val res =
                    when (version) {
                        "bundled" ->
                            Acc.installBundledAccModule(this@MainActivity)
                        else ->
                            Acc.installAccModuleVersion(this@MainActivity, version)
                    }

                dialog.cancel()

                if (res?.isSuccess != true) {
                    when {
                        version != "bundled" -> //custom version installation had an error -> ask the user to select a different version
                            MaterialDialog(this@MainActivity) //Dialog to tell the user that installation failed
                                .show {
                                    title(R.string.acc_installation_failed_title)
                                    message(R.string.installation_failed_non_bundled)
                                    positiveButton(R.string.install_bundled_version) {
                                        mPreferences.accVersion = "bundled"
                                        if (checkAccInstalled()) {
                                            initUi()
                                        }
                                    }
                                    negativeButton(R.string.select_different_version) {
                                        MaterialDialog(this@MainActivity) //select a different acc version dailog
                                            .show {
                                                title(R.string.acc_version_picker_title)
                                                message(R.string.acc_version_picker_message)
                                                cancelOnTouchOutside(false)
                                                this@MainActivity.launch {
                                                    accVersionSingleChoice(mPreferences.accVersion) { version ->
                                                        mPreferences.accVersion = version

                                                        if (checkAccInstalled()) {
                                                            initUi()
                                                        }
                                                    }
                                                }
                                                onKeyCodeBackPressed {
                                                    dismiss()
                                                    finish()
                                                    false
                                                }
                                            }
                                    }
                                    if (res != null)
                                        shareLogsNeutralButton(
                                            File(
                                                filesDir,
                                                "logs/acc-install.log"
                                            ), R.string.acc_installation_failed_log
                                        )
                                    cancelOnTouchOutside(false)
                                }

                        res?.code == 3 -> //Buysbox is not installed
                            MaterialDialog(this@MainActivity)
                                .show {
                                    title(R.string.installation_failed_busybox_title)
                                    message(R.string.installation_failed_busybox)
                                    positiveButton(R.string.retry) {
                                        if (checkAccInstalled()) {
                                            initUi()
                                        }
                                    }
                                    negativeButton {
                                        finish()
                                    }
                                    cancelOnTouchOutside(false)
                                }

                        else -> //Other installation errors can not be handled automatically -> show a dialog with the logs
                            MaterialDialog(this@MainActivity)
                                .show {
                                    title(R.string.acc_installation_failed_title)
                                    message(R.string.acc_installation_failed)
                                    positiveButton(R.string.retry) {
                                        if (checkAccInstalled())
                                            initUi()
                                    }
                                    negativeButton {
                                        finish()
                                    }
                                    if (res != null)
                                        shareLogsNeutralButton(
                                            File(
                                                filesDir,
                                                "logs/acc-install.log"
                                            ), R.string.acc_installation_failed_log
                                        )
                                    cancelOnTouchOutside(false)
                                }
                    }.onKeyCodeBackPressed {
                        dialog.dismiss()
                        finish()
                        false
                    }
                } else {
                    initUi()
                }

                res?.let {
                    Log.d(LOG_TAG, it.out.joinToString("\n"))
                }
            }

            return false
        }

        val time = System.currentTimeMillis() / 1000
        if ((version == "master" || version == "dev") && time - mPreferences.lastUpdateCheck > 86400) {
            mPreferences.lastUpdateCheck = time
            checkUpdates(version)
        }

        return true
    }

    /*
    * This method should only be called when version = master | dev
    * It check if the installed version of acc is checked out to the latest commit and updates it if it's not.
    * */
    private fun checkUpdates(version: String) {
        launch {
            val lastCommit = GithubUtils.getLatestAccCommit(version)

            if (lastCommit != mPreferences.lastCommit) {
                mPreferences.lastCommit = lastCommit

                MaterialDialog(this@MainActivity).show {
                    title(R.string.install_update_dialog)
                    message(text = getString(R.string.check_update_dialog_message, version))
                    positiveButton(android.R.string.yes) {
                        val dialog = MaterialDialog(this@MainActivity).show {
                            title(R.string.checking_updates)
                            progress(R.string.wait)
                            cancelOnTouchOutside(false)
                        }

                        launch {
                            val res = Acc.instance.upgrade(version)
                            dialog.cancel()

                            when (res?.code) {
                                6 ->
                                    Toast.makeText(
                                        this@MainActivity,
                                        R.string.no_update_available,
                                        Toast.LENGTH_LONG
                                    ).show()

                                0 ->
                                    Toast.makeText(
                                        this@MainActivity,
                                        R.string.update_completed,
                                        Toast.LENGTH_LONG
                                    ).show()

                                else -> {
                                    MaterialDialog(this@MainActivity) //Other installation errors can not be handled automatically -> show a dialog with the logs
                                        .show {
                                            title(R.string.acc_installation_failed_title)
                                            message(R.string.acc_installation_failed)
                                            positiveButton(android.R.string.ok) {
                                                initUi()
                                            }
                                            //TODO add logs
                                            //shareLogsNeutralButton(File(filesDir, "logs/acc-install.log"), R.string.acc_installation_failed_log)

                                            cancelOnTouchOutside(false)
                                        }
                                }
                            }
                        }

                    }
                    negativeButton(android.R.string.no)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(main_toolbar)

        // Load preferences
        mPreferences = Preferences(this)

        // Set theme
        setTheme()

        if (!Shell.rootAccess()) {
            val dialog = MaterialDialog(this).show {
                title(R.string.tile_acc_no_root)
                message(R.string.no_root_message)
                positiveButton(android.R.string.ok) {
                    finish()
                }
                cancelOnTouchOutside(false)
                onKeyCodeBackPressed {
                    dismiss()
                    finish()
                    false
                }
            }
            return
        }

        if (checkAccInstalled()) {
            initUi()
        }
    }

    /**
     * Function for setting the app's theme depending on saved preference.
     */
    private fun setTheme() {
        when (mPreferences.appTheme) {
            "0" -> setDefaultNightMode(MODE_NIGHT_NO)
            "1" -> setDefaultNightMode(MODE_NIGHT_YES)
            "2" -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the main_activity_menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_appbar_menu, menu)
        return true
    }

    override fun onBackPressed() {
        if (main_bottom_nav.selectedItemId == R.id.botNav_home) {
            super.onBackPressed()
        } else {
            main_bottom_nav.selectedItemId = R.id.botNav_home
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ACC_CONFIG_EDITOR_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data?.getBooleanExtra(Constants.ACC_HAS_CHANGES, false) == true) {
                    launch {
                        mViewModel.updateAccConfig(data.getParcelableExtra(Constants.ACC_CONFIG_KEY))

                        // Remove the current selected profile
                        mViewModel.clearCurrentSelectedProfile()
                    }
                }
            }
        } else if (requestCode == ACC_PROFILE_CREATOR_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val accConfig: AccConfig = data.getParcelableExtra(Constants.ACC_CONFIG_KEY)
                    val profileNameRegex = """^[^\\/:*?"<>|]+${'$'}""".toRegex()
                    MaterialDialog(this)
                        .show {
                            title(R.string.profile_name)
                            message(R.string.dialog_profile_name_message)
                            input(waitForPositiveButton = false) { dialog, charSequence ->
                                val inputField = dialog.getInputField()
                                val isValid = profileNameRegex.matches(charSequence)

                                inputField.error =
                                    if (isValid) null else getString(R.string.invalid_chars)
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                            }
                            positiveButton(R.string.save) { dialog ->
                                val profileName = dialog.getInputField().text.toString()

                                // Add Profile to Database via ViewModel function
                                val profile = AccaProfile(
                                    0,
                                    profileName,
                                    accConfig
                                )

                                mMainActivityViewModel.insertProfile(profile)
                            }
                            negativeButton(android.R.string.cancel)
                        }
                }
            }
        } else if (requestCode == ACC_PROFILE_EDITOR_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data?.getBooleanExtra(
                        Constants.ACC_HAS_CHANGES,
                        false
                    ) == true && data.hasExtra(Constants.DATA_KEY)
                ) {
                    val accConfig: AccConfig = data.getParcelableExtra(Constants.ACC_CONFIG_KEY) ?: return

                    // Extract the data
                    val editorData = data.getBundleExtra(Constants.DATA_KEY) ?: return
                    val profileId = editorData.getInt(Constants.PROFILE_ID_KEY)
                    launch {
                        mMainActivityViewModel.getProfileById(profileId)?.let { selectedProfile ->
                            // Update the selected Profile
                            selectedProfile.accConfig = accConfig

                            // Update the profile
                            mMainActivityViewModel.updateProfile(selectedProfile)
                        }
                    }
                }
            }
        } else if (requestCode == ACC_ADD_PROFILE_SCHEDULER_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data?.hasExtra(Constants.DATA_KEY) == true) {
                data.getBundleExtra(Constants.DATA_KEY)?.let { dataBundle ->
                    val scheduleName = dataBundle.getString(Constants.SCHEDULE_NAME_KEY) ?: return
                    val time = dataBundle.getString(Constants.SCHEDULE_TIME_KEY)  ?: return
                    val executeOnce = dataBundle.getBoolean(Constants.SCHEDULE_EXEC_ONCE_KEY)
                    val executeOnBoot = dataBundle.getBoolean(Constants.SCHEDULE_EXEC_ONBOOT_KEY)

                    mSchedulesViewModel
                        .addSchedule(
                            scheduleName,
                            time,
                            executeOnce,
                            executeOnBoot,
                            data.getParcelableExtra(Constants.ACC_CONFIG_KEY)  ?: return
                        )
                }
            }
        } else if (requestCode == ACC_EDIT_PROFILE_SCHEDULER_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data?.hasExtra(Constants.DATA_KEY) == true) {
                data.getBundleExtra(Constants.DATA_KEY)?.let { dataBundle ->
                    val id = dataBundle.getInt(Constants.SCHEDULE_ID_KEY)
                    val scheduleName = dataBundle.getString(Constants.SCHEDULE_NAME_KEY) ?: return
                    val time = dataBundle.getString(Constants.SCHEDULE_TIME_KEY) ?: return
                    val executeOnce = dataBundle.getBoolean(Constants.SCHEDULE_EXEC_ONCE_KEY)
                    val executeOnBoot = dataBundle.getBoolean(Constants.SCHEDULE_EXEC_ONBOOT_KEY)
                    val enabled = dataBundle.getBoolean(Constants.SCHEDULE_ENABLED_KEY)

                    mSchedulesViewModel
                        .editSchedule(
                            id,
                            scheduleName,
                            enabled,
                            time,
                            executeOnce,
                            executeOnBoot,
                            data.getParcelableExtra(Constants.ACC_CONFIG_KEY) ?: return
                        )
                }
            }
        }
    }

    fun accScheduleFabOnClick(view: View) {
        MaterialDialog(this).show {
            title(R.string.create_schedule)
            addScheduleDialog(mMainActivityViewModel.profiles) { profileId, scheduleName, time, executeOnce, executeOnBoot ->
                if (profileId == -1L) {
                    Intent(this@MainActivity, AccConfigEditorActivity::class.java).also { intent ->
                        val dataBundle = Bundle()
                        dataBundle.putString(Constants.SCHEDULE_NAME_KEY, scheduleName)
                        dataBundle.putString(Constants.SCHEDULE_TIME_KEY, time)
                        dataBundle.putBoolean(Constants.SCHEDULE_EXEC_ONCE_KEY, executeOnce)
                        dataBundle.putBoolean(Constants.SCHEDULE_EXEC_ONBOOT_KEY, executeOnBoot)

                        intent.putExtra(Constants.DATA_KEY, dataBundle)
                        intent.putExtra(Constants.ACC_CONFIG_KEY, Acc.instance.defaultConfig)
                        intent.putExtra(Constants.TITLE_KEY, getString(R.string.schedule_creator))
                        startActivityForResult(intent, ACC_ADD_PROFILE_SCHEDULER_REQUEST)
                    }
                } else {
                    launch {
                        mMainActivityViewModel.getProfileById(profileId.toInt())?.let { configProfile ->
                            mSchedulesViewModel
                                .addSchedule(
                                    scheduleName,
                                    time,
                                    executeOnce,
                                    executeOnBoot,
                                    configProfile.accConfig
                                )
                        }
                    }
                }
            }
            negativeButton(android.R.string.cancel)
        }
    }

    fun editSchedule(schedule: Schedule) {
        MaterialDialog(this).show {
            title(R.string.edit_schedule)
            editScheduleDialog(
                schedule,
                mMainActivityViewModel.profiles
            ) { profileId, scheduleName, time, executeOnce, executeOnBoot ->
                when (profileId) {
                    -1L -> //keep current config
                        mSchedulesViewModel
                            .editSchedule(
                                schedule.profile.uid,
                                scheduleName,
                                schedule.isEnabled,
                                time,
                                executeOnce,
                                executeOnBoot,
                                schedule.profile.accConfig
                            )
                    -2L -> //edit current config
                        Intent(
                            this@MainActivity,
                            AccConfigEditorActivity::class.java
                        ).also { intent ->
                            val dataBundle = Bundle()
                            dataBundle.putInt(Constants.SCHEDULE_ID_KEY, schedule.profile.uid)
                            dataBundle.putString(Constants.SCHEDULE_NAME_KEY, scheduleName)
                            dataBundle.putString(Constants.SCHEDULE_TIME_KEY, time)
                            dataBundle.putBoolean(Constants.SCHEDULE_EXEC_ONCE_KEY, executeOnce)
                            dataBundle.putBoolean(Constants.SCHEDULE_EXEC_ONBOOT_KEY, executeOnBoot)
                            dataBundle.putBoolean(Constants.SCHEDULE_ENABLED_KEY, schedule.isEnabled)

                            intent.putExtra(Constants.DATA_KEY, dataBundle)
                            intent.putExtra(Constants.TITLE_KEY, getString(R.string.schedule_creator))
                            intent.putExtra(Constants.ACC_CONFIG_KEY, schedule.profile.accConfig)

                            startActivityForResult(intent, ACC_EDIT_PROFILE_SCHEDULER_REQUEST)
                        }
                    -3L -> //new custom config
                        Intent(
                            this@MainActivity,
                            AccConfigEditorActivity::class.java
                        ).also { intent ->
                            val dataBundle = Bundle()
                            dataBundle.putInt(Constants.SCHEDULE_ID_KEY, schedule.profile.uid)
                            dataBundle.putString(Constants.SCHEDULE_NAME_KEY, scheduleName)
                            dataBundle.putString(Constants.SCHEDULE_TIME_KEY, time)
                            dataBundle.putBoolean(Constants.SCHEDULE_EXEC_ONCE_KEY, executeOnce)
                            dataBundle.putBoolean(Constants.SCHEDULE_EXEC_ONBOOT_KEY, executeOnBoot)
                            dataBundle.putBoolean(Constants.SCHEDULE_ENABLED_KEY, schedule.isEnabled)

                            intent.putExtra(Constants.DATA_KEY, dataBundle)
                            intent.putExtra(Constants.TITLE_KEY, getString(R.string.schedule_creator))
                            intent.putExtra(Constants.ACC_CONFIG_KEY, Acc.instance.defaultConfig)
                            startActivityForResult(intent, ACC_EDIT_PROFILE_SCHEDULER_REQUEST)
                        }
                    else -> launch {
                        mMainActivityViewModel.getProfileById(profileId.toInt())?.let { configProfile ->
                                mSchedulesViewModel
                                    .editSchedule(
                                        schedule.profile.uid,
                                        scheduleName,
                                        schedule.isEnabled,
                                        time,
                                        executeOnce,
                                        executeOnBoot,
                                        configProfile.accConfig
                                    )
                            }
                    }
                }
            }
            negativeButton(android.R.string.cancel)
        }
    }

    override fun editProfile(profile: AccaProfile) {
        // Edit the configuration of the selected profile.
        Intent(
            this@MainActivity,
            AccConfigEditorActivity::class.java
        ).also { intent ->
            val dataBundle = Bundle()
            dataBundle.putInt(Constants.PROFILE_ID_KEY, profile.uid)

            // Insert the databundle into the intent.
            intent.putExtra(Constants.DATA_KEY, dataBundle)
            intent.putExtra(Constants.ACC_CONFIG_KEY, profile.accConfig)
            intent.putExtra(
                Constants.TITLE_KEY,
                this@MainActivity.getString(R.string.profile_creator)
            )
            startActivityForResult(intent, ACC_PROFILE_EDITOR_REQUEST)
        }
    }

    override fun renameProfile(profile: AccaProfile) {
        // Rename the selected profile (2nd option).
        MaterialDialog(this@MainActivity)
            .show {
                title(R.string.profile_name)
                message(R.string.dialog_profile_name_message)
                input(prefill = profile.profileName) { _, charSequence ->
                    // Set profile name
                    profile.profileName = charSequence.toString()

                    // Update the profile in the DB
                    mMainActivityViewModel.updateProfile(profile)
                }
                positiveButton(R.string.save)
                negativeButton(android.R.string.cancel)
            }
    }

    override fun deleteProfile(profile: AccaProfile) {
        // Delete the selected profile (3rd option).
        mMainActivityViewModel.deleteProfile(profile)
    }
}
