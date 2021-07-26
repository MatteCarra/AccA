package mattecarra.accapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.android.synthetic.main.dashboard_fragment.*
import kotlinx.android.synthetic.main.dashboard_fragment.view.*
import kotlinx.android.synthetic.main.edit_charging_limit_once_dialog.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mattecarra.accapp.Preferences

import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import mattecarra.accapp.SharedViewModel
import mattecarra.accapp.activities.MainActivity
import mattecarra.accapp.models.DashboardValues
import mattecarra.accapp.utils.ScopedFragment
import java.util.concurrent.atomic.AtomicBoolean

class DashboardFragment : ScopedFragment() {

    private val LOG_TAG = "DashboardFragment"

    private val PERMISSION_REQUEST: Int = 0
    private val ACC_CONFIG_EDITOR_REQUEST: Int = 1
    private val ACC_PROFILE_CREATOR_REQUEST: Int = 2
    private val ACC_PROFILE_EDITOR_REQUEST: Int = 3
    private val ACC_PROFILE_SCHEDULER_REQUEST: Int = 4

    companion object {
        fun newInstance() = DashboardFragment()
    }

    private val mViewModel: DashboardViewModel by activityViewModels()
    private lateinit var configViewModel: SharedViewModel
    private lateinit var preferences: Preferences
    private var mIsDaemonRunning: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dashboard_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.getDashboardValues().observe(viewLifecycleOwner) { dash ->
            // Set Status Card text
            dash.daemon?.let { daemon ->
                setAccdStatusUi(daemon)
            }

            // Battery/Charge details
            dash_batteryCapacity_pBar.progress = dash.batteryInfo.capacity
            dash_batteryStatus_textView.text = getString(R.string.info_status_extended,
                dash.batteryInfo.status,
                dash.batteryInfo.chargeType)

            if (dash.batteryInfo.isCharging()) {
                dash_batteryChargingSpeed_textView.text = getString(R.string.info_charging_speed)
                dash_chargingSpeed_textView.text = getString(R.string.info_charging_speed_extended,
                    dash.batteryInfo.getCurrentNow(preferences.currentUnitOfMeasure) * (if(dash.batteryInfo.isCharging()) 1 else -1),
                    dash.batteryInfo.getVoltageNow(preferences.voltageUnitOfMeasure))
            } else {
                dash_batteryChargingSpeed_textView.text = getString(R.string.info_discharging_speed)
                dash_chargingSpeed_textView.text =
                    getString(R.string.info_discharging_speed_extended,
                        dash.batteryInfo.getCurrentNow(preferences.currentUnitOfMeasure) * (if(dash.batteryInfo.isCharging()) 1 else -1))
            }

            dash_batteryTemperature_textView.text = dash.batteryInfo.temperature.toString() + Typography.degree
            dash_batteryHealth_textView.text = dash.batteryInfo.health

        }

        activity?.let { it ->
            preferences = Preferences(it)

            configViewModel = ViewModelProvider(it).get(SharedViewModel::class.java)

            view.dash_resetBatteryStats_button.setOnClickListener {
                launch {
                    Acc.instance.resetBatteryStats()
                }
            }

            view.dash_editCargingLimitOnce_button.setOnClickListener {
                val dialog = MaterialDialog(it.context).show {
                    title(R.string.edit_charging_limit_once)
                    message(R.string.edit_charging_limit_once_dialog_msg)
                    cancelOnTouchOutside(false)
                    customView(R.layout.edit_charging_limit_once_dialog)
                    positiveButton(R.string.apply) {
                        launch {
                            val limit = getCustomView().findViewById<NumberPicker>(R.id.charging_limit).value
                            Acc.instance.setChargingLimitForOneCharge(limit)
                            Toast.makeText(context, getString(R.string.done_applied_charge_limit, limit), Toast.LENGTH_LONG).show()
                        }
                    }
                    negativeButton(android.R.string.cancel) {
                        launch {
                            Toast.makeText(context, R.string.charge_limit_not_applied, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                val picker = dialog.getCustomView().charging_limit
                picker.maxValue = 100
                picker.minValue = 20
                picker.value = 100
            }

            view.status_card_view.setOnClickListener(::accdOnClick)
        }

        dash_daemonToggle_button.setOnClickListener {
            Toast.makeText(context, R.string.wait, Toast.LENGTH_LONG).show()

            launch {
                val finished = AtomicBoolean(false)
                val stopDaemon = Acc.instance.isAccdRunning()

                dash_daemonToggle_button.isEnabled = false
                dash_daemonRestart_button.isEnabled = false

                val observer = Observer<DashboardValues> { daemonInfo ->
                    if(daemonInfo?.daemon == !stopDaemon && !finished.getAndSet(true)) { //if accDeamon status is the opposite of the status it had before the action -> change had effect
                        finished.set(true)

                        dash_daemonToggle_button.isEnabled = true
                        dash_daemonRestart_button.isEnabled = true
                    }
                }

                mViewModel.getDashboardValues().observe(viewLifecycleOwner, observer)

                withContext(Dispatchers.IO) {
                    if (stopDaemon)
                        Acc.instance.abcStopDaemon()
                    else
                        Acc.instance.abcStartDaemon()
                }

                delay(5000)

                mViewModel.getDashboardValues().removeObserver(observer)

                if(!finished.getAndSet(true)) {
                    dash_daemonToggle_button.isEnabled = true
                    dash_daemonRestart_button.isEnabled = true
                }
            }
        }

        dash_daemonRestart_button.setOnClickListener {
            Toast.makeText(context, R.string.wait, Toast.LENGTH_LONG).show()

            dash_daemonToggle_button.isEnabled = false
            dash_daemonRestart_button.isEnabled = false

            launch {
                dash_daemonToggle_button.isEnabled = false
                dash_daemonRestart_button.isEnabled = false

                withContext(Dispatchers.IO) {
                    Acc.instance.accRestartDaemon()
                }

                delay(3000)

                dash_daemonToggle_button.isEnabled = true
                dash_daemonRestart_button.isEnabled = true
            }
        }

        mViewModel.getDashboardValues().observe(viewLifecycleOwner, Observer { d ->
            toggleAccdStatusUi(d.daemon)
            mIsDaemonRunning = d.daemon
        })
    }

    private fun toggleAccdStatusUi(running: Boolean?) {
        when (mIsDaemonRunning) {
            null -> {
                setAccdStatusUi(running)
            }
            false -> {
                if (running != null && running) {
                    setAccdStatusUi(running)
                }
            }
            true -> {
                if (running != null && !running) {
                    setAccdStatusUi(running)
                }
            }
        }
    }

    private fun setAccdStatusUi(running: Boolean?) {
        if (running != null) {
            if (running) {
                // Hide progress bar
                dash_accdStatus_pb.visibility = View.GONE
                // Show and change icon
                dash_accdStatus_imageView.visibility = View.VISIBLE
                dash_accdStatus_frameLay.setBackgroundColor(getColor(requireActivity().baseContext, R.color.colorSuccessful))
                dash_accdStatus_imageView.setImageResource(R.drawable.ic_outline_check_circle_24px)
                dash_accdStatus_textView.setText(R.string.acc_daemon_status_running)
                // Enable buttons
                dash_daemonRestart_button.isEnabled = true
                dash_daemonToggle_button.isEnabled = true
                dash_daemonToggle_button.setIconResource(R.drawable.ic_outline_stop_24px)
                dash_daemonToggle_button.setText(R.string.stop)
            } else {
                // Hide progress bar
                dash_accdStatus_pb.visibility = View.GONE
                // Show and change icon
                dash_accdStatus_imageView.visibility = View.VISIBLE
                dash_accdStatus_frameLay.setBackgroundColor(getColor(requireActivity().baseContext, R.color.color_error))
                dash_accdStatus_imageView.setImageResource(R.drawable.ic_outline_error_outline_24px)
                dash_accdStatus_textView.setText(R.string.acc_daemon_status_not_running)
                // Enable buttons
                dash_daemonRestart_button.isEnabled = true
                dash_daemonToggle_button.isEnabled = true
                dash_daemonToggle_button.setIconResource(R.drawable.ic_outline_play_arrow_24px)
                dash_daemonToggle_button.setText(R.string.start)
            }
        }
    }

    /**
     * Function for ACCD status card OnClick in DashboardFragment
     */
    fun accdOnClick(view: View) {
        if (dash_accdButtons_linLay.visibility == View.GONE) {
            dash_accdButtons_linLay.visibility = View.VISIBLE
            dash_title_accdStatus_textView.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_baseline_arrow_drop_up_24px,
                0
            )
        } else {
            dash_accdButtons_linLay.visibility = View.GONE
            dash_title_accdStatus_textView.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_baseline_arrow_drop_down_24px,
                0
            )
        }
    }
}
