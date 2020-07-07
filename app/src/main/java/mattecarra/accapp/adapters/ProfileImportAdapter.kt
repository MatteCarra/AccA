package mattecarra.accapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ExportItem

class ProfileImportAdapter: RecyclerView.Adapter<ProfileImportAdapter.ProfileImportItemHolder>() {

    private var mProfiles: ArrayList<ExportItem> = ArrayList()

    inner class ProfileImportItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ExportItem.Listener, View.OnClickListener {

        private lateinit var mExportProfile: ExportItem

        private val itemRl: RelativeLayout = itemView.findViewById(R.id.pex_item_rl)
        private val titleTv: TextView = itemView.findViewById(R.id.pex_item_name_tv)
        private val selectedCb: CheckBox = itemView.findViewById(R.id.pex_item_checkbox)

        init {
            itemRl.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            mProfiles[adapterPosition].check()
            // TODO: call viewmodel from activity to update the data (checked item)
        }

        fun setData(exportItem: ExportItem) {
            mExportProfile = exportItem
            titleTv.text = exportItem.getName()
            // Get checked state from model
            if (mExportProfile.isChecked()) {
                mExportProfile.check()
            }
        }

        override fun onCheckedChanged(value: Boolean) {
            selectedCb.isChecked = value
        }

        fun getExportProfile(): ExportItem {
            return mExportProfile
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileImportItemHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.profile_export_item, parent, false)
        return ProfileImportItemHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileImportItemHolder, position: Int) {
        val exportProfile: ExportItem = mProfiles[position]
        exportProfile.setOnCheckedChangedListener(holder)
        holder.setData(exportProfile)
    }

    override fun onViewRecycled(holder: ProfileImportItemHolder) {
        holder.getExportProfile().setOnCheckedChangedListener(null)
    }

    override fun getItemCount(): Int {
        return mProfiles.size
    }

    fun setProfiles(profiles: List<AccaProfile>) {
        // Create ExportProfile list based off AccaProfile list provided
        // Clear existing profiles
        mProfiles.clear()

        for (profile in profiles) {
            var exPro = ExportItem(profile, profile.profileName)
            mProfiles.add(exPro)
        }

        notifyDataSetChanged()
    }

    fun getExports(): ArrayList<ExportItem> {
        return mProfiles
    }
}