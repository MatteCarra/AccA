package mattecarra.accapp

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import mattecarra.accapp.djs.Djs
import mattecarra.accapp.utils.Constants.ACC_VERSION
import mattecarra.accapp.utils.Constants.CURRENT_UNIT_OF_MEASURE
import mattecarra.accapp.utils.Constants.DJS_ENABLED
import mattecarra.accapp.utils.Constants.THEME
import mattecarra.accapp.utils.Constants.VOLTAGE_UNIT_OF_MEASURE


enum class CurrentUnit { uA, mA, A }
enum class VoltageUnit { uV, mV, V }

class Preferences(private val context: Context) {
    private val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var currentUnitOfMeasure: CurrentUnit
        get() = sharedPrefs.getString(CURRENT_UNIT_OF_MEASURE, null)?.let {
            when(it) {
                "uA" -> CurrentUnit.uA
                "mA" -> CurrentUnit.mA
                else -> CurrentUnit.A
            }
        } ?: CurrentUnit.A
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putString(CURRENT_UNIT_OF_MEASURE, when(value) {
                    CurrentUnit.uA -> "uA"
                    CurrentUnit.mA -> "mA"
                    CurrentUnit.A ->  "A"
                }
            )
            editor.apply()
        }

    var voltageUnitOfMeasure: VoltageUnit
        get() = sharedPrefs.getString(VOLTAGE_UNIT_OF_MEASURE, null)?.let {
            when(it) {
                "uV" -> VoltageUnit.uV
                "mV" -> VoltageUnit.mV
                else -> VoltageUnit.V
            }
        } ?: VoltageUnit.V
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putString(VOLTAGE_UNIT_OF_MEASURE, when(value) {
                VoltageUnit.uV -> "uV"
                VoltageUnit.mV -> "mV"
                VoltageUnit.V -> "V"
            })
            editor.apply()
        }

    var accVersion: String
        get() = sharedPrefs.getString(ACC_VERSION, "bundled") ?: "bundled"
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putString(ACC_VERSION, value)
            editor.apply()
        }

    var appTheme: String?
        get() = sharedPrefs.getString(THEME, "2")
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putString(THEME, value ?: "2")
            editor.apply()
        }

    var lastUpdateCheck: Long
        get() = sharedPrefs.getLong("LAST_UPDATE_CHECK", -1)
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putLong("LAST_UPDATE_CHECK", value)
            editor.apply()
        }

    var lastCommit: String?
        get() = sharedPrefs.getString("LAST_COMMIT", null)
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putString("LAST_COMMIT", value)
            editor.apply()
        }

    var djsEnabled: Boolean
        get() = sharedPrefs.getBoolean(DJS_ENABLED, false) && Djs.isDjsInstalled()
        set(value) {
            val editor = sharedPrefs.edit()
            editor.putBoolean(DJS_ENABLED, value)
            editor.apply()
        }
}