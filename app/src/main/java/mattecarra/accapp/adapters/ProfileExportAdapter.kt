package mattecarra.accapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ProfileExportItem

class ProfileExportAdapter: RecyclerView.Adapter<ProfileExportAdapter.ProfileExportItemHolder>() {

    private var mProfiles: List<ProfileExportItem> = ArrayList()

    inner class ProfileExportItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ProfileExportItem.Listener {

        lateinit var mExportProfile: ProfileExportItem

        val titleTv: TextView = itemView.findViewById(R.id.pex_item_name_tv)
        val selectedCb: CheckBox = itemView.findViewById(R.id.pex_item_checkbox)

        fun setData(exportItem: ProfileExportItem) {
            mExportProfile = exportItem
            titleTv.text = exportItem.getName()
        }

        override fun onCheckedChanged(value: Boolean) {
            selectedCb.isChecked = value
        }

        fun getExportProfile(): ProfileExportItem {
            return mExportProfile
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileExportItemHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.profile_export_item, parent, false)
        return ProfileExportItemHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileExportItemHolder, position: Int) {
        val exportProfile: ProfileExportItem = mProfiles[position]
        exportProfile.setOnCheckedChangedListener(holder)
        holder.setData(exportProfile)
    }

    override fun onViewRecycled(holder: ProfileExportItemHolder) {
        holder.getExportProfile().setOnCheckedChangedListener(null)
    }

    override fun getItemCount(): Int {
        return mProfiles.size
    }

    fun getCheckedProfiles(): List<AccaProfile> {
        val profiles: List<AccaProfile> = ArrayList()

        for (items in mProfiles) {
            profiles.add(items.getProfile())
        }

        return profiles
    }

    fun toggleCheckboxes() {
        // TODO: Toggle all profiles
    }

    private fun setCheckboxStates(checked: Boolean) {
        for (profile in mProfiles) {
            if (profile.isChecked() != checked) {
                profile.setIsChecked(checked)
            }
        }
    }
}