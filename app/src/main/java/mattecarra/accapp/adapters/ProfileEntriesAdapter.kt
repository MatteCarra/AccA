package mattecarra.accapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp.models.ProfileEntry

class ProfileEntriesAdapter() : RecyclerView.Adapter<ProfileEntryHolder>() {

    private var mEntries: MutableList<ProfileEntry>

    init {
        mEntries = ArrayList()
    }

    fun addEntry(entry: ProfileEntry) {
        mEntries.add(entry)

        var position = itemCount -1
        if (position == 0) {
            notifyDataSetChanged()
        } else {
            notifyItemInserted(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileEntryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_export_item, parent, false)
        return ProfileEntryHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileEntryHolder, position: Int) {
        val entry = mEntries.get(position)
        entry.setOnCheckedChangedListener(holder)
        holder.setData(entry)
    }

    override fun onViewRecycled(holder: ProfileEntryHolder) {
        holder.getEntry().setOnCheckedChangedListener(null)
    }

    override fun getItemCount(): Int {
        return mEntries.size
    }

    fun getCheckedEntries(): List<ProfileEntry> {
        val entries: MutableList<ProfileEntry> = ArrayList()

        for (entry: ProfileEntry in mEntries) {
            if (entry.isChecked()) {
                entries.add(entry);
            }
        }

        return entries
    }

    fun toggleCheckboxes() {
        val checkedEntries = getCheckedEntries().size
        if (checkedEntries == 0 || checkedEntries != mEntries.size) {
            setCheckboxStates(true)
        } else {
            setCheckboxStates(false)
        }
    }

    private fun setCheckboxStates(checked: Boolean) {
        for (entry: ProfileEntry in mEntries) {
            if (entry.isChecked() != checked) {
                entry.setIsChecked(checked)
            }
        }
    }

    fun clearEntries() {
        mEntries.clear()
        notifyDataSetChanged()
    }

}