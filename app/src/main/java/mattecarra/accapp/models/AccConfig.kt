package mattecarra.accapp.models

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import mattecarra.accapp.MainApplication
import mattecarra.accapp.R

/**
 * Data class for AccConfig.
 * @param configResetUnplugged Reset the battery stats upon unplugging the device.
 * @param configChargeSwitch changes the charge switch file.
 */
@Parcelize
data class AccConfig(var configCapacity: ConfigCapacity,
                     var configVoltage: ConfigVoltage,
                     var configCurrMax: Int?,
                     var configTemperature: ConfigTemperature,
                     var configOnBoot: String?,
                     var configOnPlug: String?,
                     var configCoolDown: ConfigCoolDown?,
                     var configResetUnplugged: Boolean,
                     var configResetBsOnPause: Boolean,
                     var configChargeSwitch: String?,
                     var prioritizeBatteryIdleMode: Boolean) : Parcelable {

    private companion object : Parceler<AccConfig> {

        override fun create(parcel: Parcel): AccConfig {
            return Gson().fromJson(parcel.readString(), AccConfig::class.java)
        }

        override fun AccConfig.write(parcel: Parcel, flags: Int) {
            // Convert this to a GSON string
            parcel.writeString(Gson().toJson(this))
        }
    }

    fun getOnPlug(context: Context): String {
        return if (configOnPlug.isNullOrBlank()) {
            context.getString(R.string.voltage_control_file_not_set)
        } else {
            configOnPlug as String
        }
    }

    /**
     * Capacity Configuration
     * @param shutdown percentage when the device will be shutdown.
     * @param resume percentage when charging should resume.
     * @param pause percentage when charging should be paused.
     */
//    data class ConfigCapacity (var shutdown: Int, var resume: Int, var pause: Int)

    data class ConfigCapacity(var shutdown: Int, var resume: Int, var pause: Int) {
        fun toString(context: Context): String {
            return String.format(context.getString(R.string.template_capacity_profile), shutdown, resume, pause)
        }
    }

    /**
     * Voltage Configuration
     * @param controlFile path to the device's voltage control file.
     * @param max the max voltage the device should take from the charger.
     */
    data class ConfigVoltage (var controlFile: String?, var max: Int?)

    /**
     * Temperature Configuration
     * @param coolDownTemperature percentage when the cool down phase should start.
     * @param maxTemperature maximum temperature of the battery while charging. When met, charging will pause for <pause> seconds.
     * @param pause time in seconds to wait for the temperature to drop below <max>, to resume charging.
     */
    data class ConfigTemperature (var coolDownTemperature: Int, var maxTemperature: Int, var pause: Int) {

        fun toString(context: Context): String {
            return String.format(context.getString(R.string.template_temperature_profile, coolDownTemperature, maxTemperature, pause))
        }
    }


    /**
     * Cool Down configuration
     * @param atPercent coolDown starts at the specified percent.
     * @param charge charge time in seconds.
     * @param pause pause time in seconds.
     */
    data class ConfigCoolDown (var atPercent: Int, var charge: Int, var pause: Int) {
        fun toString(context: Context): String {
            return String.format(context.getString(R.string.template_cool_down_profile,
                atPercent, charge, pause))
        }
    }

}