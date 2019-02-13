package mattecarra.accapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.content_main.*
import mattecarra.accapp.AccUtils
import mattecarra.accapp.R
import mattecarra.accapp.data.AccConfig
import mattecarra.accapp.data.Capacity
import mattecarra.accapp.data.Cooldown
import mattecarra.accapp.data.Temp
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity() {
    private val LOG_TAG = "MainActivity"
    private val PERMISSION_REQUEST: Int = 0
    private val ACC_CONFIG_EDITOR_REQUEST: Int = 1
    private lateinit var config: AccConfig

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
                    battery_info.text = getString(R.string.battery_info, batteryInfo.health, batteryInfo.temp, batteryInfo.current / 1000, batteryInfo.voltage / 1000000f)

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

    private fun initUi() {
        try {
            this.config = AccUtils.readConfig()
        } catch (ex: Exception) {
            ex.printStackTrace()
            showConfigReadError()
            this.config = AccUtils.defaultConfig //if config is null I use default config values.
        }

        edit_config.setOnClickListener {
            Intent(this@MainActivity, AccConfigEditorActivity::class.java).also { intent ->
                startActivityForResult(intent, ACC_CONFIG_EDITOR_REQUEST)
            }
        }

        deamon_start_stop.setOnClickListener {
            Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show()

            if(AccUtils.isAccdRunning())
                AccUtils.accStopDeamon()
            else
                AccUtils.accStartDeamon()
        }

        reset_stats_on_unplugged_switch.setOnCheckedChangeListener { _, isChecked ->
            config.resetUnplugged = isChecked
            AccUtils.updateResetUnplugged(isChecked)
        }
        reset_stats_on_unplugged_switch.isChecked = config.resetUnplugged
        reset_battery_stats.setOnClickListener {
            AccUtils.resetBatteryStats()
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
            //.setIcon(R.drawable.app_icon)
        appUpdater.start()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST)
            return
        }

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

        initUi()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACC_CONFIG_EDITOR_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(LOG_TAG, "onActivityResult ACC_CONFIG_EDITOR_REQUEST result ok")
                if(data?.getBooleanExtra("hasChanges", false) == true) {
                    Log.d(LOG_TAG, "Unparcelling config")
                    config = data.getParcelableExtra("config")
                    doAsync {
                        Log.d(LOG_TAG, "Saving config")
                        config.updateAcc()
                        Log.d(LOG_TAG, "Saved")
                    }
                }
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