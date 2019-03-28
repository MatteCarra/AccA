package mattecarra.accapp.models

/**
 * Data class for AccConfig.
 * @param resetUnplugged Reset the battery stats upon unplugging the device.
 * @param chargeSwitch changes the charge switch file.
 */
data class AccConfig(var configCapacity: ConfigCapacity,
                     var configVoltage: ConfigVoltage,
                     var configTemperature: ConfigTemperature,
                     var configOnBootExit: ConfigOnBootExit,
                     var configOnBoot: ConfigOnBoot,
                     var configOnPlug: ConfigOnPlug,
                     var configCoolDown: ConfigCoolDown,
                     var resetUnplugged: Boolean,
                     var chargeSwitch: String?) {

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
     * OnBootExit Configuration
     * @param enabled is the OnBoot option enabled.
     */
    data class ConfigOnBootExit (var enabled: Boolean)

    /**
     * OnBoot Configuration
     * @param command the command which is run on boot if <OnBoot> is enabled.
     */
    data class ConfigOnBoot (var command: String?)

    /**
     * OnPlug Configuration
     * @param ConfigOnPlug the command to run when the device is plugged in.
     */
    data class ConfigOnPlug (var command: String?)

    /**
     * Cool Down configuration
     * @param atPercent coolDown starts at the specified percent.
     * @param charge charge time in seconds.
     * @param pause pause time in seconds.
     */
    data class ConfigCoolDown (var atPercent: Int, var charge: Int, var pause: Int)

}