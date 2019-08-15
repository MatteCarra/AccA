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
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile

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
        ((profileId: Long, hour: Int, minute: Int) -> Unit)


fun MaterialDialog.addScheduleDialog(
    profilesLiveData: LiveData<List<AccaProfile>>,
    listener: AddScheduleListener
): MaterialDialog {
    val ADD_NEW = AccaProfile(-1, context.getString(R.string.new_config), Acc.instance.defaultConfig)

    val adapter = ProfileSpinnerAdapter()
    val observer = Observer<List<AccaProfile>> {
        val list = mutableListOf(ADD_NEW)
        list.addAll(it)
        adapter.setItems(list)
    }

    val dialog = customView(R.layout.schedule_dialog)
        .positiveButton(R.string.save) { dialog ->
            val view = dialog.getCustomView()
            val spinner = view.findViewById<Spinner>(R.id.profile_selector)
            val timePicker = view.findViewById<TimePicker>(R.id.time_picker)
            val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.hour else timePicker.currentHour
            val minute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.minute else timePicker.currentMinute

            listener(spinner.selectedItemId, hour, minute)
        }
        .onDismiss {
            profilesLiveData.removeObserver(observer)
        }


    val view = dialog.getCustomView()
    val spinner = view.findViewById<Spinner>(R.id.profile_selector)

    profilesLiveData.observeForever(observer)
    spinner.adapter = adapter

    view.findViewById<TimePicker>(R.id.time_picker).setIs24HourView(true)

    return dialog
}