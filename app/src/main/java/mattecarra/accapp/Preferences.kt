package mattecarra.accapp

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import mattecarra.accapp.fragments.SettingsFragment.Companion.CURRENT_UNIT_OF_MEASURE
import mattecarra.accapp.fragments.SettingsFragment.Companion.VOLTAGE_UNIT_OF_MEASURE

class Preferences(context: Context) {
    val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var uAhCurrent: Boolean?
        get() = sharedPrefs.getString(CURRENT_UNIT_OF_MEASURE, null)?.toIntOrNull() == 1
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putString(CURRENT_UNIT_OF_MEASURE, value?.let { if(it) "1" else "0" })
            editor.apply()
        }

    var uVMeasureUnit: Boolean?
        get() = sharedPrefs.getString(VOLTAGE_UNIT_OF_MEASURE, null)?.toIntOrNull() == 1
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putString(VOLTAGE_UNIT_OF_MEASURE, value?.let { if(it) "1" else "0" })
            editor.apply()
        }
}