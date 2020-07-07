package mattecarra.accapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp._interface.OnExportItemCheckedListener
import mattecarra.accapp.models.ExportItem

// Lambda functions for replacing interfaces: https://medium.com/@ddst/passing-lambda-function-for-adapter-callback-in-kotlin-6c9552af7262

class ProfileExportAdapter(val callback: OnExportItemCheckedListener): RecyclerView.Adapter<ProfileExportAdapter.ProfileExportItemHolder>() {

    private var mProfiles: List<ExportItem> = ArrayList()

    inner class ProfileExportItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ExportItem.Listener, View.OnClickListener {

        lateinit var mExportProfile: ExportItem
        private val mCallback: OnExportItemCheckedListener = callback

        private val itemRl: RelativeLayout = itemView.findViewById(R.id.pex_item_rl)
        private val titleTv: TextView = itemView.findViewById(R.id.pex_item_name_tv)
        private val selectedCb: CheckBox = itemView.findViewById(R.id.pex_item_checkbox)

        init {
            itemRl.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
//            mProfiles[adapterPosition].check()
            // use callback
            mCallback.onExportItemSelected(adapterPosition)
        }

        fun setData(exportItem: ExportItem) {
            mExportProfile = exportItem
            titleTv.text = exportItem.getName()
        }

        override fun onCheckedChanged(value: Boolean) {
            selectedCb.isChecked = value
        }

        fun getExportProfile(): ExportItem {
            return mExportProfile
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileExportItemHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.profile_export_item, parent, false)
        return ProfileExportItemHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileExportItemHolder, position: Int) {
        val exportProfile: ExportItem = mProfiles[position]
        exportProfile.setOnCheckedChangedListener(holder)
        holder.setData(exportProfile)
    }

    override fun onViewRecycled(holder: ProfileExportItemHolder) {
        holder.getExportProfile().setOnCheckedChangedListener(null)
    }

    override fun getItemCount(): Int {
        return mProfiles.size
    }

    fun setProfiles(profiles: List<ExportItem>) {
        // Create ExportProfile list based off AccaProfile list provided
        mProfiles = profiles
        notifyDataSetChanged()
    }

    fun getExports(): List<ExportItem> {
        return mProfiles
    }
}