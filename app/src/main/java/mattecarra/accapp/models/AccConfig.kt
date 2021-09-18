package mattecarra.accapp.models

import android.content.Context
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import java.io.Serializable

/**
 * Data class for AccConfig.
 * @param configResetUnplugged Reset the battery stats upon unplugging the device.
 * @param configChargeSwitch changes the charge switch file.
 */
//@Parcelize
data class AccConfig(
    var configCapacity: ConfigCapacity = ConfigCapacity(),
    var configVoltage: ConfigVoltage = ConfigVoltage(),
    var configCurrMax: Int? = null,
    var configTemperature: ConfigTemperature = ConfigTemperature(),
    var configOnBoot: String? = null,
    var configOnPlug: String? = null,
    var configCoolDown: ConfigCoolDown? = null,
    var configResetUnplugged: Boolean = false,
    var configResetBsOnPause: Boolean = false,
    var configChargeSwitch: String? = null,
    var configIsAutomaticSwitchingEnabled: Boolean = true,
    var prioritizeBatteryIdleMode: Boolean = false
) : Serializable
{

    //    private companion object : Parceler<AccConfig> {
    ////
    ////        override fun create(parcel: Parcel): AccConfig {
    ////            return Gson().fromJson(parcel.readString(), AccConfig::class.java)
    ////        }
    ////
    ////        override fun AccConfig.write(parcel: Parcel, flags: Int) {
    ////            // Convert this to a GSON string
    ////            parcel.writeString(Gson().toJson(this))
    ////        }
    ////    }

    fun getOnPlug(context: Context): String
    {
        return if (configOnPlug.isNullOrBlank()) context.getString(R.string.voltage_control_file_not_set)
        else configOnPlug as String
    }

    /**
     * Capacity Configuration
     * @param shutdown percentage when the device will be shutdown.
     * @param resume percentage when charging should resume.
     * @param pause percentage when charging should be paused.
     */
//    data class ConfigCapacity (var shutdown: Int, var resume: Int, var pause: Int)

    data class ConfigCapacity(var shutdown: Int = 0, var resume: Int = 60, var pause: Int = 70) :
        Serializable
    {
        fun toString(context: Context): String
        {
            return String.format(
                context.getString(R.string.template_capacity_profile),
                shutdown,
                resume,
                pause
            )
        }
    }

    /**
     * Voltage Configuration
     * @param controlFile path to the device's voltage control file.
     * @param max the max voltage the device should take from the charger.
     */
    data class ConfigVoltage(var controlFile: String? = null, var max: Int? = null) : Serializable
    {
        fun toString(context: Context): String
        {
            return if (Acc.instance.version >= 202002170) context.getString(R.string.voltage_max) + " " + (max.toString()
                ?: "-")
            else context.getString(R.string.voltage_control_file) + " " + (controlFile.toString()
                ?: "-")
        }
    }

    /**
     * Temperature Configuration.
     * Default set as 40/60/90.
     * @param coolDownTemperature percentage when the cool down phase should start.
     * @param maxTemperature maximum temperature of the battery while charging. When met, charging will pause for <pause> seconds.
     * @param pause time in seconds to wait for the temperature to drop below <max>, to resume charging.
     */
    data class ConfigTemperature(
        var coolDownTemperature: Int = 40,
        var maxTemperature: Int = 60,
        var pause: Int = 90
    ) : Serializable
    {
        fun toString(context: Context): String
        {
            return String.format(
                context.getString(
                    R.string.template_temperature_profile,
                    coolDownTemperature,
                    maxTemperature,
                    pause
                )
            )
        }
    }

    /**
     * Cool Down configuration.
     * Default set as 60/50/10.
     * @param atPercent coolDown starts at the specified percent.
     * @param charge charge time in seconds.
     * @param pause pause time in seconds.
     */
    data class ConfigCoolDown(var atPercent: Int = 60, var charge: Int = 50, var pause: Int = 10) :
        Serializable
    {
        fun toString(context: Context): String
        {
            return context.getString(R.string.template_cool_down_profile, atPercent, charge, pause)
        }
    }
}