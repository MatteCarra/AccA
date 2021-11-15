package mattecarra.accapp.utils

import android.content.SharedPreferences
import mattecarra.accapp.utils.Constants.PROFILE_KEY

object ProfileUtils {

    fun getCurrentProfile(sharedPrefs: SharedPreferences): Int
    {
        sharedPrefs.getInt(PROFILE_KEY, -1).also {
            LogExt().d(javaClass.simpleName, "getCurrentProfile()=$it")
            return it
        }
    }

    fun saveCurrentProfile(profileId: Int, sharedPrefs: SharedPreferences)
    {
        LogExt().d(javaClass.simpleName,"saveCurrentProfile($profileId)")
        sharedPrefs.edit().putInt(PROFILE_KEY, profileId).apply()
    }

    fun clearCurrentSelectedProfile(sharedPrefs: SharedPreferences)
    {
        LogExt().d(javaClass.simpleName,"clearCurrentSelectedProfile(-1)")
        sharedPrefs.edit().putInt(PROFILE_KEY, -1).apply()
    }
}