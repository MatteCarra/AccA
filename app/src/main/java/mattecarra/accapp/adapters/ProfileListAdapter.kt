package mattecarra.accapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageButton
import android.widget.LinearLayout
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

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener
    {
        val selectedView: View = itemView.findViewById(R.id.item_profile_selectedIndicator_view)
        val titleTv: TextView = itemView.findViewById(R.id.item_profile_title_textView)
        val capacityLL: LinearLayout = itemView.findViewById(R.id.item_profile_capacity_ll)
        val capacityTv: TextView = itemView.findViewById(R.id.item_profile_capacity_tv)
        val chargingVoltLL: LinearLayout = itemView.findViewById(R.id.item_profile_charging_voltage_ll)
        val chargingVoltTv: TextView = itemView.findViewById(R.id.item_profile_charging_voltage_tv)
        val temperatureLL: LinearLayout = itemView.findViewById(R.id.item_profile_temperature_ll)
        val temperatureTv: TextView = itemView.findViewById(R.id.item_profile_temperature_tv)
        val coolDownLL: LinearLayout = itemView.findViewById(R.id.item_profile_cooldown_ll)
        val coolDownTv: TextView = itemView.findViewById(R.id.item_profile_cooldown_tv)
        val onBootLL: LinearLayout = itemView.findViewById(R.id.item_profile_on_boot_ll)
        val onBootTv: TextView = itemView.findViewById(R.id.item_profile_on_boot_tv)
        val onPlugLL: LinearLayout = itemView.findViewById(R.id.item_profile_on_plug_ll)
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

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int)
    {
        val profile = mProfilesList[position]

        holder.titleTv.text = profile.profileName
        holder.capacityTv.text = profile.accConfig.configCapacity.toString(mContext)

        // if on\off getting from AccConfigEditorActivity :: 168
        // todo prop temperatureTv on\off no exist in table\data ... coolDownLL too
        holder.temperatureLL.visibility = if(profile.accConfig.configTemperature.coolDownTemperature >= 90 && profile.accConfig.configTemperature.maxTemperature >= 95) View.GONE else View.VISIBLE;
        holder.temperatureTv.text = profile.accConfig.configTemperature.toString(mContext)

        // todo add and integrate current
        holder.chargingVoltLL.visibility = if (profile.accConfig.configVoltage.controlFile == null && profile.accConfig.configVoltage.max == null) View.GONE else View.VISIBLE;
        holder.chargingVoltTv.text = profile.accConfig.configVoltage.toString()

        // if on\off getting from AccConfigEditorActivity :: 196
        holder.coolDownLL.visibility = if(profile.accConfig.configCoolDown == null || profile.accConfig.configCoolDown!!.atPercent > 100)  View.GONE else View.VISIBLE;
        holder.coolDownTv.text = profile.accConfig.configCoolDown?.toString(mContext)

        holder.onBootLL.visibility = if (profile.accConfig.configOnBoot == null) View.GONE else View.VISIBLE
        holder.onBootTv.text = profile.accConfig.configOnBoot

        holder.onPlugLL.visibility = if (profile.accConfig.configOnPlug == null) View.GONE else View.VISIBLE
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