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
import mattecarra.accapp.databinding.DashboardFragmentBinding
import mattecarra.accapp.SharedViewModel
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
    private var mIsDaemonRunning: Boolean? = null

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

        mViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        val view = binding.root
        binding.viewModel = mViewModel
        binding.lifecycleOwner = this
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

            view.status_card_view.setOnClickListener(::accdOnClick)
        }

        dash_daemonToggle_button.setOnClickListener {
            Toast.makeText(context, R.string.wait, Toast.LENGTH_LONG).show()

            launch {
                val finished = AtomicBoolean(false)
                val stopDaemon = Acc.instance.isAccdRunning()

                dash_daemonToggle_button.isEnabled = false
                dash_daemonRestart_button.isEnabled = false

                val observer = Observer<Boolean?> { accdRunning ->
                    if (accdRunning != null) {
                        if(accdRunning == !stopDaemon && !finished.getAndSet(true)) { //if accDeamon status is the opposite of the status it had before the action -> change had effect
                            finished.set(true)

                            dash_daemonToggle_button.isEnabled = true
                            dash_daemonRestart_button.isEnabled = true
                        }
                    }
                }

                mViewModel.daemon.observe(viewLifecycleOwner, observer)

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

        mViewModel.daemon.observe(viewLifecycleOwner, Observer { d ->
            toggleAccdStatusUi(d)
            mIsDaemonRunning = d
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
