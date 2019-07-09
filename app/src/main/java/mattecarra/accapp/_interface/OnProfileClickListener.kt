package mattecarra.accapp._interface

import mattecarra.accapp.models.AccaProfile

/**
 * Interface for handling OnClick with a Profile
 */
interface OnProfileClickListener {

    fun onProfileClick(accaProfile: AccaProfile)
    fun onProfileLongClick(accaProfile: AccaProfile)
    fun onProfileOptionsClick(accaProfile: AccaProfile)
}