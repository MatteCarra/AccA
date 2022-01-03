package mattecarra.accapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp._interface.OnProfileClickListener
import mattecarra.accapp.databinding.ProfilesItemBinding
import mattecarra.accapp.models.AccaProfile

class ProfileListAdapter internal constructor(context: Context, activeProfileId: Int) :
    RecyclerView.Adapter<ProfileListAdapter.ProfileViewHolder>()
{
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var mListener: OnProfileClickListener
    private val mContext = context

    private var mProfilesList = emptyList<AccaProfile>()
    private var mActiveProfileId: Int = activeProfileId

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener
    {
        val content = ProfilesItemBinding.bind(itemView)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?)
        {
            mListener.onProfileClick(mProfilesList[adapterPosition])
        }

        override fun onLongClick(v: View?): Boolean
        {
            //  mListener.onProfileLongClick(mProfilesList[adapterPosition])
            mListener.editProfile(mProfilesList[adapterPosition])
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder
    {
        val itemView = mInflater.inflate(R.layout.profiles_item, parent, false)
        return ProfileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int)
    {
        val profile = mProfilesList[position]

        holder.content.itemProfileTitleTextView.text = profile.profileName

        holder.content.itemProfileCapacityLl.isVisible = profile.pEnables.eCapacity
        holder.content.itemProfileCapacityTv.text = profile.accConfig.configCapacity.toString(mContext)

        // TODO You must make a switch as a separate item for manual mode or use the parameters from the global configuration in the settings. Here only the display of the selected option.
        holder.content.itemProfileSwitchLl.isVisible = profile.pEnables.eChargingSwitch
        holder.content.itemProfileSwitchDataTv.text = profile.accConfig.configChargeSwitch ?: mContext.getString(R.string.automatic)
        holder.content.itemProfileAutomaticSwitchingTv.visibility =
            if (profile.accConfig.configChargeSwitch.isNullOrEmpty()) View.GONE
            else if (profile.accConfig.configIsAutomaticSwitchingEnabled) View.VISIBLE else View.GONE

        //----------------------------------------------------

        holder.content.itemProfileChargingVoltageLl.isVisible = profile.pEnables.eVoltage || profile.pEnables.eCurrMax
        holder.content.itemProfileChargingVoltageTv.text = profile.accConfig.configVoltage.toString(mContext)
        holder.content.itemProfileCurrentMaxTv.text = mContext.getString(R.string.current_max) + " " + profile.accConfig.configCurrMax.toString()

        val volt = (profile.accConfig.configVoltage.controlFile != null || profile.accConfig.configVoltage.max != null)
        val currmax = profile.accConfig.configCurrMax != null

        if ((volt && !currmax) || (!volt && currmax))
        {
            holder.content.itemProfileChargingVoltageTv.isVisible = volt
            holder.content.itemProfileCurrentMaxTv.isVisible = currmax
        }

        //----------------------------------------------------

        holder.content.itemProfileTemperatureLl.isVisible = profile.pEnables.eTemperature
        holder.content.itemProfileTemperatureTv.text = profile.accConfig.configTemperature.toString(mContext)

        holder.content.itemProfileCooldownLl.isVisible = profile.pEnables.eCoolDown
        holder.content.itemProfileCooldownTv.text =
            if (profile.accConfig.configCoolDown == null) "-"
            else profile.accConfig.configCoolDown?.toString(mContext)

        holder.content.itemProfileOnBootLl.isVisible = profile.pEnables.eRunOnBoot
        holder.content.itemProfileOnBootTv.text =
            if (profile.accConfig.configOnBoot == null) "-"
            else profile.accConfig.configOnBoot

        holder.content.itemProfileOnPlugLl.isVisible = profile.pEnables.eRunOnPlug
        holder.content.itemProfileOnPlugTv.text =
            if (profile.accConfig.configOnPlug == null) "-"
            else profile.accConfig.getOnPlug(mContext)

        holder.content.itemProfilePrioritizeBatteryIdleTv.isVisible = profile.accConfig.prioritizeBatteryIdleMode
        holder.content.itemProfileResetBsOnPauseTv.isVisible = profile.accConfig.configResetBsOnPause
        holder.content.itemProfileResettUnpluggedTv.isVisible = profile.accConfig.configResetUnplugged

        holder.content.itemProfileOptionsIb.setOnClickListener {

            with(PopupMenu(mContext, holder.content.itemProfileOptionsIb))
            {
                menuInflater.inflate(R.menu.profiles_options_menu, this.menu)

                setOnMenuItemClickListener {
                    when (it.itemId)
                    {
                        R.id.profile_option_menu_edit -> mListener.editProfile(mProfilesList[position])
                        R.id.profile_option_menu_rename -> mListener.renameProfile(mProfilesList[position])
                        R.id.profile_option_menu_delete -> mListener.deleteProfile(mProfilesList[position])
                    }
                    true
                }

                show()
            }
        }

        // Make visible or Hide the selectedView
        holder.content.itemProfileSelectedIndicatorView.isVisible = profile.uid == mActiveProfileId
    }

    internal fun setActiveProfile(id: Int)
    {
        mActiveProfileId = id
        notifyDataSetChanged()
    }

    internal fun setProfiles(profiles: List<AccaProfile>)
    {
        mProfilesList = profiles
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mProfilesList.size

    fun getProfileAt(pos: Int): AccaProfile = mProfilesList[pos]

    /**
     * Set the OnProfileClickListener, the parent must implement the interface.
     */
    fun setOnClickListener(profileClickListener: OnProfileClickListener)
    {
        mListener = profileClickListener
    }
}