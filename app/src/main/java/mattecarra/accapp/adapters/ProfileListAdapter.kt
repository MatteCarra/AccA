package mattecarra.accapp.adapters

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp._interface.OnProfileClickListener
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.Constants
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ProfileListAdapter internal constructor(context: Context) : RecyclerView.Adapter<ProfileListAdapter.ProfileViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mProfilesList = emptyList<AccaProfile>()
    private lateinit var mListener: OnProfileClickListener
    private val mContext = context

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener  {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key.equals(Constants.PROFILE_KEY)) {

                doAsync {
                    val profileId = sharedPreferences!!.getInt(key, -1)

                    uiThread {
                        if (adapterPosition != -1) {
                            if (mProfilesList[adapterPosition].uid == profileId) {
                                selectedView.visibility = View.VISIBLE
                            } else {
                                // Hide the selectedView
                                selectedView.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

        override fun onClick(v: View?) {
            mListener.onProfileClick(mProfilesList[adapterPosition])
        }

        override fun onLongClick(v: View?): Boolean {
            mListener.onProfileLongClick(mProfilesList[adapterPosition])
            return true
        }

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(itemView.context)
            prefs.registerOnSharedPreferenceChangeListener(this)

            onSharedPreferenceChanged(prefs, Constants.PROFILE_KEY)
        }

        val selectedView: View = itemView.findViewById(R.id.item_profile_selectedIndicator_view)
        val titleTv: TextView = itemView.findViewById(R.id.item_profile_title_textView)
        val capacityTv: TextView = itemView.findViewById(R.id.item_profile_capacity_tv)
        val temperatureTv: TextView = itemView.findViewById(R.id.item_profile_temperature_tv)
        val onPlugTv: TextView = itemView.findViewById(R.id.item_profile_onplug_tv)
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

        val profileId = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(Constants.PROFILE_KEY, -1)
        if (profile.uid == profileId) {
            // Make visible
            holder.selectedView.visibility = View.VISIBLE
        } else {
            holder.selectedView.visibility = View.INVISIBLE
        }
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