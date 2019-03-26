package mattecarra.accapp.`interface`

import mattecarra.accapp.models.ProfileEntity

/**
 * Interface for handling OnClick with a Profile
 */
interface OnProfileClickListener {

    fun onProfileClick(profileEntity: ProfileEntity)

}