package mattecarra.accapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.gson.Gson
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.content_main.*
import mattecarra.accapp.utils.AccUtils
import mattecarra.accapp.R
import mattecarra.accapp.adapters.Profile
import mattecarra.accapp.adapters.ProfilesViewAdapter
import mattecarra.accapp.adapters.Schedule
import mattecarra.accapp.adapters.ScheduleRecyclerViewAdapter
import mattecarra.accapp.data.AccConfig
import mattecarra.accapp.utils.ProfileUtils
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

class MainActivity : AppCompatActivity() {
    private val LOG_TAG = "MainActivity"
    private val PERMISSION_REQUEST: Int = 0
    private val ACC_CONFIG_EDITOR_REQUEST: Int = 1
    private val ACC_PROFILE_CREATOR_REQUEST: Int = 2
    private val ACC_PROFILE_EDITOR_REQUEST: Int = 3
    private val ACC_PROFILE_SCHEDULER_REQUEST: Int = 4

    private lateinit var config: AccConfig
    private lateinit var sharedPrefs: SharedPreferences
    private val gson: Gson = Gson()

    private var profilesAdapter: ProfilesViewAdapter? = null

    private lateinit var scheduleAdapter: ScheduleRecyclerViewAdapter
    val addSchedule: (Schedule) -> Unit = { schedule ->
        if(scheduleAdapter.itemCount == 0) {
            no_schedules_jobs_textview.visibility = View.GONE
            scheduled_jobs_recyclerview.visibility = View.VISIBLE
        }
        scheduleAdapter.add(schedule)
    }
    val deleteSchedule: (Schedule) -> Unit = { schedule ->
        AccUtils.deleteSchedule(schedule.executeOnce, schedule.name)
        scheduleAdapter.remove(schedule)

        if(scheduleAdapter.itemCount == 0) {
            no_schedules_jobs_textview.visibility = View.VISIBLE
            scheduled_jobs_recyclerview.visibility = View.GONE
        }
    }

    //Used to update battery info every second
    private val handler = Handler()
    private val updateUIRunnable = object : Runnable {
        override fun run() {
            val r = this //need this to make it recursive
            doAsync {
                val batteryInfo = AccUtils.getBatteryInfo()
                val isDeamonRunning = AccUtils.isAccdRunning()
                uiThread {
                    deamon_start_stop_label.text = getString(if(isDeamonRunning) R.string.acc_deamon_status_running else R.string.acc_deamon_status_not_running)
                    deamon_start_stop.text = getString(if(isDeamonRunning) R.string.stop else R.string.start)

                    status.text = batteryInfo.status
                    battery_info.text = getString(R.string.battery_info, batteryInfo.health, batteryInfo.temp, batteryInfo.current / 1000, batteryInfo.voltage)

                    handler.postDelayed(r, 1000)// Repeat the same runnable code block again after 1 seconds
                }
            }
        }
    }

    private fun showConfigReadError() {
        MaterialDialog(this).show {
            title(R.string.config_error_title)
            message(R.string.config_error_dialog)
            positiveButton(android.R.string.ok)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initUi()
                } else {
                    finish()
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun initProfiles() {
        val profileList = ProfileUtils.listProfiles(this, gson)

        val currentProfile = sharedPrefs.getString("PROFILE", null)

        val layoutManager = GridLayoutManager(this, 3)
        profilesAdapter = ProfilesViewAdapter(ArrayList(profileList.map { Profile(it) }), currentProfile) { profile, longPress ->
            if(longPress) {
                MaterialDialog(this@MainActivity).show {
                    listItems(R.array.profile_long_press_options) { _, index, _ ->
                        when(index) {
                            0 -> {
                                Intent(this@MainActivity, AccConfigEditorActivity::class.java).also { intent ->
                                    val dataBundle = Bundle()
                                    dataBundle.putString("profileName", profile.profileName)

                                    intent.putExtra("config", ProfileUtils.readProfile(profile.profileName, this@MainActivity, gson))
                                    intent.putExtra("data", dataBundle)
                                    startActivityForResult(intent, ACC_PROFILE_EDITOR_REQUEST)
                                }
                            }
                            1 -> {
                                MaterialDialog(this@MainActivity)
                                    .show {
                                        title(R.string.profile_name)
                                        message(R.string.dialog_profile_name_message)
                                        input(prefill = profile.profileName) { _, charSequence ->
                                            //profiles index
                                            val profileList: MutableList<String> = ProfileUtils.listProfiles(this@MainActivity, gson).toMutableList()

                                            if(profileList.contains(charSequence.toString())) {
                                                //TODO input not valid
                                                return@input
                                            }

                                            profileList.add(charSequence.toString())
                                            profileList.remove(profile.profileName) //remove all profile name
                                            ProfileUtils.writeProfiles(this@MainActivity, profileList, gson) //Update profiles file with new profile

                                            //Saving profile
                                            val f = File(context.filesDir, "${profile.profileName}.profile")
                                            f.renameTo(File(context.filesDir, "$charSequence.profile"))

                                            profile.profileName = charSequence.toString()
                                            profilesAdapter?.notifyItemChanged(profile)
                                        }
                                        positiveButton(R.string.save)
                                        negativeButton(android.R.string.cancel)
                                    }
                            }
                            2 -> {
                                val f = File(context.filesDir, "$profile.profile")
                                f.delete()

                                ProfileUtils.writeProfiles(
                                    this@MainActivity,
                                    ProfileUtils
                                        .listProfiles(this@MainActivity, gson).filter { it != profile.profileName },
                                    gson
                                ) //update profile list without this element

                                profilesAdapter?.remove(profile)
                                if(profilesAdapter?.itemCount == 0) {
                                    this@MainActivity.profiles_recyclerview.visibility = android.view.View.GONE
                                    this@MainActivity.no_profiles_textview.visibility = android.view.View.VISIBLE
                                }
                            }
                        }
                    }
                }
            } else {
                //apply profile
                val profileConfig = ProfileUtils.readProfile(profile.profileName, this@MainActivity, gson)

                doAsync {
                    val res = profileConfig.updateAcc()

                    ProfileUtils.saveCurrentProfile(profile.profileName, sharedPrefs)

                    if(!res.voltControlUpdateSuccessful) {
                        uiThread {
                            Toast.makeText(this@MainActivity, R.string.wrong_volt_file, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                profilesAdapter?.selectedProfile = profile.profileName
                profilesAdapter?.notifyDataSetChanged()
            }
        }

        profiles_recyclerview.layoutManager = layoutManager
        profiles_recyclerview.adapter = profilesAdapter

        if(profileList.isNotEmpty()) {
            profiles_recyclerview.visibility = android.view.View.VISIBLE
            no_profiles_textview.visibility = android.view.View.GONE
        } else {
            profiles_recyclerview.visibility = android.view.View.GONE
            no_profiles_textview.visibility = android.view.View.VISIBLE
        }
    }

    private fun initUi() {
        try {
            this.config = AccUtils.readConfig()
        } catch (ex: Exception) {
            ex.printStackTrace()
            showConfigReadError()
            this.config = AccUtils.defaultConfig //if config is null I use default config values.
        }

        //profiles
        initProfiles()

        //Rest of the UI
        edit_config.setOnClickListener {
            Intent(this@MainActivity, AccConfigEditorActivity::class.java).also { intent ->
                startActivityForResult(intent, ACC_CONFIG_EDITOR_REQUEST)
            }
        }

        edit_charging_switch.setOnClickListener {
            val chargingSwitches = AccUtils.listChargingSwitches()

            var currentSwitch = AccUtils.getCurrentChargingSwitch()

            MaterialDialog(this@MainActivity).show {
                noAutoDismiss()
                title(R.string.edit_charging_switch)
                listItemsSingleChoice(items = chargingSwitches, initialSelection = chargingSwitches.indexOf(currentSwitch), waitForPositiveButton = false)  { _, _, text ->
                    currentSwitch = text
                    setActionButtonEnabled(WhichButton.POSITIVE, true)
                    setActionButtonEnabled(WhichButton.NEUTRAL, true)
                }
                positiveButton(R.string.save) {
                    currentSwitch?.let {
                        AccUtils.setChargingSwitch(it)
                    }
                    dismiss()
                }
                neutralButton(R.string.test_switch) {
                    val description = currentSwitch?.let {
                        val res = AccUtils.testChargingSwitch(it)
                        when(res) {
                            0 -> R.string.charging_switch_works
                            1 -> R.string.charging_switch_does_not_work
                            2 -> R.string.plug_battery_to_test
                            else -> null
                        }
                    } ?: R.string.no_option_selected //should never happen

                    MaterialDialog(this@MainActivity).show {
                        title(R.string.test_switch)
                        message(description)
                        positiveButton(android.R.string.ok)
                    }
                }
                negativeButton(android.R.string.cancel) {
                    dismiss()
                }

                if(currentSwitch == null)
                    setActionButtonEnabled(WhichButton.NEUTRAL, false)
            }
        }

        create_acc_profile.setOnClickListener {
            Intent(this@MainActivity, AccConfigEditorActivity::class.java).also { intent ->
                startActivityForResult(intent, ACC_PROFILE_CREATOR_REQUEST)
            }
        }

        deamon_start_stop.setOnClickListener {
            Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show()

            doAsync {
                if(AccUtils.isAccdRunning())
                    AccUtils.accStopDeamon()
                else
                    AccUtils.accStartDeamon()
            }
        }

        deamon_restart.setOnClickListener {
            Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show()

            doAsync {
                AccUtils.accRestartDeamon()
            }
        }

        reset_stats_on_unplugged_switch.setOnCheckedChangeListener { _, isChecked ->
            config.resetUnplugged = isChecked
            AccUtils.updateResetUnplugged(isChecked)

            //If I manually modify the config I have to set current profile to null (custom profile)
            ProfileUtils.saveCurrentProfile(null, sharedPrefs)
        }

        reset_stats_on_unplugged_switch.isChecked = config.resetUnplugged
        reset_battery_stats.setOnClickListener {
            AccUtils.resetBatteryStats()
        }


        val schedules = ArrayList(AccUtils.listAllSchedules())
        if(schedules.isEmpty()) {
            no_schedules_jobs_textview.visibility = View.VISIBLE
            scheduled_jobs_recyclerview.visibility = View.GONE
        }

        scheduleAdapter = ScheduleRecyclerViewAdapter(schedules) { schedule, delete ->
            if(delete) {
                deleteSchedule(schedule)
            } else {
                MaterialDialog(this).show {
                    title(R.string.schedule_job)
                    message(R.string.edit_scheduled_command)
                    input(prefill = schedule.command, inputType = TYPE_TEXT_FLAG_NO_SUGGESTIONS, allowEmpty = false) { _, charSequence ->
                        schedule.command =  charSequence.toString()
                        AccUtils.schedule(schedule.executeOnce, schedule.hour, schedule.minute, charSequence.toString())
                    }
                    positiveButton(R.string.save)
                    negativeButton(android.R.string.cancel)
                    neutralButton(R.string.delete) {
                        deleteSchedule(schedule)
                    }
                }
            }
        }


        val layoutManager = LinearLayoutManager(this)
        scheduled_jobs_recyclerview.layoutManager = layoutManager
        scheduled_jobs_recyclerview.adapter = scheduleAdapter

        create_schedule.setOnClickListener {
            val dialog = MaterialDialog(this@MainActivity).show {
                customView(R.layout.schedule_dialog)
                positiveButton(R.string.save) { dialog ->
                    val view = dialog.getCustomView()
                    val spinner = view.findViewById<Spinner>(R.id.profile_selector)
                    val executeOnceCheckBox = view.findViewById<CheckBox>(R.id.schedule_recurrency)
                    val timePicker = view.findViewById<TimePicker>(R.id.time_picker)
                    val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.hour else timePicker.currentHour
                    val minute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.minute else timePicker.currentMinute

                    if(spinner.selectedItemId == 0.toLong()) {
                        Intent(this@MainActivity, AccConfigEditorActivity::class.java).also { intent ->
                            val dataBundle = Bundle()
                            dataBundle.putInt("hour", hour)
                            dataBundle.putInt("minute", minute)
                            dataBundle.putBoolean("executeOnce", executeOnceCheckBox.isChecked)

                            intent.putExtra("data", dataBundle)
                            startActivityForResult(intent, ACC_PROFILE_SCHEDULER_REQUEST)
                        }
                    } else {
                        val profile = spinner.selectedItem as String
                        val configProfile = ProfileUtils.readProfile(profile, this@MainActivity, gson)

                        addSchedule(Schedule("$hour$minute", executeOnceCheckBox.isChecked, hour, minute, configProfile.getCommands().joinToString(separator = "; ")))

                        AccUtils.schedule(
                            executeOnceCheckBox.isChecked,
                            hour,
                            minute,
                            configProfile.getCommands()
                        )
                    }
                }
                negativeButton(android.R.string.cancel)
            }

            val profiles = ArrayList<String>()
            profiles.add(getString(R.string.new_config))
            profiles.addAll(ProfileUtils.listProfiles(this, gson))
            val view = dialog.getCustomView()
            val spinner = view.findViewById<Spinner>(R.id.profile_selector)
            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, profiles)

            view.findViewById<TimePicker>(R.id.time_picker).setIs24HourView(true)

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.adapter = adapter;
        }
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

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)


        if(!Shell.rootAccess()) {
            val dialog = MaterialDialog(this).show {
                title(R.string.tile_acc_no_root)
                message(R.string.no_root_message)
                positiveButton(android.R.string.ok) {
                    finish()
                }
                cancelOnTouchOutside(false)
            }

            dialog.setOnKeyListener { _, keyCode, _ ->
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss()
                    finish()
                    false
                } else true
            }
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST)
            return
        }

        initUi()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACC_CONFIG_EDITOR_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if(data?.getBooleanExtra("hasChanges", false) == true) {
                    config = data.getParcelableExtra("config")
                    doAsync {
                        val res = config.updateAcc()

                        //If I manually modify the config I have to set current profile to null (custom profile)
                        ProfileUtils.saveCurrentProfile(null, sharedPrefs)
                        profilesAdapter?.let { adapter ->
                            uiThread {
                                if(!res.voltControlUpdateSuccessful) {
                                    Toast.makeText(this@MainActivity, R.string.wrong_volt_file, Toast.LENGTH_LONG).show()
                                }

                                adapter.selectedProfile = null
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        } else if(requestCode == ACC_PROFILE_CREATOR_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if(data != null) {
                    val config: AccConfig = data.getParcelableExtra("config")
                    val fileNameRegex = """^[^\\/:*?"<>|]+${'$'}""".toRegex()
                    MaterialDialog(this)
                        .show {
                            title(R.string.profile_name)
                            message(R.string.dialog_profile_name_message)
                            input(waitForPositiveButton = false) { dialog, charSequence ->
                                val inputField = dialog.getInputField()
                                val isValid = fileNameRegex.matches(charSequence)

                                inputField.error = if (isValid) null else getString(R.string.invalid_chars)
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                            }
                            positiveButton(R.string.save) { dialog ->
                                val input = dialog.getInputField().text.toString()

                                //profiles index
                                val profileList = ProfileUtils.listProfiles(this@MainActivity, gson).toMutableList()

                                if(!profileList.contains(input)) {
                                    profileList.add(input)
                                    ProfileUtils.writeProfiles(this@MainActivity, profileList, gson) //Update profiles file with new profile
                                }

                                //Saving profile
                                val f = File(context.filesDir, "$input.profile")
                                val json = gson.toJson(config)
                                f.writeText(json)

                                if(profilesAdapter?.itemCount == 0) {
                                    this@MainActivity.profiles_recyclerview.visibility = android.view.View.VISIBLE
                                    this@MainActivity.no_profiles_textview.visibility = android.view.View.GONE
                                }

                                profilesAdapter?.add(Profile(input))

                            }
                            negativeButton(android.R.string.cancel)
                        }
                }
            }
        } else if(requestCode == ACC_PROFILE_EDITOR_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if(data?.getBooleanExtra("hasChanges", false) == true && data.hasExtra("data")) {
                    val config: AccConfig = data.getParcelableExtra("config")
                    val profileName = data.getBundleExtra("data").getString("profileName")

                    //Saving profile
                    val f = File(this@MainActivity.filesDir, "$profileName.profile")
                    val json = gson.toJson(config)
                    f.writeText(json)
                }
            }
        } else if(requestCode == ACC_PROFILE_SCHEDULER_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data?.hasExtra("data") == true) {
                val dataBundle = data.getBundleExtra("data")

                val hour = dataBundle.getInt("hour")
                val minute = dataBundle.getInt("minute")
                val executeOnce = dataBundle.getBoolean("executeOnce")
                val commands = data.getParcelableExtra<AccConfig>("config").getCommands()

                addSchedule(Schedule("${String.format("%02d", hour)}${String.format("%02d", minute)}", executeOnce, hour, minute, commands.joinToString(separator = "; ")))

                AccUtils.schedule(
                    executeOnce,
                    hour,
                    minute,
                    commands
                )
            }
        }
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
        when(item.itemId) {
            R.id.actions_logs -> {
                startActivity(Intent(this, LogViewerActivity::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        handler.post(updateUIRunnable) // Start the initial runnable task by posting through the handler

        super.onResume()
    }

    override fun onPause() {
        handler.removeCallbacks(updateUIRunnable)

        super.onPause()
    }
}