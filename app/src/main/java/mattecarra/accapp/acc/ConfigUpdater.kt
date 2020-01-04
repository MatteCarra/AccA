package mattecarra.accapp.acc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.models.AccConfig

data class ConfigUpdater(val accConfig: AccConfig) {
    suspend fun execute(acc: AccInterface): ConfigUpdateResult = withContext(Dispatchers.IO) {
        ConfigUpdateResult(
            acc.updateAccCapacity(
                accConfig.configCapacity.shutdown, accConfig.configCoolDown?.atPercent ?: 101,
                accConfig.configCapacity.resume, accConfig.configCapacity.pause
            ),
            acc.updateAccVoltControl(
                accConfig.configVoltage.controlFile,
                accConfig.configVoltage.max
            ),
            acc.updateAccTemperature(
                accConfig.configTemperature.coolDownTemperature,
                accConfig.configTemperature.maxTemperature,
                accConfig.configTemperature.pause
            ),
            acc.updateAccCoolDown(
                accConfig.configCoolDown?.charge,
                accConfig.configCoolDown?.pause
            ),
            acc.updateResetUnplugged(accConfig.configResetUnplugged),
            acc.updateResetOnPause(accConfig.configResetOnPause),
            acc.updateAccOnBoot(accConfig.configOnBoot),
            acc.updateAccOnPlugged(accConfig.configOnPlug),
            acc.updateAccChargingSwitch(accConfig.configChargeSwitch),
            acc.updatePrioritizeBatteryIdleMode(accConfig.prioritizeBatteryIdleMode)
        )
    }

    fun concatenateCommands(acc: AccInterface): String {
        return arrayOf(
            acc.getUpdateAccCapacityCommand(
                accConfig.configCapacity.shutdown, accConfig.configCoolDown?.atPercent ?: 101,
                accConfig.configCapacity.resume, accConfig.configCapacity.pause
            ),
            acc.getUpdateAccVoltControlCommand(
                accConfig.configVoltage.controlFile,
                accConfig.configVoltage.max
            ),
            acc.getUpdateAccTemperatureCommand(
                accConfig.configTemperature.coolDownTemperature,
                accConfig.configTemperature.maxTemperature,
                accConfig.configTemperature.pause
            ),
            acc.getUpdateAccCoolDownCommand(
                accConfig.configCoolDown?.charge,
                accConfig.configCoolDown?.pause
            ),
            acc.getUpdateResetUnpluggedCommand(accConfig.configResetUnplugged),
            acc.getUpdateResetOnPauseCommand(accConfig.configResetOnPause),
            acc.getUpdateAccOnBootCommand(accConfig.configOnBoot),
            acc.getUpdateAccOnPluggedCommand(accConfig.configOnPlug),
            acc.getUpdateAccChargingSwitchCommand(accConfig.configChargeSwitch),
            acc.getUpdatePrioritizeBatteryIdleModeCommand(accConfig.prioritizeBatteryIdleMode)
        ).joinToString("; ")
    }
}

/**
 * Data class for returning and interpreting update results when applying new values.
 */
data class ConfigUpdateResult(
    val capacityUpdateSuccessful: Boolean,
    val voltControlUpdateSuccessful: Boolean,
    val tempUpdateSuccessful: Boolean,
    val coolDownUpdateSuccessful: Boolean,
    val resetUnpluggedUpdateSuccessful: Boolean,
    val resetBSOnPauseSuccessful: Boolean,
    val onBootUpdateSuccessful: Boolean,
    val onPluggedUpdateSuccessful: Boolean,
    val chargingSwitchUpdateSuccessful: Boolean,
    val prioritizeBatteryIdleModeSuccessful: Boolean
) {
    fun debug() {
        if(!capacityUpdateSuccessful) println("Update capacity update failed")

        if(!voltControlUpdateSuccessful) println("Volt control update failed")

        if(!tempUpdateSuccessful) println("Temp update update failed")

        if(!coolDownUpdateSuccessful) println("Cooldown update update failed")

        if(!resetUnpluggedUpdateSuccessful) println("Reset unplugged update failed")

        if(!resetBSOnPauseSuccessful) println("Reset battery stats on pause update failed")

        if(!onBootUpdateSuccessful) println("onBoot update failed")

        if(!onPluggedUpdateSuccessful) println("onPlugged update failed")

        if(!chargingSwitchUpdateSuccessful) println("Charging switch update failed")

        if(!prioritizeBatteryIdleModeSuccessful) println("Battery idle mode update failed")
    }

    fun isSuccessful(): Boolean {
        return capacityUpdateSuccessful && voltControlUpdateSuccessful && tempUpdateSuccessful && coolDownUpdateSuccessful && resetUnpluggedUpdateSuccessful && onBootUpdateSuccessful && onPluggedUpdateSuccessful && chargingSwitchUpdateSuccessful
    }
}
