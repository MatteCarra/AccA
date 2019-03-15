package mattecarra.accapp.fragments

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.content_main.*

import mattecarra.accapp.R
import mattecarra.accapp.activities.AccConfigEditorActivity
import mattecarra.accapp.data.BatteryInfo

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dashboard_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)

        viewModel.getBatteryInfo().observe(this, Observer<BatteryInfo>{info ->
            // TODO: Update UI with info.
            updateBatteryInfo(info)
        })

        viewModel.getIsDaemonRunning().observe(this, Observer<Boolean>{ daemon ->
            updateAccdStatus(daemon)
        })
    }

    /**
     *  Update's UI components for the ACCD Status Card
     */
    private fun updateAccdStatus(isDaemonRunning: Boolean) {
        if (isDaemonRunning) {
            // ACCD Status Card
            tv_main_accdStatus.text = getString(R.string.acc_daemon_status_running)
            fl_status_container.background = ColorDrawable(resources.getColor(R.color.colorSuccessful))
            iv_main_status_icon.setImageResource(R.drawable.ic_baseline_check_circle_24px)

            daemon_start_stop.text = getString(R.string.stop)
            daemon_start_stop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_stop_24px, 0, 0, 0)
        } else {
            // ACCD Status Card
            tv_main_accdStatus.text = getString(R.string.acc_daemon_status_not_running)
            fl_status_container.background = ColorDrawable(resources.getColor(R.color.colorError))
            iv_main_status_icon.setImageResource(R.drawable.ic_baseline_error_24px)

            daemon_start_stop.text = getString(R.string.start)
            daemon_start_stop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_play_arrow_24px, 0, 0, 0)
        }
    }

    /**
     * Function for ACCD status card OnClick
     */
    fun accdOnClick(view: View) {
        if (consLay_accdButtons.visibility == View.GONE) {
            consLay_accdButtons.visibility = View.VISIBLE
            tv_main_title_accdStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_up_24px, 0)
        } else {
            consLay_accdButtons.visibility = View.GONE
            tv_main_title_accdStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_down_24px, 0)
        }
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
     * Function for setting the respective battery text into their textviews.
     * TODO: See if the performance is still a little jank, otherwise, use the handler to update UI elements within the observable.
     */
    private fun updateBatteryInfo(batteryInfo: BatteryInfo) {

        // Battery Capacity
        progressBar_capacity.progress = batteryInfo.capacity
        // Battery Status (Charging (Fast)
        tv_main_batteryStatus.text = getString(R.string.info_status_extended, batteryInfo.status, batteryInfo.chargeType)
        // Battery Speed (500mA at 4.11V)
        val charging = batteryInfo.isCharging()
        charging_discharging_speed_label.text = if(charging) getString(R.string.info_charging_speed) else getString(R.string.info_discharging_speed)
        tv_main_batterySpeed.text = getString(if(charging) R.string.info_charging_speed_extended else R.string.info_discharging_speed_extended, batteryInfo.getSimpleCurrentNow() * (if(charging) -1 else 1), batteryInfo.getVoltageNow())
        // Battery Temperature
        tv_main_batteryTemp.text = batteryInfo.temperature.toString().plus(Typography.degree)
        // Battery Health
        tv_main_batteryHealth.text = batteryInfo.health
    }

}
