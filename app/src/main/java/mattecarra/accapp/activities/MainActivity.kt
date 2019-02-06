package mattecarra.accapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.widget.CompoundButton
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
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


class MainActivity : AppCompatActivity(), NumberPicker.OnValueChangeListener, CompoundButton.OnCheckedChangeListener {
    private val PERMISSION_REQUEST: Int = 0

    private lateinit var config: AccConfig

    //Used to update battery info every second
    private val handler = Handler()
    private val updateUIRunnable = object : Runnable {
        override fun run() {
            val r = this //need this to make it recursive
            doAsync {
                val batteryInfo = AccUtils.getBatteryInfo()
                uiThread {
                    status.text = batteryInfo.status
                    battery_info.text = getString(R.string.battery_info, batteryInfo.health, batteryInfo.temp, batteryInfo.current / 1000, batteryInfo.voltage / 1000000f)

                    handler.postDelayed(r, 1000)// Repeat the same runnable code block again after 1 seconds
                }
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
                } else {
                    config.temp.coolDownTemp = 90
                    config.temp.pauseChargingTemp = 95
                }
            }

            R.id.cooldown_switch -> {
                cooldown_percentage_picker.isEnabled = isChecked
                charge_ratio_picker.isEnabled = isChecked
                pause_ratio_picker.isEnabled = isChecked

                if(isChecked) {
                    cooldown_percentage_picker.value = 60
                    config.capacity.coolDownCapacity = 60
                } else {
                    cooldown_percentage_picker.value = 101
                    config.capacity.coolDownCapacity = 101
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

            else -> {}
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
            val isDeamonRunning = AccUtils.isAccdRunning()
            deamon_start_stop_label.text = getString(if(isDeamonRunning) R.string.acc_deamon_status_running else R.string.acc_deamon_status_not_running)
            deamon_start_stop.text = getString(if(isDeamonRunning) R.string.stop else R.string.start)

            handler.post(updateUIRunnable) // Start the initial runnable task by posting through the handler

            reset_stats_on_unplugged_switch.setOnCheckedChangeListener { _, isChecked -> config.resetUnplugged = isChecked }
            reset_battery_stats.setOnClickListener { AccUtils.resetBatteryStats() }

            this.config = AccUtils.readConfig()
        } catch (ex: Exception) {
            ex.printStackTrace()
            showConfigReadError()
            this.config = AccConfig(
                Capacity(5, 60, 70, 80),
                Cooldown(50, 10),
                Temp(40, 45, 90),
                false
            ) //if config is null I use default config values.
        }

        reset_stats_on_unplugged_switch.isChecked = config.resetUnplugged

        shutdown_capacity_picker.minValue = 0
        shutdown_capacity_picker.maxValue = 40
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

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

    override fun onDestroy() {
        handler.removeCallbacks(updateUIRunnable)

        super.onDestroy()
    }
}