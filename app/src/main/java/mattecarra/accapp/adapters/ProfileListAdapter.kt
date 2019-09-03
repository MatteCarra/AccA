package mattecarra.accapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp._interface.OnProfileClickListener
import mattecarra.accapp.models.AccaProfile

class ProfileListAdapter internal constructor(context: Context, activeProfileId: Int) :
    RecyclerView.Adapter<ProfileListAdapter.ProfileViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mProfilesList = emptyList<AccaProfile>()
    private lateinit var mListener: OnProfileClickListener
    private val mContext = context

    private var mActiveProfileId: Int = activeProfileId

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val selectedView: View = itemView.findViewById(R.id.item_profile_selectedIndicator_view)
        val titleTv: TextView = itemView.findViewById(R.id.item_profile_title_textView)
        val capacityTv: TextView = itemView.findViewById(R.id.item_profile_capacity_tv)
        val temperatureTv: TextView = itemView.findViewById(R.id.item_profile_temperature_tv)
        val onPlugTv: TextView = itemView.findViewById(R.id.item_profile_on_plug_tv)
        val optionsIb: ImageButton = itemView.findViewById(R.id.item_profile_options_ib)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            mListener.onProfileClick(mProfilesList[adapterPosition])
        }

        override fun onLongClick(v: View?): Boolean {
            mListener.onProfileLongClick(mProfilesList[adapterPosition])
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val itemView = mInflater.inflate(R.layout.profiles_item, parent, false)
        return ProfileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = mProfilesList[position]
        holder.titleTv.text = profile.profileName
        holder.capacityTv.text = profile.accConfig.configCapacity.toString(mContext)
        holder.temperatureTv.text = profile.accConfig.configTemperature.toString(mContext)
        holder.onPlugTv.text = profile.accConfig.getOnPlug(mContext)

        holder.optionsIb.setOnClickListener {
            with(PopupMenu(mContext, holder.optionsIb)) {
                menuInflater.inflate(R.menu.profiles_options_menu, this.menu)

                setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.profile_option_menu_edit -> {
                                mListener.editProfile(mProfilesList[position])
                            }
                            R.id.profile_option_menu_rename -> {
                                mListener.renameProfile(mProfilesList[position])
                            }
                            R.id.profile_option_menu_delete -> {
                                mListener.deleteProfile(mProfilesList[position])
                            }
                        }
                        true
                    }

                show()
            }
        }

        if (profile.uid == mActiveProfileId) {
            // Make visible
            holder.selectedView.visibility = View.VISIBLE
        } else {
            // Hide the selectedView
            holder.selectedView.visibility = View.GONE
        }
    }

    internal fun setActiveProfile(id: Int) {
        mActiveProfileId = id
        notifyDataSetChanged()
    }

    internal fun setProfiles(profiles: List<AccaProfile>) {
        mProfilesList = profiles
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mProfilesList.size
    }

    fun getProfileAt(pos: Int): AccaProfile {
        return mProfilesList[pos]
    }

    /**
     * Set the OnProfileClickListener, the parent must implement the interface.
     */
    fun setOnClickListener(profileClickListener: OnProfileClickListener) {
        mListener = profileClickListener
    }
}