package mattecarra.accapp.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.ItemListener
import com.afollestad.materialdialogs.list.listItems
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.databinding.WidgetBatteryDialogBinding
import mattecarra.accapp.models.scolorview.ColorPickerView.ColorObserver
import mattecarra.accapp.viewmodel.ProfilesViewModel
import mattecarra.accapp.viewmodel.SharedViewModel
import xml.*
import com.topjohnwu.superuser.internal.Utils.context
import mattecarra.accapp.Preferences
import mattecarra.accapp.databinding.EditChargingLimitOnceDialogBinding
import java.lang.Exception
import java.util.*

class BatteryDialogActivity : AppCompatActivity()
{
    private var widgetId: Int = -1;
    private var isAccdRunning: Boolean = false
    private var isAccdInstalled: Boolean = false
    private var manualStop: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?)
    {

        //--------------------------------------------------
        // select locale and theme day\night

        val spl = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).getString("language", "def")

        val config = resources.configuration
        val locale = if (spl.equals("def")) Locale.getDefault() else Locale(spl)

        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) config.setLocale(locale) else config.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) createConfigurationContext(config)

        resources.updateConfiguration(config, resources.displayMetrics)

        when (Preferences(this).appTheme)
        {
            "0" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "1" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "2" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        //--------------------------------------------------

        super.onCreate(savedInstanceState)

        widgetId = intent.getIntExtra(WIDGET_ID_NAME, -1)
        manualStop = getSharedPreferences(WIDGET_PREF_NAME, MODE_PRIVATE).getBoolean(WIDGET_ALL_STOP, false)

        val content = WidgetBatteryDialogBinding.inflate(layoutInflater)
        content.wdtSettingWidgetBtn.isVisible = widgetId > -1
        content.wdtSettingWidgetBtn.text = getString(R.string.qwa_setting)+" "+ widgetId.toString()
        content.wdtDisableOptimizationBattery.isGone = isBatteryOptimizationDisabled(this)

        setContentView(content.root)

        //-----------------------------------------

        fun updateTextStatusACC()
        {
            content.wdtDaemonAccBtn.text = if (!isAccdInstalled) getString(R.string.acc_installation_failed_title)
            else if (isAccdRunning) getString(R.string.acc_daemon_status_running)
            else getString(R.string.acc_daemon_status_not_running)
        }

        fun updateTextStartWidget()
        {
            content.wdtStartWidgetBtn.text =
            if (manualStop) getString(R.string.qwa_start) else getString(R.string.qwa_stop)
            //else getString(R.string.acc_daemon_status_not_running)
        }

        GlobalScope.launch {
            isAccdRunning = Acc.instance.isAccdRunning()
            isAccdInstalled = Acc.instance.getAccVersion() != null
            runOnUiThread( Runnable { updateTextStatusACC() } )
        }

        updateTextStartWidget()

        //---------------------------------------------------------------------------------

        content.activityRoot.setOnClickListener { finish() }
        content.btnDismiss.setOnClickListener { finish() }

        //---------------------------------------------------------------------------------
        // android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS

        content.wdtDisableOptimizationBattery.setOnClickListener {
            try
            {
                val intent = Intent()
                intent.action = "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"
                intent.data = Uri.parse("package:" + context.packageName)
                startActivity(intent)
                finish()
            }
            catch (e: Exception)
            {
                Toast.makeText(context, getString(R.string.battery_optimization_fail), Toast.LENGTH_SHORT).show()
            }
        }

        //---------------------------------------------------------------------------------

        content.wdtOpenAccaBtn.setOnClickListener {

            finish()

            ContextCompat.startActivity(context.applicationContext,
                Intent(context.applicationContext, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtras(intent), null)

            //val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            //startActivity(intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        //---------------------------------------------------------------------------------

        content.wdtDaemonAccBtn.setOnClickListener {
            runBlocking {
                if (isAccdRunning) Acc.instance.abcStopDaemon()
                else Acc.instance.abcStartDaemon()
                updateTextStatusACC()
                finish()
            }
        }

        //---------------------------------------------------------------------------------

        content.wdtStartWidgetBtn.setOnClickListener {
            sendBroadcast(Intent(this, BatteryInfoWidget::class.java).setAction(WIDGET_ACTION_REVERSE))
            finish()
        }

        //---------------------------------------------------------------------------------

        content.wdtProfileBtn.setOnClickListener {

            val profilesViewModel = ProfilesViewModel(application)
            val mSharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

            MaterialDialog(this).show {

                runBlocking {

                    title(R.string.title_profiles)
                    val temp = profilesViewModel.getProfiles()

                    if (temp.isEmpty()) message(R.string.no_profiles)
                    else
                    {
                        listItems(items = temp.map { b -> b.profileName },
                        selection = { _, index, _ -> runBlocking {
                            mSharedViewModel.updateAccConfig(temp[index].accConfig)
                            mSharedViewModel.setCurrentSelectedProfile(temp[index].uid)
                            Toast.makeText(this@BatteryDialogActivity, getString(R.string.selecting_profile_toast, temp[index].profileName), Toast.LENGTH_SHORT).show()
                            sendBroadcast(Intent(this@BatteryDialogActivity, BatteryInfoWidget::class.java).setAction(WIDGET_ALL_UPDATE))
                            finish()
                        }})
                    }

                    negativeButton(text = "cancel", click = { dismiss() })
                }
            }
        }

        //---------------------------------------------------------------------------------

        content.wdtChargeOnceBtn.setOnClickListener {

            val dialog = EditChargingLimitOnceDialogBinding.inflate(layoutInflater)

            MaterialDialog(it.context).show {
                title(R.string.edit_charging_limit_once_button)
                message(R.string.edit_charging_limit_once_dialog_msg)
                cancelOnTouchOutside(false)
                customView(view=dialog.root)

                positiveButton(R.string.apply) {
                    runBlocking {
                        val limit = getCustomView().findViewById<NumberPicker>(R.id.charging_limit).value
                        Acc.instance.setChargingLimitForOneCharge(limit)
                        Toast.makeText(context, getString(R.string.done_applied_charge_limit, limit), Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
                negativeButton(android.R.string.cancel) {
                    Toast.makeText(context, R.string.charge_limit_not_applied, Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            val picker = dialog.chargingLimit
            picker.maxValue = 100
            picker.minValue = 20
            picker.value = 100
        }

        //---------------------------------------------------------------------------------

        content.wdtSettingWidgetBtn.setOnClickListener{

            MaterialDialog(this).apply {

                val bind = mattecarra.accapp.databinding.WidgetBatteryPrefBinding.inflate(layoutInflater)
                val sp = context.getSharedPreferences(WIDGET_PREF_NAME, MODE_PRIVATE)
                val swidgetId = widgetId.toString()

                var textColor = sp.getInt(swidgetId + WIDGET_TCOLOR, Color.BLACK)
                var textSize = sp.getInt(swidgetId + WIDGET_TSIZE, 14)
                var roundSize = sp.getInt(swidgetId + WIDGET_BROUND, 8)
                var backgroundColor = sp.getInt(swidgetId + WIDGET_BCOLOR, Color.parseColor("#FFF5F5F5"))

                val gradient = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(backgroundColor, backgroundColor))
                gradient.shape = GradientDrawable.RECTANGLE
                gradient.cornerRadius = roundSize.toFloat()

                bind.sampleWdt.batteryInfoLl.isVisible = true
                bind.sampleWdt.batteryIcon.isVisible = false

                title(text = getString(R.string.swd_title))
                customView(view = bind.root, scrollable = true)

                negativeButton(text = "cancel", click = { dismiss() })
                positiveButton(text = "ok", click = {

                    sp.edit()
                    .putInt(swidgetId + WIDGET_TCOLOR, textColor)
                    .putInt(swidgetId + WIDGET_TSIZE, textSize)
                    .putInt(swidgetId + WIDGET_BROUND, roundSize)
                    .putInt(swidgetId + WIDGET_BCOLOR, backgroundColor)

                    .putBoolean(swidgetId + WIDGET_SLABELS, bind.showLabelChk.isChecked)
                    .putBoolean(swidgetId + WIDGET_SSYMBOL, bind.replaceLabelChk.isChecked)
                    .putBoolean(swidgetId + WIDGET_SVALUE, bind.showEndvalueChk.isChecked)
                    .putBoolean(swidgetId + WIDGET_SSTATUS, bind.showStatusChk.isChecked)
                    .putBoolean(swidgetId + WIDGET_SCURRENT, bind.showCurrentChk.isChecked)
                    .putBoolean(swidgetId + WIDGET_STEMP, bind.showTemperatureChk.isChecked)
                    .putBoolean(swidgetId + WIDGET_SVOLT, bind.showVoltageChk.isChecked)
                    .putBoolean(swidgetId + WIDGET_SPROFILE, bind.showProfileChk.isChecked)
                    .apply()

                    sendBroadcast(Intent(context, BatteryInfoWidget::class.java).setAction(WIDGET_ALL_UPDATE))
                    finish()
                })

                //-----------------------------------------------------------

                bind.showStatusChk.setOnCheckedChangeListener { _, isChecked -> bind.sampleWdt.statusLine.isVisible = isChecked }
                bind.showCurrentChk.setOnCheckedChangeListener { _, isChecked -> bind.sampleWdt.chargingLine.isVisible = isChecked }
                bind.showTemperatureChk.setOnCheckedChangeListener { _, isChecked -> bind.sampleWdt.temperLine.isVisible = isChecked }
                bind.showVoltageChk.setOnCheckedChangeListener { _, isChecked -> bind.sampleWdt.voltageLine.isVisible = isChecked }
                bind.showProfileChk.setOnCheckedChangeListener { _, isChecked -> bind.sampleWdt.profileLine.isVisible = isChecked }

                bind.showLabelChk.setOnCheckedChangeListener { _, isChecked ->
                    bind.replaceLabelChk.isEnabled = isChecked
                    bind.sampleWdt.statusLabel.isVisible = isChecked
                    bind.sampleWdt.chargingLabel.isVisible = isChecked
                    bind.sampleWdt.temperLabel.isVisible = isChecked
                    bind.sampleWdt.voltageLabel.isVisible = isChecked
                    bind.sampleWdt.profileLabel.isVisible = isChecked
                }

                bind.replaceLabelChk.setOnCheckedChangeListener { _, isChecked ->
                    bind.sampleWdt.statusLabel.text = if (isChecked) "Ⓢ:" else getString(R.string.info_status)
                    bind.sampleWdt.chargingLabel.text= if (isChecked) "Ⓒ:" else getString(R.string.info_charging_speed)
                    bind.sampleWdt.temperLabel.text= if (isChecked) "Ⓣ:" else getString(R.string.info_temperature)
                    bind.sampleWdt.voltageLabel.text = if (isChecked) "Ⓥ:" else getString(R.string.info_voltage)
                    bind.sampleWdt.profileLabel.text= if (isChecked) "Ⓟ:" else getString(R.string.info_profile)
                }

                //----------------------------------------------------

                bind.textSizeSeek.setOnSeekBarChangeListener( object : OnSeekBarChangeListener
                {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
                    {
                        textSize = progress
                        bind.textSizeView.text = textSize.toString()
                        bind.sampleWdt.statusLabel.textSize = textSize.toFloat()
                        bind.sampleWdt.statusOut.textSize = textSize.toFloat()
                        bind.sampleWdt.chargingLabel.textSize = textSize.toFloat()
                        bind.sampleWdt.chargingOut.textSize = textSize.toFloat()
                        bind.sampleWdt.temperLabel.textSize = textSize.toFloat()
                        bind.sampleWdt.temperOut.textSize = textSize.toFloat()
                        bind.sampleWdt.voltageLabel.textSize = textSize.toFloat()
                        bind.sampleWdt.voltageOut.textSize = textSize.toFloat()
                        bind.sampleWdt.profileLabel.textSize = textSize.toFloat()
                        bind.sampleWdt.profileOut.textSize = textSize.toFloat()

                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                //----------------------------------------------------

                bind.roundSizeSeek.setOnSeekBarChangeListener( object : OnSeekBarChangeListener
                {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
                    {
                        roundSize = progress
                        bind.roundSizeView.text = roundSize.toString()
                        gradient.cornerRadius = progress.toFloat()
                        bind.sampleWdt.imbkgfck.setImageBitmap(gradient.toBitmap(100,100))
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                //----------------------------------------------------

                bind.textMainBtn.setOnClickListener {
                    bind.settingsText.isVisible = false
                    bind.settingsBackground.isVisible = false
                    bind.settingsCheck.isVisible = true
                }

                bind.textColorBtn.setOnClickListener {
                    bind.settingsCheck.isVisible = false
                    bind.settingsBackground.isVisible = false
                    bind.settingsText.isVisible = true
                }

                bind.backgroundColorBtn.setOnClickListener {
                    bind.settingsCheck.isVisible = false
                    bind.settingsText.isVisible = false
                    bind.settingsBackground.isVisible = true
                }

                //----------------------------------------------------

                bind.colorPickerText.observer = object : ColorObserver
                {
                    override fun onColor(color: Int, fromUser: Boolean)
                    {
                        textColor = color
                        bind.sampleWdt.statusLabel.setTextColor(color)
                        bind.sampleWdt.statusOut.setTextColor(color)
                        bind.sampleWdt.chargingLabel.setTextColor(color)
                        bind.sampleWdt.chargingOut.setTextColor(color)
                        bind.sampleWdt.temperLabel.setTextColor(color)
                        bind.sampleWdt.temperOut.setTextColor(color)
                        bind.sampleWdt.voltageLabel.setTextColor(color)
                        bind.sampleWdt.voltageOut.setTextColor(color)
                        bind.sampleWdt.profileLabel.setTextColor(color)
                        bind.sampleWdt.profileOut.setTextColor(color)
                    }
                }

                bind.colorPickerBackground.observer = object : ColorObserver
                {
                    override fun onColor(color: Int, fromUser: Boolean)
                    {
                        backgroundColor = color
                        gradient.colors = intArrayOf(color, color)
                        bind.sampleWdt.imbkgfck.setImageBitmap(gradient.toBitmap(100,100))
                    }
                }

                //----------------------------------------------------

                bind.colorPickerText.color = textColor
                bind.colorPickerBackground.color = backgroundColor

                bind.textSizeSeek.progress = textSize
                bind.textSizeView.text = textSize.toString()

                bind.roundSizeSeek.progress = roundSize
                bind.roundSizeView.text = roundSize.toString()

                bind.sampleWdt.imbkgfck.setImageBitmap(gradient.toBitmap(100,100))

                bind.showLabelChk.isChecked = sp.getBoolean(swidgetId + WIDGET_SLABELS, true)
                bind.replaceLabelChk.isChecked = sp.getBoolean(swidgetId + WIDGET_SSYMBOL, false)
                bind.showEndvalueChk.isChecked = sp.getBoolean(swidgetId + WIDGET_SVALUE, true)
                bind.showStatusChk.isChecked = sp.getBoolean(swidgetId + WIDGET_SSTATUS, true)
                bind.showCurrentChk.isChecked = sp.getBoolean(swidgetId + WIDGET_SCURRENT, true)
                bind.showTemperatureChk.isChecked = sp.getBoolean(swidgetId + WIDGET_STEMP, true)
                bind.showVoltageChk.isChecked = sp.getBoolean(swidgetId + WIDGET_SVOLT, true)
                bind.showProfileChk.isChecked = sp.getBoolean(swidgetId + WIDGET_SPROFILE, true)

            }.show()

        }

        //---------------------------------------------------------------------------------
    }

    fun isBatteryOptimizationDisabled(context: Context): Boolean
    {
        return Build.VERSION.SDK_INT < 23 || (context.getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(context.packageName)
    }

}
