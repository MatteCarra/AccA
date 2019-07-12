package mattecarra.accapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.activity_main.*
import mattecarra.accapp.R
import mattecarra.accapp.SharedViewModel
import mattecarra.accapp._interface.OnProfileClickListener
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.fragments.DashboardFragment
import mattecarra.accapp.fragments.ProfilesFragment
import mattecarra.accapp.fragments.SchedulesFragment
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.Constants
import mattecarra.accapp.utils.progress
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    OnProfileClickListener {

    private val LOG_TAG = "MainActivity"
    private val ACC_CONFIG_EDITOR_REQUEST = 1
    private val ACC_PROFILE_CREATOR_REQUEST = 2
    private val ACC_PROFILE_EDITOR_REQUEST = 3
    private val ACC_PROFILE_SCHEDULER_REQUEST = 4

    private lateinit var mViewModel: SharedViewModel
    private lateinit var mMainActivityViewModel: MainActivityViewModel

    val mMainFragment = DashboardFragment.newInstance()
    val mProfilesFragment = ProfilesFragment.newInstance()
    val mSchedulesFragment = SchedulesFragment.newInstance()

//    private var profilesAdapter: ProfilesViewAdapter? = null
//
//    private var batteryInfo: BatteryInfo? = null
//    private var isDaemonRunning = false

    // TODO: Move schedules to the Schedules Fragment
//    private lateinit var scheduleAdapter: ScheduleRecyclerViewAdapter
//
//    val addSchedule: (Schedule) -> Unit = { schedule ->
//        if(scheduleAdapter.itemCount == 0) {
//            no_schedules_jobs_textview.visibility = View.GONE
//            scheduled_jobs_recyclerview.visibility = View.VISIBLE
//        }
//        scheduleAdapter.add(schedule)
//    }
//
//    val deleteSchedule: (Schedule) -> Unit = { schedule ->
//        AccUtils.deleteSchedule(schedule.executeOnce, schedule.name)
//        scheduleAdapter.remove(schedule)
//
//        if(scheduleAdapter.itemCount == 0) {
//            no_schedules_jobs_textview.visibility = View.VISIBLE
//            scheduled_jobs_recyclerview.visibility = View.GONE
//        }
//    }

//    //Used to update battery info every second
//    private val handler = Handler()
//    private val updateUIRunnable = object : Runnable {
//        override fun run() {
//            val r = this //need this to make it recursive
//            doAsync {
//                val batteryInfo = AccUtils.getBatteryInfo()
//                isDaemonRunning = AccUtils.isAccdRunning()
//                uiThread {
//                    // Run accd UI check
//                    updateAccdStatus(isDaemonRunning)
//
//                    setBatteryInfo(batteryInfo)
//
//                    handler.postDelayed(r, 1000)// Repeat the same runnable code block again after 1 seconds
//                }
//            }
//        }
//    }


    private fun showConfigReadError() {
        MaterialDialog(this).show {
            title(R.string.config_error_title)
            message(R.string.config_error_dialog)
            positiveButton(android.R.string.ok)
        }
    }

    override fun onNavigationItemSelected(m: MenuItem): Boolean {
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
            // TODO: Chancge id of schedules menu item
            R.id.botNav_settings -> {
                loadFragment(mSchedulesFragment)
                return true
            }
        }

        return false
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
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
     * Function for starting and stopping the ACCD
     */
    fun accdStartStopOnClick(view: View) {

        Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show()

        doAsync {
            if (Acc.instance.isAccdRunning())
                Acc.instance.abcStopDaemon()
            else
                Acc.instance.abcStartDaemon()
        }
    }

    /**
     * Function for restarting the ACCD
     */
    fun accdRestartDaemonOnClick(view: View) {
        Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show()

        doAsync {
            Acc.instance.abcRestartDaemon()
        }
    }

    /**
     * Function for launching the profile creation Activity
     */
    fun accProfilesFabOnClick(view: View) {
        Intent(this@MainActivity, AccConfigEditorActivity::class.java).also { intent ->
            intent.putExtra("titleTv", this@MainActivity.getString(R.string.profile_creator))
            startActivityForResult(intent, ACC_PROFILE_CREATOR_REQUEST)
        }
    }

    /**
     * Override function for handling ProfileOnClicks
     */
    override fun onProfileClick(profile: AccaProfile) {
        // Applies the selected profile

        doAsync {
            mViewModel.updateAccConfig(profile.accConfig)
            mViewModel.setCurrentSelectedProfile(profile.uid)
        }

        // Display Toast for the user.
        Toast.makeText(
            this,
            getString(R.string.profile_selected_toast, profile.profileName),
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

    override fun onProfileOptionsClick(profile: AccaProfile) {
        MaterialDialog(this@MainActivity).show {
            listItems(R.array.profile_long_press_options) { _, index, _ ->
                when (index) {
                    0 -> {
                        // Edit the configuration of the selected profile.
                        Intent(
                            this@MainActivity,
                            AccConfigEditorActivity::class.java
                        ).also { intent ->
                            val dataBundle = Bundle()
                            dataBundle.putInt(Constants.PROFILE_ID_KEY, profile.uid)
                            dataBundle.putParcelable(Constants.ACC_CONFIG_KEY, profile.accConfig)

                            // Insert the databundle into the intent.
                            intent.putExtra(Constants.DATA_KEY, dataBundle)
                            intent.putExtra(
                                Constants.TITLE_KEY,
                                this@MainActivity.getString(R.string.profile_creator)
                            )
                            startActivityForResult(intent, ACC_PROFILE_EDITOR_REQUEST)
                        }
                    }
                    1 -> {
                        // Rename the selected profile (2nd option).
                        MaterialDialog(this@MainActivity)
                            .show {
                                title(R.string.profile_name)
                                message(R.string.dialog_profile_name_message)
                                input(prefill = profile.profileName) { _, charSequence ->
                                    //TODO: Check if the profile name is valid
//                                    val profileNameRegex = """^[^\\/:*?"<>|]+${'$'}""".toRegex()
//                                    val isValid = !profileNameRegex.matches(charSequence)

                                    // Set profile name
                                    profile.profileName = charSequence.toString()

                                    // Update the profile in the DB
                                    mMainActivityViewModel.updateProfile(profile)
                                }
                                positiveButton(R.string.save)
                                negativeButton(android.R.string.cancel)
                            }
                    }
                    2 -> {
                        // Delete the selected profile (3rd option).
                        mMainActivityViewModel.deleteProfile(profile)
                    }
                }
            }
        }
    }

    private fun initUi() {
        // Assign ViewModel
        mViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        mMainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        // Set Bottom Navigation Bar Item Selected Listener
        botNav_main.setOnNavigationItemSelectedListener(this)

        // Load in dashboard fragment
        botNav_main.selectedItemId = mMainActivityViewModel.selectedNavBarItem

        //Rest of the UI

        // TODO: Integrate schedules into another fragment
//        val schedules = ArrayList(AccUtils.listAllSchedules())
//        if(schedules.isEmpty()) {
//            no_schedules_jobs_textview.visibility = View.VISIBLE
//            scheduled_jobs_recyclerview.visibility = View.GONE
//        }

        // TODO: Move the recyclerview stuff into the respective ViewModels
//        scheduleAdapter = ScheduleRecyclerViewAdapter(schedules) { schedule, delete ->
//            if(delete) {
//                deleteSchedule(schedule)
//            } else {
//                MaterialDialog(this).show {
//                    titleTv(R.string.schedule_job)
//                    message(R.string.edit_scheduled_command)
//                    input(prefill = schedule.command, inputType = TYPE_TEXT_FLAG_NO_SUGGESTIONS, allowEmpty = false) { _, charSequence ->
//                        schedule.command =  charSequence.toString()
//                        AccUtils.schedule(schedule.executeOnce, schedule.hour, schedule.minute, charSequence.toString())
//                    }
//                    positiveButton(R.string.save)
//                    negativeButton(android.R.string.cancel)
//                    neutralButton(R.string.delete) {
//                        deleteSchedule(schedule)
//                    }
//                }
//            }
//        }


//        val layoutManager = LinearLayoutManager(this)
//        scheduled_jobs_recyclerview.layoutManager = layoutManager
//        scheduled_jobs_recyclerview.adapter = scheduleAdapter

        // TODO: Move schedule onClicks to the new Schedule fragment
//        create_schedule.setOnClickListener {
//            val dialog = MaterialDialog(this@MainActivity).show {
//                customView(R.layout.schedule_dialog)
//                positiveButton(R.string.save) { dialog ->
//                    val view = dialog.getCustomView()
//                    val spinner = view.findViewById<Spinner>(R.id.profile_selector)
//                    val executeOnceCheckBox = view.findViewById<CheckBox>(R.id.schedule_recurrency)
//                    val timePicker = view.findViewById<TimePicker>(R.id.time_picker)
//                    val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.hour else timePicker.currentHour
//                    val minute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.minute else timePicker.currentMinute
//
//                    if(spinner.selectedItemId == 0.toLong()) {
//                        Intent(this@MainActivity, AccConfigEditorActivity::class.java).also { intent ->
//                            val dataBundle = Bundle()
//                            dataBundle.putInt("hour", hour)
//                            dataBundle.putInt("minute", minute)
//                            dataBundle.putBoolean("executeOnce", executeOnceCheckBox.isChecked)
//
//                            intent.putExtra("data", dataBundle)
//                            intent.putExtra("titleTv", this@MainActivity.getString(R.string.schedule_creator))
//                            startActivityForResult(intent, ACC_PROFILE_SCHEDULER_REQUEST)
//                        }
//                    } else {
//                        val profile = spinner.selectedItem as String
//                        val configProfile = ProfileUtils.readProfile(profile, this@MainActivity, gson)
//
//                        addSchedule(Schedule("$hour$minute", executeOnceCheckBox.isChecked, hour, minute, configProfile.getCommands().joinToString(separator = "; ")))
//
//                        AccUtils.schedule(
//                            executeOnceCheckBox.isChecked,
//                            hour,
//                            minute,
//                            configProfile.getCommands()
//                        )
//                    }
//                }
//                negativeButton(android.R.string.cancel)
//            }

//            val profiles = ArrayList<String>()
//            profiles.add(getString(R.string.new_config))
//            profiles.addAll(ProfileUtils.listProfiles(this, gson))
//            val view = dialog.getCustomView()
//            val spinner = view.findViewById<Spinner>(R.id.profile_selector)
//            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, profiles)
//
//            view.findViewById<TimePicker>(R.id.time_picker).setIs24HourView(true)
//
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner.adapter = adapter;
//        }
    }

    private fun checkAccInstalled(): Boolean {
        if(!Acc.isBundledAccInstalled(filesDir) || !Acc.isBundledAccUpdated()) { //TODO let the user decide between bundle and stable
            val dialog = MaterialDialog(this).show {
                title(R.string.installing_acc)
                progress(R.string.wait)
                cancelOnTouchOutside(false)
            }

            dialog.setOnKeyListener { _, keyCode, _ ->
                keyCode == KeyEvent.KEYCODE_BACK
            }

            doAsync {
                val res = Acc.installBundledAccModule(this@MainActivity)
                uiThread {
                    dialog.cancel()

                    if (res?.isSuccess != true) {
                        //TODO add an option to share logs
                        val failureDialog = MaterialDialog(this@MainActivity)
                            .show {
                                title(R.string.installation_failed_title)
                                message(R.string.installation_failed)
                                positiveButton(R.string.retry) {
                                    if (checkAccInstalled())
                                        initUi()
                                }
                                negativeButton {
                                    finish()
                                }
                                cancelOnTouchOutside(false)
                            }

                        failureDialog.setOnKeyListener { _, keyCode, _ ->
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                dialog.dismiss()
                                finish()
                                false
                            } else true
                        }
                    } else {
                        initUi()
                    }
                }

                res?.let {
                    Log.d(LOG_TAG, it.out.joinToString("\n"))
                }
            }
            return false
        }

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val appUpdater = AppUpdater(this)
            .setDisplay(Display.NOTIFICATION)
            .setUpdateFrom(UpdateFrom.GITHUB)
            .setGitHubUserAndRepo("MatteCarra", "AccA")
            .setIcon(R.drawable.ic_notification)
        appUpdater.start()

        if (!Shell.rootAccess()) {
            val dialog = MaterialDialog(this).show {
                title(R.string.tile_acc_no_root)
                message(R.string.no_root_message)
                positiveButton(android.R.string.ok) {
                    finish()
                }
                cancelOnTouchOutside(false)
            }

            dialog.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss()
                    finish()
                    false
                } else true
            }
            return
        }

        if (checkAccInstalled()) {
            initUi()
        }
    }

    override fun onBackPressed() {
        if (botNav_main.selectedItemId == R.id.botNav_home) {
            super.onBackPressed()
        } else {
            botNav_main.selectedItemId = R.id.botNav_home
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACC_CONFIG_EDITOR_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data?.getBooleanExtra(Constants.ACC_HAS_CHANGES, false) == true) {
                    doAsync {
                        val result =
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
                    val accConfig: AccConfig = data.getParcelableExtra(Constants.ACC_CONFIG_KEY)
                    // Extract the data
                    val editorData = data.getBundleExtra(Constants.DATA_KEY)
                    val profileId = editorData.getInt(Constants.PROFILE_ID_KEY)
                    doAsync {
                        val selectedProfile: AccaProfile =
                            mMainActivityViewModel.getProfileById(profileId)

                        runOnUiThread {
                            // Update the selected Profile
                            selectedProfile.accConfig = accConfig

                            // Update the profile
                            mMainActivityViewModel.updateProfile(selectedProfile)
                        }
                    }
                }
            }
        }
//        }
//        } else if(requestCode == ACC_PROFILE_SCHEDULER_REQUEST && resultCode == Activity.RESULT_OK) {
//            if(data?.hasExtra("data") == true) {
//                val dataBundle = data.getBundleExtra("data")
//
//                val hour = dataBundle.getInt("hour")
//                val minute = dataBundle.getInt("minute")
//                val executeOnce = dataBundle.getBoolean("executeOnce")
//                val commands = data.getParcelableExtra<AccConfig>("mAccConfig").getCommands()
//
//                addSchedule(Schedule("${String.format("%02d", hour)}${String.format("%02d", minute)}", executeOnce, hour, minute, commands.joinToString(separator = "; ")))
//
//                AccUtils.schedule(
//                    executeOnce,
//                    hour,
//                    minute,
//                    commands
//                )
//            }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the main_activity_menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.actions_logs -> {
                startActivity(Intent(this, LogViewerActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }

//    override fun onResume() {
//        handler.post(updateUIRunnable) // Start the initial runnable task by posting through the handler
//
//        super.onResume()
//    }
//
//    override fun onPause() {
//        handler.removeCallbacks(updateUIRunnable)
//
//        super.onPause()
//    }
}
