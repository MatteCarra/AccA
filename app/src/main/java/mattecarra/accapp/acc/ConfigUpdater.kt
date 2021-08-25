package mattecarra.accapp.acc

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.acc._interface.AccInterface
import mattecarra.accapp.models.AccConfig

//----------------------------------------------------------------------
// Class to enable\disable Shell commands for greater flexibility

data class ConfigUpdaterEnable(  // primary constructor, all values as TRUE
    var sendCapacity: Boolean = true,
    var sendVoltage: Boolean = true,
    var sendCurrMax: Boolean = true,
    var sendTemperature: Boolean = true,
    var sendCoolDown: Boolean = true,
    var sendOnBoot: Boolean = true,
    var sendOnPlug: Boolean = true,
    var sendResetUnplugged: Boolean = true,
    var sendResetBsOnPause: Boolean = true,
    var sendChargeSwitch: Boolean = true,
    var sendBatteryIdleMode: Boolean = true
) {

    // Secondary constructor, values are obtained from setting over the base
    constructor(mSharedPrefs: SharedPreferences) : this()
    {
        getParam(mSharedPrefs)
    }

    fun getParam(mSharedPrefs: SharedPreferences)
    {
        sendCurrMax = mSharedPrefs.getBoolean("escCurrMax", true)
        sendVoltage = mSharedPrefs.getBoolean("escVoltage", true)

    }
}

//----------------------------------------------------------------------

data class ConfigUpdater(val accConfig: AccConfig, val cue: ConfigUpdaterEnable) {
    suspend fun execute(acc: AccInterface): ConfigUpdateResult = withContext(Dispatchers.IO) {
        ConfigUpdateResult(

            cue.sendCapacity && acc.updateAccCapacity(
                accConfig.configCapacity.shutdown, accConfig.configCoolDown?.atPercent ?: 101,
                accConfig.configCapacity.resume, accConfig.configCapacity.pause
            ),

            cue.sendVoltage && acc.updateAccVoltControl(
                accConfig.configVoltage.controlFile,
                accConfig.configVoltage.max
            ),

            cue.sendCurrMax && acc.updateAccCurrentMaxCommand(accConfig.configCurrMax),

            cue.sendTemperature && acc.updateAccTemperature(
                accConfig.configTemperature.coolDownTemperature,
                accConfig.configTemperature.maxTemperature,
                accConfig.configTemperature.pause
            ),

            cue.sendCoolDown && acc.updateAccCoolDown(
                accConfig.configCoolDown?.charge,
                accConfig.configCoolDown?.pause
            ),

            cue.sendResetUnplugged && acc.updateResetUnplugged(accConfig.configResetUnplugged),
            cue.sendResetBsOnPause && acc.updateResetOnPause(accConfig.configResetBsOnPause),
            cue.sendOnBoot && acc.updateAccOnBoot(accConfig.configOnBoot),
            cue.sendOnPlug && acc.updateAccOnPlugged(accConfig.configOnPlug),
            cue.sendChargeSwitch && acc.updateAccChargingSwitch(accConfig.configChargeSwitch, accConfig.configIsAutomaticSwitchingEnabled),
            cue.sendBatteryIdleMode && acc.updatePrioritizeBatteryIdleMode(accConfig.prioritizeBatteryIdleMode)
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
            acc.getUpdateAccCurrentMaxCommand(accConfig.configCurrMax),
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
            acc.getUpdateResetOnPauseCommand(accConfig.configResetBsOnPause),
            acc.getUpdateAccOnBootCommand(accConfig.configOnBoot),
            acc.getUpdateAccOnPluggedCommand(accConfig.configOnPlug),
            acc.getUpdateAccChargingSwitchCommand(accConfig.configChargeSwitch, accConfig.configIsAutomaticSwitchingEnabled),
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
    val currentMaxUpdateSuccessful: Boolean,
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
        if(!capacityUpdateSuccessful) println("Update capacity update failed or disable")

        if(!voltControlUpdateSuccessful) println("Volt control update failed or disable")

        if(!currentMaxUpdateSuccessful) println("Current max update failed or disable")

        if(!tempUpdateSuccessful) println("Temp update update failed or disable")

        if(!coolDownUpdateSuccessful) println("Cooldown update update failed or disable")

        if(!resetUnpluggedUpdateSuccessful) println("Reset unplugged update failed or disable")

        if(!resetBSOnPauseSuccessful) println("Reset battery stats on pause update failed or disable")

        if(!onBootUpdateSuccessful) println("onBoot update failed or disable")

        if(!onPluggedUpdateSuccessful) println("onPlugged update failed or disable")

        if(!chargingSwitchUpdateSuccessful) println("Charging switch update failed or disable")

        if(!prioritizeBatteryIdleModeSuccessful) println("Battery idle mode update failed or disable")
    }

    fun isSuccessful(): Boolean {
        return capacityUpdateSuccessful && voltControlUpdateSuccessful && tempUpdateSuccessful && coolDownUpdateSuccessful && resetUnpluggedUpdateSuccessful && onBootUpdateSuccessful && onPluggedUpdateSuccessful && chargingSwitchUpdateSuccessful
    }
}
