package mattecarra.accapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import mattecarra.accapp.database.AccaRoomDatabase
import mattecarra.accapp.models.AccaProfile
import java.io.File

object ProfileUtils {

    fun getCurrentProfile(sharedPrefs: SharedPreferences): String? {
        return sharedPrefs.getString("PROFILE", null)
    }

    fun saveCurrentProfile(profile: String?, sharedPrefs: SharedPreferences) {
        val editor = sharedPrefs.edit()
        editor.putString("PROFILE", profile)
        editor.apply()
    }

    fun listProfiles(context: Context, gson: Gson = Gson()): List<String> {
        val profiles = File(context.filesDir, "profiles")

        return if(!profiles.exists())
                emptyList()
            else
                gson.fromJson(profiles.readText(), Array<String>::class.java).toList()

    }

    fun writeProfiles(context: Context, profiles: List<String>, gson: Gson = Gson()) {
        File(context.filesDir, "profiles").writeText(gson.toJson(profiles))
    }

//    fun readProfile(profileToApply: String, context: Context, gson: Gson = Gson()): AccConfig {
//        val file = File(context.filesDir, "$profileToApply.profile")
//        return gson.fromJson(file.readText(), AccConfig::class.java)
//    }
}