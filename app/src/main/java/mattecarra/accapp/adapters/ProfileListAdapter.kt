package mattecarra.accapp.adapters

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.opengl.Visibility
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

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener, SharedPreferences.OnSharedPreferenceChangeListener{

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key.equals(Constants.PROFILE_KEY)) {

                doAsync {
                    val profileId = sharedPreferences!!.getInt(key, -1)

                    uiThread {
                        if (mProfilesList[adapterPosition].uid == profileId) {
                            profileSelectedView.visibility = View.VISIBLE
                        } else {
                            // Hide the profileSelectedView
                            profileSelectedView.visibility = View.GONE
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
        holder.profileCapacityTextView.text = profile.accConfig.configCapacity.pause.toString()
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