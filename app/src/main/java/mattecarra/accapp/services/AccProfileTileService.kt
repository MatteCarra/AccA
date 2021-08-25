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
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.acc.ConfigUpdaterEnable
import mattecarra.accapp.fragments.ProfilesViewModel
import mattecarra.accapp.utils.ProfileUtils
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

@TargetApi(Build.VERSION_CODES.N)
class AccProfileTileService: TileService() {
    private val LOG_TAG = "AccProfileTileService"
    private lateinit var profilesViewModel: ProfilesViewModel

    override fun onCreate() {
        super.onCreate()

        profilesViewModel = ProfilesViewModel(application)
        profilesViewModel.getProfiles().observeForever(Observer {
            updateTile()
        })
    }

    private fun updateTile() {
        val tile = qsTile

        val profiles = profilesViewModel.getProfiles().value
        if(profiles?.isNotEmpty() == true) {
            val profileId = ProfileUtils.getCurrentProfile(PreferenceManager.getDefaultSharedPreferences(this))
            val currProfile = if(profileId != -1) profiles.find { it.uid == profileId } else null
            if(currProfile != null) {
                tile.label =  getString(R.string.profile_tile_label, currProfile.profileName)
                tile.state =  Tile.STATE_ACTIVE
            } else {
                tile.label = getString(R.string.profile_not_selected)
                tile.state =  Tile.STATE_INACTIVE
            }
            tile.icon = Icon.createWithResource(this, R.drawable.ic_battery_charging_80) //use acc icon once ready
        } else {
            tile.label = getString(R.string.no_profiles)
            tile.state =  Tile.STATE_UNAVAILABLE
            tile.icon = Icon.createWithResource(this, R.drawable.ic_battery_charging_full) //use acc icon once ready
        }

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

    //Get profiles list and increment current profile of one unit.
    override fun onClick() {
        super.onClick()

        val mSharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        profilesViewModel.getProfiles().value?.let { profileList ->
            val currentProfile = ProfileUtils.getCurrentProfile(mSharedPrefs)

            var index = profileList.indexOfFirst { it.uid ==  currentProfile} + 1
            if(index >= profileList.size)
                index = 0

            val profile = profileList[index]

            //apply profile
            doAsync {
                val res = runBlocking { Acc.instance.updateAccConfig(profile.accConfig, ConfigUpdaterEnable(mSharedPrefs)) }

                if(!res.isSuccessful()) {
                    res.debug()

                    uiThread {
                        //Update tile infos
                        qsTile.state =  Tile.STATE_ACTIVE
                        qsTile.label =  getString(R.string.error_occurred)
                        qsTile.updateTile()
                    }
                } else {
                    uiThread {
                        //Update tile infos
                        qsTile.state =  Tile.STATE_ACTIVE
                        qsTile.label =  getString(R.string.profile_tile_label, profile.profileName)
                        qsTile.updateTile()
                    }
                }

                ProfileUtils.saveCurrentProfile(profile.uid, mSharedPrefs)
            }
        }
    }
}