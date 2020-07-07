package mattecarra.accapp.adapters

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp.models.ProfileEntry

class ProfileEntryHolder: RecyclerView.ViewHolder, ProfileEntry.Listener {
    private var mNameTv: TextView
    private var mCheckbox: CheckBox
    private lateinit var mProfileEntry: ProfileEntry;

    constructor(view: View) : super(view) {
        mNameTv = view.findViewById(R.id.pex_item_name_tv)
        mCheckbox = view.findViewById(R.id.pex_item_checkbox)

        view.setOnClickListener { v-> mProfileEntry.setIsChecked(!mProfileEntry.isChecked()) }
    }

    fun setData(profileEntry: ProfileEntry) {
        mProfileEntry = profileEntry

        val context = itemView.context
        mNameTv.setText(profileEntry.getName())
        mCheckbox.isChecked = profileEntry.isChecked()
    }

    fun getEntry(): ProfileEntry {
        return mProfileEntry
    }

    override fun onCheckChanged(value: Boolean) {
        mCheckbox.isChecked = value
    }
}