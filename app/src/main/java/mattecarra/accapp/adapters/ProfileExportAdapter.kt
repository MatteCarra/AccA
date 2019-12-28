package mattecarra.accapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import mattecarra.accapp.R
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.RowProfileExportData

class ProfileExportAdapter(private val context: Context, profiles: List<AccaProfile>, checkedChanged: CompoundButton.OnCheckedChangeListener) :
    BaseAdapter() {

    var mProfileExportList: ArrayList<RowProfileExportData>
    var mCheckedChangeListener: CompoundButton.OnCheckedChangeListener

    init {
        // Create new RowProfileExportData objexts and assign list
        val profileExportList: ArrayList<RowProfileExportData> = ArrayList()
        for (profile: AccaProfile in profiles) {
             profileExportList.add(RowProfileExportData(profile, profile.profileName))
        }
        mProfileExportList = profileExportList
        mCheckedChangeListener = checkedChanged
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView
        val holder: RowViewHolder

        val exportProfile = mProfileExportList[position]

        if (convertView == null) {
            holder = RowViewHolder()
            convertView = LayoutInflater.from(context).inflate(R.layout.profile_export_item, null, true)

            holder.mCheckBox = convertView!!.findViewById(R.id.pex_item_checkbox)
            holder.mProfile = exportProfile.getProfile()
            holder.mTitle = convertView!!.findViewById(R.id.pex_item_name_tv)
            holder.mListener = exportProfile.getListener()

            convertView.tag = holder
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = convertView.tag as RowViewHolder
        }

        convertView.setOnClickListener(holder.mListener)

        holder.mTitle.text = holder.mProfile.profileName
        holder.mCheckBox.isSelected = exportProfile.getCheckState()
        holder.mCheckBox.setOnClickListener(exportProfile.getListener())
        holder.mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener)

        return convertView
    }

    override fun getItem(id: Int): Any {
        return mProfileExportList[id]
    }

    override fun getCount(): Int {
        return mProfileExportList.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    private inner class RowViewHolder {
        lateinit var mTitle: TextView
        lateinit var mProfile: AccaProfile
        lateinit var mCheckBox: CheckBox
        lateinit var mListener: View.OnClickListener
    }
}