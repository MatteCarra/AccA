package mattecarra.accapp

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import mattecarra.accapp.fragments.SettingsFragment.Companion.ACC_VERSION
import mattecarra.accapp.fragments.SettingsFragment.Companion.CURRENT_UNIT_OF_MEASURE
import mattecarra.accapp.fragments.SettingsFragment.Companion.VOLTAGE_UNIT_OF_MEASURE

class Preferences(context: Context) {
    val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var uACurrent: Boolean?
        get() = sharedPrefs.getString(CURRENT_UNIT_OF_MEASURE, null)?.let { it == "uA" }
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putString(CURRENT_UNIT_OF_MEASURE, value?.let { if(it) "uA" else "mA" })
            editor.apply()
        }

    var uVMeasureUnit: Boolean?
        get() = sharedPrefs.getString(VOLTAGE_UNIT_OF_MEASURE, null)?.let { it == "uV" }
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putString(VOLTAGE_UNIT_OF_MEASURE, value?.let { if(it) "uV" else "mV" })
            editor.apply()
        }

    var accVersion: String
        get() = sharedPrefs.getString(ACC_VERSION, "bundled") ?: "bundled"
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putString(ACC_VERSION, value)
            editor.apply()
        }
}