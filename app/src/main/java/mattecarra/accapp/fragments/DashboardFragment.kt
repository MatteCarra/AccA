package mattecarra.accapp.fragments

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.android.synthetic.main.dashboard_fragment.*
import kotlinx.android.synthetic.main.dashboard_fragment.view.*
import kotlinx.android.synthetic.main.edit_charging_limit_once_dialog.view.*

import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.SharedViewModel
import mattecarra.accapp.models.BatteryInfo

class DashboardFragment : Fragment() {

    private val LOG_TAG = "DashboardFragment"

    private val PERMISSION_REQUEST: Int = 0
    private val ACC_CONFIG_EDITOR_REQUEST: Int = 1
    private val ACC_PROFILE_CREATOR_REQUEST: Int = 2
    private val ACC_PROFILE_EDITOR_REQUEST: Int = 3
    private val ACC_PROFILE_SCHEDULER_REQUEST: Int = 4

    companion object {
        fun newInstance() = DashboardFragment()
    }

    private lateinit var viewModel: DashboardViewModel
    private lateinit var configViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dashboard_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let { it ->
            configViewModel = ViewModelProviders.of(it).get(SharedViewModel::class.java)

            viewModel = ViewModelProviders.of(it).get(DashboardViewModel::class.java)

            viewModel.getBatteryInfo().observe(this, Observer<BatteryInfo>{info ->
                updateBatteryInfo(info)
            })

            viewModel.getIsDaemonRunning().observe(this, Observer<Boolean>{ daemon ->
                updateAccdStatus(daemon)
            })

            configViewModel.observeConfig(this, Observer { config ->
                view.dash_resetStatusUnplug_switch.isChecked = config.configResetUnplugged
            })

            view.dash_resetBatteryStats_button.setOnClickListener {
                Acc.instance.resetBatteryStats()
            }

            view.dash_editCargingLimitOnce_button.setOnClickListener {
                val dialog = MaterialDialog(it.context).show {
                    title(R.string.edit_charging_limit_once)
                    message(R.string.edit_charging_limit_once_dialog_msg)
                    customView(R.layout.edit_charging_limit_once_dialog)
                    positiveButton(R.string.apply) {
                        Acc.instance.setChargingLimitForOneCharge(getCustomView().findViewById<NumberPicker>(R.id.charging_limit).value)
                        Toast.makeText(context, R.string.done, Toast.LENGTH_LONG).show()
                    }
                    negativeButton(android.R.string.cancel)
                }

                val picker = dialog.getCustomView().charging_limit
                picker.maxValue = 100
                picker.minValue = configViewModel.getAccConfigValue { it.configCapacity.pause }
                picker.value = 100
            }

            view.dash_resetStatusUnplug_switch.setOnCheckedChangeListener { _, isChecked ->
                configViewModel.updateAccConfigValue {
                    it.configResetUnplugged = isChecked

                    //If I manually modify the mAccConfig I have to set current profile to null (custom profile)
                    configViewModel.setCurrentSelectedProfile(-1)
                }
            }

            view.status_card_view.setOnClickListener(::accdOnClick)
        }
    }

    /**
     *  Update's UI components for the ACCD Status Card
     */
    private fun updateAccdStatus(isDaemonRunning: Boolean) {
        if (isDaemonRunning) {
            // ACCD Status Card
            dash_accdStatus_textView.text = getString(R.string.acc_daemon_status_running)
            dash_accdStatus_frameLay.background = ColorDrawable(resources.getColor(R.color.colorSuccessful))
            dash_accdStatus_imageView.setImageResource(R.drawable.ic_baseline_check_circle_24px)

            dash_daemonToggle_button.text = getString(R.string.stop)
            dash_daemonToggle_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_stop_24px, 0, 0, 0)
        } else {
            // ACCD Status Card
            dash_accdStatus_textView.text = getString(R.string.acc_daemon_status_not_running)
            dash_accdStatus_frameLay.background = ColorDrawable(resources.getColor(R.color.colorError))
            dash_accdStatus_imageView.setImageResource(R.drawable.ic_baseline_error_24px)

            dash_daemonToggle_button.text = getString(R.string.start)
            dash_daemonToggle_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_play_arrow_24px, 0, 0, 0)
        }
    }

    /**
     * Function for ACCD status card OnClick in DashboardFragment
     */
    fun accdOnClick(view: View) {
        if (dash_accdButtons_linLay.visibility == View.GONE) {
            dash_accdButtons_linLay.visibility = View.VISIBLE
            dash_title_accdStatus_textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_up_24px, 0)
        } else {
            dash_accdButtons_linLay.visibility = View.GONE
            dash_title_accdStatus_textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_down_24px, 0)
        }
    }

    /**
     * Function for setting the respective battery text into their textviews.
     * TODO: See if the performance is still a little jank, otherwise, use the handler to update UI elements within the observable.
     */
    private fun updateBatteryInfo(batteryInfo: BatteryInfo) {
        // Battery Capacity
        dash_batteryCapacity_pBar.progress = batteryInfo.capacity
        // Battery Status (Charging (Fast)
        dash_batteryStatus_textView.text = getString(R.string.info_status_extended, batteryInfo.status, batteryInfo.chargeType)
        // Battery Speed (500mA at 4.11V)
        val charging = batteryInfo.isCharging()
        dash_batteryChargingSpeed_textView.text = if(charging) getString(R.string.info_charging_speed) else getString(R.string.info_discharging_speed)
        dash_chargingSpeed_textView.text = getString(if(charging) R.string.info_charging_speed_extended else R.string.info_discharging_speed_extended, batteryInfo.getSimpleCurrentNow() * (if(charging) 1 else -1), batteryInfo.getVoltageNow())
        // Battery Temperature
        dash_batteryTemperature_textView.text = batteryInfo.temperature.toString().plus(Typography.degree)
        // Battery Health
        dash_batteryHealth_textView.text = batteryInfo.health
    }

}
