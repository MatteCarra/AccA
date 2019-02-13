package mattecarra.accapp.adapters

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.parcel.Parcelize
import mattecarra.accapp.R
import java.util.ArrayList

@Parcelize
data class Profile(val profileName: String): Parcelable

class ProfilesViewAdapter(val profiles: ArrayList<Profile>, var selectedProfile: String?, private val listener: (Profile, Boolean) -> Unit) : RecyclerView.Adapter<ProfilesViewAdapter.ProfileViewHolder>() {

    fun saveState(bundle: Bundle) {
        bundle.putParcelableArrayList("profiles", profiles)
    }

    fun restoreState(bundle: Bundle) {
        bundle.getParcelableArrayList<Profile>("profiles")?.let {
            profiles.addAll(it)
        }
    }

    fun add(profile: Profile) {
        profiles.add(profile)
        notifyItemInserted(profiles.size - 1)
    }

    fun remove(profile: Profile) {
        val index = profiles.indexOf(profile)
        if(index != -1) {
            profiles.removeAt(index)
            notifyItemRemoved(profiles.size - 1)
        }
    }

    fun getItem(index: Int): Profile {
        return profiles[index]
    }

    fun size(): Int {
        return profiles.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilesViewAdapter.ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_row, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilesViewAdapter.ProfileViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, item.profileName == selectedProfile, listener)
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    inner class ProfileViewHolder(internal var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(profile: Profile, isSelected: Boolean, listener: (Profile, Boolean) -> Unit) = with(itemView) {
            val button = view.findViewById<Button>(R.id.profile_text_view)
            button.text = profile.profileName
            view.isSelected = isSelected

            button.setOnClickListener {
                selectedProfile = profile.profileName
                listener(profile, false)
            }
            button.setOnLongClickListener {
                listener(profile, true)
                true //consumed long click
            }

        }
    }
}