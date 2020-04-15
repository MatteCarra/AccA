package mattecarra.accapp.dialogs

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.Schedule

class ProfileSpinnerAdapter : BaseAdapter(), SpinnerAdapter {
    private var mList = emptyList<AccaProfile>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =  convertView ?: LayoutInflater.from(parent.context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        val textView = view.findViewById(android.R.id.text1) as TextView
        textView.text = getItem(position).profileName
        return view;
    }

    fun setItems(list: List<AccaProfile>) {
        if(mList != list) {
            mList = list
        }
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): AccaProfile {
        return mList[position]
    }

    override fun getItemId(position: Int): Long {
        return mList[position].uid.toLong()
    }

    override fun getCount(): Int {
        return mList.size
    }

}

typealias AddScheduleListener =
        ((profileId: Long, scheduleName: String, time: String, executeOnce: Boolean, executeOnBoot: Boolean) -> Unit)


fun MaterialDialog.addScheduleDialog(
    profilesLiveData: LiveData<List<AccaProfile>>,
    profiles: MutableList<AccaProfile> = mutableListOf(AccaProfile(-1, context.getString(R.string.new_config), Acc.instance.defaultConfig)),
    schedule: Schedule? = null,
    listener: AddScheduleListener
): MaterialDialog {
    val adapter = ProfileSpinnerAdapter()
    val observer = Observer<List<AccaProfile>> {
        profiles.addAll(it)
        adapter.setItems(profiles)
    }

    val dialog = customView(R.layout.schedule_dialog)
        .positiveButton(R.string.save) { dialog ->
            val view = dialog.getCustomView()
            val spinner = view.findViewById<Spinner>(R.id.profile_selector)
            val timePicker = view.findViewById<TimePicker>(R.id.time_picker)
            val scheduleType = view.findViewById<Spinner>(R.id.schedule_type_selector).selectedItemId
            val scheduleName = view.findViewById<EditText>(R.id.schedule_name_edit_text)
            val executeOnBootCheckBox = view.findViewById<CheckBox>(R.id.execute_on_boot_checkbox)

            val time = if(scheduleType == 2L) {
                "boot"
            } else {
                val hour =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.hour else timePicker.currentHour
                val minute =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.minute else timePicker.currentMinute

                "${String.format("%02d", hour)}${String.format("%02d", minute)}"
            }

            listener(spinner.selectedItemId, scheduleName.text.toString(), time, scheduleType == 1L, executeOnBootCheckBox.isChecked && scheduleType != 2L)
        }
        .onDismiss {
            profilesLiveData.removeObserver(observer)
        }


    val customView = dialog.getCustomView()
    val spinner = customView.findViewById<Spinner>(R.id.profile_selector)
    val timePicker = view.findViewById<TimePicker>(R.id.time_picker)
    val scheduleTypeSpinner = customView.findViewById<Spinner>(R.id.schedule_type_selector)
    val scheduleNameEditText = view.findViewById<EditText>(R.id.schedule_name_edit_text)
    val executeOnBootCheckBox = view.findViewById<CheckBox>(R.id.execute_on_boot_checkbox)

    profilesLiveData.observeForever(observer)
    spinner.adapter = adapter

    scheduleTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            when(id) {
                 2L -> {
                     customView.findViewById<LinearLayout>(R.id.time_picker_container).visibility = View.GONE
                 }
                else -> {
                    customView.findViewById<LinearLayout>(R.id.time_picker_container).visibility = View.VISIBLE
                }
            }
        }
    }

    timePicker.setIs24HourView(true)

    schedule?.let { schedule ->
        scheduleTypeSpinner.setSelection(
            when {
                schedule.isBootSchedule() -> 2
                schedule.executeOnce -> 1
                else -> 0
            }
        )

        schedule.getTime()?.let { (hour, minute) ->
            timePicker.currentHour = hour
            timePicker.currentMinute = minute
        }

        scheduleNameEditText.setText(schedule.profile.scheduleName)

        executeOnBootCheckBox.isChecked = schedule.executeOnBoot
    }

    return dialog
}

typealias EditScheduleListener =
        ((profileId: Long, profileName: String, time: String, executeOnce: Boolean, executeOnBoot: Boolean) -> Unit)


fun MaterialDialog.editScheduleDialog(
    schedule: Schedule,
    profilesLiveData: LiveData<List<AccaProfile>>,
    listener: EditScheduleListener
): MaterialDialog {
    return addScheduleDialog(
        profilesLiveData,
        mutableListOf(AccaProfile(-1, context.getString(R.string.schedule_profile_keep_current), Acc.instance.defaultConfig), AccaProfile(-2, context.getString(R.string.schedule_profile_edit_current), Acc.instance.defaultConfig), AccaProfile(-3, context.getString(R.string.new_config), Acc.instance.defaultConfig)),
        schedule,
        listener
    )
}