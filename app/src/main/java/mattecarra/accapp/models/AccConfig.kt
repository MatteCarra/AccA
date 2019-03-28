package mattecarra.accapp.models

data class AccConfig (
    var cooldownCapacity: Int,
    var pauseCapacity: Int,
    var resumeCapacity: Int,
    var shutdownCapacity: Int,
    var chargingSwitch: String?,
    var cooldownCharge: Int,
    var cooldownPause: Int,
    var onBootCommand: String?,
    var onBootExit: Boolean,
    var onPluggedCommand: String?,
    var resetUnplugged: Boolean,
    var cooldownTemperature: Int,
    var pauseChargingTemperature: Int,
    var cooldownWaitTime: Int,
    var voltageControlFile: String?,
    var voltageMax: Int?
)