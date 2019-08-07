package mattecarra.accapp._interface

import mattecarra.accapp.models.AccaProfile

/**
 * Interface for handling OnClick with a Profile
 */
interface OnProfileClickListener {

    fun onProfileClick(profile: AccaProfile)
    fun onProfileLongClick(profile: AccaProfile)
    fun onProfileOptionsClick(profile: AccaProfile)
    fun editProfile(profile: AccaProfile)
    fun renameProfile(profile: AccaProfile)
    fun deleteProfile(profile: AccaProfile)
}