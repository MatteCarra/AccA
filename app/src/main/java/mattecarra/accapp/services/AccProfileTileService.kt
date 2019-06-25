package mattecarra.accapp.services

import android.Manifest
import android.annotation.TargetApi
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import mattecarra.accapp.R
import mattecarra.accapp.utils.ProfileUtils
import org.jetbrains.anko.doAsync
import java.io.File

@TargetApi(Build.VERSION_CODES.N)
class AccProfileTileService: TileService() {
    private val LOG_TAG = "AccProfileTileService"

    private fun updateTile() {
        val tile = qsTile

        //TODO check if there is any profile
        /*if(isThereAnyProfile()) {
            tile.label = getString(R.string.no_profiles)
            tile.state =  Tile.STATE_UNAVAILABLE
            tile.icon = Icon.createWithResource(this, R.drawable.ic_battery_charging_full) //use acc icon once ready
        } else {
            val profileId = ProfileUtils.getCurrentProfile(PreferenceManager.getDefaultSharedPreferences(this))
            if(profileId != -1) {
                tile.label =  getString(R.string.profile_tile_label, profileId.toString()) //TODO get profile name from profileId
                tile.state =  Tile.STATE_ACTIVE
            } else {
                tile.label = getString(R.string.profile_not_selected)
                tile.state =  Tile.STATE_INACTIVE
            }
            tile.icon = Icon.createWithResource(this, R.drawable.ic_battery_charging_80) //use acc icon once ready
        }*/

        tile.updateTile()

    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        //TODO: Implement profile incrementer
        //Get profiles list and increment current profile of one unit.

        /* TODO: Adjust profile application function

            val gson = Gson()
            val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

            val profileList = ProfileUtils.listProfiles(this, gson)

            val currentProfile = ProfileUtils.getCurrentProfile(sharedPrefs)

            var index = (currentProfile?.let { profileList.indexOf(it) } ?: -1) + 1
            if(index >= profileList.size)
                index = 0

            val profileToApply = profileList[index]

            //apply profile
            val profileConfig = ProfileUtils.readProfile(profileToApply, this, gson)

            doAsync {
                profileConfig.updateAcc()

                ProfileUtils.saveCurrentProfile(profileToApply, sharedPrefs)
            }

            //Update tile infos
            qsTile.state =  Tile.STATE_ACTIVE
            qsTile.label =  getString(R.string.profile_tile_label, profileToApply)
            qsTile.updateTile()
        */
    }
}