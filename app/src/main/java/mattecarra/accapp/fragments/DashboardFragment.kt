package mattecarra.accapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mattecarra.accapp.Preferences
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.databinding.DashboardFragmentBinding
import mattecarra.accapp.databinding.EditChargingLimitOnceDialogBinding
import mattecarra.accapp.models.DashboardValues
import mattecarra.accapp.utils.LogExt
import mattecarra.accapp.utils.ScopedFragment
import mattecarra.accapp.viewmodel.DashboardViewModel
import mattecarra.accapp.viewmodel.SharedViewModel
import java.util.concurrent.atomic.AtomicBoolean

class DashboardFragment : ScopedFragment()
{

    private lateinit var binding :DashboardFragmentBinding

    private val LOG_TAG = "DashboardFragment"

    private val PERMISSION_REQUEST: Int = 0
    private val ACC_CONFIG_EDITOR_REQUEST: Int = 1
    private val ACC_PROFILE_CREATOR_REQUEST: Int = 2
    private val ACC_PROFILE_EDITOR_REQUEST: Int = 3
    private val ACC_PROFILE_SCHEDULER_REQUEST: Int = 4

    companion object
    {
        fun newInstance() = DashboardFragment()
    }

    private val mViewModel: DashboardViewModel by activityViewModels()
    private lateinit var mDashboardConfigFrg: DashboardConfigFragment
    private lateinit var configViewModel: SharedViewModel
    private lateinit var preferences: Preferences
    private var mIsDaemonRunning: Boolean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DashboardFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        LogExt().d(javaClass.simpleName, "onViewCreated()")

        super.onViewCreated(view, savedInstanceState)

        //-----------------------------------------------------------------

        val transaction = activity?.supportFragmentManager?.beginTransaction()
        mDashboardConfigFrg = DashboardConfigFragment.newInstance()
        transaction?.replace(R.id.current_profile, mDashboardConfigFrg)
        transaction?.commit()

        //-----------------------------------------------------------------

        mViewModel.getDashboardValues().observe(viewLifecycleOwner) { dash ->
            // Set Status Card text
            dash.daemon?.let { daemon -> setAccdStatusUi(daemon) }

            // Battery/Charge details
            binding.dashBatteryCapacityPBar.progress = dash.batteryInfo.capacity
            binding.dashBatteryStatusTextView.text = getString(R.string.info_status_extended, dash.batteryInfo.status, dash.batteryInfo.chargeType)

            binding.dashBatteryChargingSpeedTextView.text = if (dash.batteryInfo.isCharging()) getString(R.string.info_charging_speed) else getString(R.string.info_discharging_speed)

            val plus = if (Acc.instance.version < 202107280) dash.batteryInfo.isCharging() else true
            binding.dashChargingSpeedTextView.text = dash.batteryInfo.getCurrentNow(preferences.currentInputUnitOfMeasure, preferences.currentOutputUnitOfMeasure, plus, true)

            binding.dashBatteryTemperatureTextView.text = dash.batteryInfo.getTemperature(preferences.temperatureOutputUnitOfMeasure, true)
            binding.dashBatteryHealthTextView.text = dash.batteryInfo.health
            binding.dashBatteryVoltageTextView.text = dash.batteryInfo.getVoltageNow(preferences.voltageInputUnitOfMeasure, preferences.voltageOutputUnitOfMeasure, true)
        }

        activity?.let { it ->

            preferences = Preferences(it)
            configViewModel = ViewModelProvider(it).get(SharedViewModel::class.java)

            binding.dashResetBatteryStatsButton.setOnClickListener {
                launch { Acc.instance.resetBatteryStats() }
            }

            binding.dashEditCargingLimitOnceButton.setOnClickListener {
                val dialog = EditChargingLimitOnceDialogBinding.inflate(layoutInflater)
                MaterialDialog(it.context).show {
                    title(R.string.edit_charging_limit_once_button)
                    message(R.string.edit_charging_limit_once_dialog_msg)
                    cancelOnTouchOutside(false)
                    customView(view=dialog.root)
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

                val picker = dialog.chargingLimit
                picker.maxValue = 100
                picker.minValue = 20
                picker.value = 100
            }
        }

        binding.dashDaemonToggleButton.setOnClickListener {
            Toast.makeText(context, R.string.wait, Toast.LENGTH_LONG).show()

            launch {
                val finished = AtomicBoolean(false)
                val stopDaemon = Acc.instance.isAccdRunning()

                binding.dashDaemonToggleButton.isEnabled = false
                binding.dashDaemonRestartButton.isEnabled = false

                val observer = Observer<DashboardValues> { daemonInfo ->
                    if (daemonInfo?.daemon == !stopDaemon && !finished.getAndSet(true))
                    { //if accDeamon status is the opposite of the status it had before the action -> change had effect
                        finished.set(true)

                        binding.dashDaemonToggleButton.isEnabled = true
                        binding.dashDaemonRestartButton.isEnabled = true
                    }
                }

                mViewModel.getDashboardValues().observe(viewLifecycleOwner, observer)

                withContext(Dispatchers.IO) {
                    if (stopDaemon) Acc.instance.abcStopDaemon()
                    else Acc.instance.abcStartDaemon()
                }

                delay(5000)

                mViewModel.getDashboardValues().removeObserver(observer)

                if (!finished.getAndSet(true))
                {
                    binding.dashDaemonToggleButton.isEnabled = true
                    binding.dashDaemonRestartButton.isEnabled = true
                }
            }
        }

        binding.dashDaemonRestartButton.setOnClickListener {
            Toast.makeText(context, R.string.wait, Toast.LENGTH_LONG).show()

            binding.dashDaemonToggleButton.isEnabled = false
            binding.dashDaemonRestartButton.isEnabled = false

            launch {
                binding.dashDaemonToggleButton.isEnabled = false
                binding.dashDaemonRestartButton.isEnabled = false

                withContext(Dispatchers.IO) {
                    Acc.instance.accRestartDaemon()
                }

                delay(3000)

                binding.dashDaemonToggleButton.isEnabled = true
                binding.dashDaemonRestartButton.isEnabled = true
            }
        }

        mViewModel.getDashboardValues().observe(viewLifecycleOwner, Observer { d ->
            toggleAccdStatusUi(d.daemon)
            mIsDaemonRunning = d.daemon
        })
    }

    private fun toggleAccdStatusUi(running: Boolean?)
    {
        when (mIsDaemonRunning)
        {
            null ->
            {
                setAccdStatusUi(running)
            }
            false ->
            {
                if (running != null && running) setAccdStatusUi(running)
            }
            true ->
            {
                if (running != null && !running) setAccdStatusUi(running)
            }
        }
    }

    private fun setAccdStatusUi(running: Boolean?)
    {
        if (running == null) return

        if (running)
        {
            // Hide progress bar
            binding.dashAccdStatusPb.visibility = View.GONE
            // Show and change icon
            binding.dashAccdStatusImageView.visibility = View.VISIBLE
            binding.dashAccdStatusFrameLay.setBackgroundColor(getColor(requireActivity().baseContext, R.color.colorTransparent))
            binding.dashAccdStatusImageView.setImageResource(R.drawable.ic_outline_check_circle_24px)
            binding.dashAccdStatusTextView.setText(R.string.acc_daemon_status_running)
            // Enable buttons
            binding.dashDaemonRestartButton.isEnabled = true
            binding.dashDaemonToggleButton.isEnabled = true
            binding.dashDaemonToggleButton.setIconResource(R.drawable.ic_outline_stop_24px)
            binding.dashDaemonToggleButton.setText(R.string.stop)
        }
        else
        {
            // Hide progress bar
            binding.dashAccdStatusPb.visibility = View.GONE
            // Show and change icon
            binding.dashAccdStatusImageView.visibility = View.VISIBLE
            binding.dashAccdStatusFrameLay.setBackgroundColor(getColor(requireActivity().baseContext, R.color.colorTransparent))
            binding.dashAccdStatusImageView.setImageResource(R.drawable.ic_outline_error_outline_24px)
            binding.dashAccdStatusTextView.setText(R.string.acc_daemon_status_not_running)
            // Enable buttons
            binding.dashDaemonRestartButton.isEnabled = true
            binding.dashDaemonToggleButton.isEnabled = true
            binding.dashDaemonToggleButton.setIconResource(R.drawable.ic_outline_play_arrow_24px)
            binding.dashDaemonToggleButton.setText(R.string.start)
        }
    }

}
