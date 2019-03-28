package mattecarra.accapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp._interface.OnProfileClickListener
import mattecarra.accapp.models.AccaProfile

class ProfileListAdapter internal constructor(context: Context) : RecyclerView.Adapter<ProfileListAdapter.ProfileViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mProfilesList = emptyList<AccaProfile>()
    private lateinit var mListener: OnProfileClickListener

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

        val profile = mProfilesList[position]
        holder.profileTitleItemView.text = profile.profileName
        holder.profileCapacityTextView.text = profile.accConfig.configCoolDown.atPercent.toString()
    }

    internal fun setProfiles(profiles: List<AccaProfile>) {

        mProfilesList = profiles
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {

        return mProfilesList.size
    }

    /**
     * Set the OnProfileClickListener, the parent must implement the interface.
     */
    fun setOnClickListener(profileClickListener: OnProfileClickListener) {
        mListener = profileClickListener
    }
}