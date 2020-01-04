package mattecarra.accapp.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
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
import mattecarra.accapp.SharedViewModel
import mattecarra.accapp.databinding.DashboardFragmentBinding
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

    private lateinit var mViewModel: DashboardViewModel
    private lateinit var configViewModel: SharedViewModel
    private lateinit var preferences: Preferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: DashboardFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.dashboard_fragment,
            container,
            false
        )

        val view = binding.root

        mViewModel = activity?.let { ViewModelProviders.of(it).get(DashboardViewModel::class.java) } ?: ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        binding.viewModel = mViewModel
        binding.lifecycleOwner = this

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { it ->
            preferences = Preferences(it)

            configViewModel = ViewModelProviders.of(it).get(SharedViewModel::class.java)

            configViewModel.observeConfig(this, Observer { config ->
                view.dash_resetStatusUnplug_switch.isChecked = config.configResetUnplugged
                view.dash_resetBSOnPause_switch.isChecked = config.configResetBsOnPause
            })

            view.dash_resetBatteryStats_button.setOnClickListener {
                launch {
                    Acc.instance.resetBatteryStats()
                }
            }

            view.dash_editCargingLimitOnce_button.setOnClickListener {
                val dialog = MaterialDialog(it.context).show {
                    title(R.string.edit_charging_limit_once)
                    message(R.string.edit_charging_limit_once_dialog_msg)
                    customView(R.layout.edit_charging_limit_once_dialog)
                    positiveButton(R.string.apply) {
                        launch {
                            Acc.instance.setChargingLimitForOneCharge(getCustomView().findViewById<NumberPicker>(R.id.charging_limit).value)
                            Toast.makeText(context, R.string.done, Toast.LENGTH_LONG).show()
                        }
                    }
                    negativeButton(android.R.string.cancel)
                }

                val picker = dialog.getCustomView().charging_limit
                picker.maxValue = 100
                picker.minValue = configViewModel.getAccConfigValue { it.configCapacity.pause }
                picker.value = 100
            }

            view.dash_resetStatusUnplug_switch.setOnCheckedChangeListener { _, isChecked ->
                launch {
                    configViewModel.updateAccConfigValue {
                        it.configResetUnplugged = isChecked

                        //If I manually modify the mAccConfig I have to set current profile to null (custom profile)
                        configViewModel.clearCurrentSelectedProfile()
                    }
                }
            }

            view.dash_resetBSOnPause_switch.setOnCheckedChangeListener { _, isCheked ->
                launch {
                    configViewModel.updateAccConfigValue {
                        it.configResetBsOnPause = isCheked

                        //If I manually modify the mAccConfig I have to set current profile to null (custom profile)
                        configViewModel.clearCurrentSelectedProfile()
                    }
                }
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

                val observer = Observer<Boolean> { accdRunning ->
                    if(accdRunning == !stopDaemon && !finished.getAndSet(true)) { //if accDeamon status is the opposite of the status it had before the action -> change had effect
                        finished.set(true)

                        dash_daemonToggle_button.isEnabled = true
                        dash_daemonRestart_button.isEnabled = true
                    }
                }

                mViewModel.daemon.observe(this@DashboardFragment, observer)

                withContext(Dispatchers.IO) {
                    if (stopDaemon)
                        Acc.instance.abcStopDaemon()
                    else
                        Acc.instance.abcStartDaemon()
                }

                delay(5000)

                mViewModel.daemon.removeObserver(observer)

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
                    Acc.instance.abcRestartDaemon()
                }

                delay(3000)

                dash_daemonToggle_button.isEnabled = true
                dash_daemonRestart_button.isEnabled = true
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
