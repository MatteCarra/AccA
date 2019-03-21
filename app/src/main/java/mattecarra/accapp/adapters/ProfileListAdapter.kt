package mattecarra.accapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp.models.ProfileEntity

class ProfileListAdapter internal constructor(context: Context) : RecyclerView.Adapter<ProfileListAdapter.ProfileViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mProfilesList = emptyList<ProfileEntity>()

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val profileSelectedView: View = itemView.findViewById(R.id.item_profile_selectedIndicator_view)
        val profileTitleItemView: TextView = itemView.findViewById(R.id.item_profile_title_textView)
        val profileCapacityTextView: TextView = itemView.findViewById(R.id.item_profile_capacityControlValue_textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {

        val itemView = mInflater.inflate(R.layout.profiles_item, parent, false)
        return ProfileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {

        val current = mProfilesList[position]
        holder.profileTitleItemView.text =current.profileName
        holder.profileCapacityTextView.text = current.pauseCapacity.toString()
    }

    internal fun setProfiles(profiles: List<ProfileEntity>) {

        mProfilesList = profiles
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {

        return mProfilesList.size
    }
}