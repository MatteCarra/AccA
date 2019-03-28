package mattecarra.accapp.models

data class AccConfig(var configCapacity: ConfigCapacity,
                     var configVoltage: ConfigVoltage,
                     var configTemperature: ConfigTemperature,
                     var configOnBoot: ConfigOnBoot,
                     var configOnPlug: ConfigOnPlug,
                     var configCooldown: ConfigCooldown) {

    data class ConfigCapacity (var shutdown: Int, var resume: Int, var pause: Int, var chargeSwitch: String?)
    data class ConfigVoltage (var controlFile: String?, var max: Int)
    data class ConfigTemperature (var cooldown: Int, var max: Int, var pause: Int)
    data class ConfigOnBoot (var enabled: Boolean, var command: String?)
    data class ConfigOnPlug (var command: String?)
    data class ConfigCooldown (var atPercent: Int, var charge: Int, var pause: Int)

}