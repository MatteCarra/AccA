package mattecarra.accapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

/**
 * Data class for AccConfig.
 * @param configResetUnplugged Reset the battery stats upon unplugging the device.
 * @param configChargeSwitch changes the charge switch file.
 */
@Parcelize
data class AccConfig(var configCapacity: @RawValue ConfigCapacity,
                     var configVoltage: @RawValue ConfigVoltage,
                     var configTemperature: @RawValue ConfigTemperature,
                     var configOnBootExit: Boolean,
                     var configOnBoot: String?,
                     var configOnPlug: String?,
                     var configCoolDown: @RawValue ConfigCoolDown,
                     var configResetUnplugged: Boolean,
                     var configChargeSwitch: String?) : Parcelable {

    /**
     * Capacity Configuration
     * @param shutdown percentage when the device will be shutdown.
     * @param resume percentage when charging should resume.
     * @param pause percentage when charging should be paused.
     */
    data class ConfigCapacity (var shutdown: Int, var resume: Int, var pause: Int)

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
    data class ConfigTemperature (var coolDownTemperature: Int, var maxTemperature: Int, var pause: Int)


    /**
     * Cool Down configuration
     * @param atPercent coolDown starts at the specified percent.
     * @param charge charge time in seconds.
     * @param pause pause time in seconds.
     */
    data class ConfigCoolDown (var atPercent: Int, var charge: Int?, var pause: Int?)

}