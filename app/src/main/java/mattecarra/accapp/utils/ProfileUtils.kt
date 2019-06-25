package mattecarra.accapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.models.AccaProfile
import java.io.File

object ProfileUtils {

    fun getCurrentProfile(sharedPrefs: SharedPreferences): Int {
        return sharedPrefs.getInt(Constants.PROFILE_KEY, -1)
    }

    fun saveCurrentProfile(profileId: Int, sharedPrefs: SharedPreferences) {
        sharedPrefs.edit().putInt(Constants.PROFILE_KEY, profileId).apply()
    }

    fun clearCurrentSelectedProfile(sharedPrefs: SharedPreferences) {
        sharedPrefs.edit().putInt(Constants.PROFILE_KEY, -1).apply()
    }
}