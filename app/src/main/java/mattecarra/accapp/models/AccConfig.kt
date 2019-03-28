package mattecarra.accapp.models

data class AccConfig(var configCapacity: ConfigCapacity,
                     var configVoltage: ConfigVoltage,
                     var configTemperature: ConfigTemperature,
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
    data class ConfigVoltage (var controlFile: String?, var max: Int)

    /**
     * Temperature Configuration
     * @param cooldown percentage when the cool down phase should start.
     * @param max maximum temperature of the battery while charging. When met, charging will pause for <pause> seconds.
     * @param pause time in seconds to wait for the temperature to drop below <max>, to resume charging.
     */
    data class ConfigTemperature (var cooldown: Int, var max: Int, var pause: Int)

    /**
     * OnBoot Configuration
     * @param enabled is the OnBoot option enabled.
     * @param command the command which is run on boot if <OnBoot> is enabled.
     */
    data class ConfigOnBoot (var enabled: Boolean, var command: String?)

    /**
     * OnPlug Configuration
     * @param ConfigOnPlug the command to run when the device is plugged in.
     */
    data class ConfigOnPlug (var command: String?)

    /**
     * Cool Down configuration
     * @param atPercent cooldown starts at the specified percent.
     * @param charge charge time in seconds.
     * @param pause pause time in seconds.
     */
    data class ConfigCoolDown (var atPercent: Int, var charge: Int, var pause: Int)

}