package mattecarra.accapp.acc

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.acc._interface.AccInterface
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.utils.LogExt

const val TAG = "ConfigUpdater()"

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
        sendCurrMax = mSharedPrefs.getBoolean("cueCurrMax", true)
        sendVoltage = mSharedPrefs.getBoolean("cueVoltage", true)
    }
}

//----------------------------------------------------------------------

data class ConfigUpdater(val accConfig: AccConfig, val cue: ConfigUpdaterEnable)
{
    suspend fun execute(acc: AccInterface): ConfigUpdateResult = withContext(Dispatchers.IO) {

        LogExt().d(TAG, "pEnable: $cue")
        LogExt().d(TAG, "pAcc: $accConfig")

        val capacityUpdate = cue.sendCapacity && acc.updateAccCapacity(accConfig.configCapacity.shutdown, accConfig.configCoolDown?.atPercent ?: 101, accConfig.configCapacity.resume, accConfig.configCapacity.pause)
        val voltControl = cue.sendVoltage && acc.updateAccVoltControl(accConfig.configVoltage.controlFile, accConfig.configVoltage.max)
        val currentMax = cue.sendCurrMax && acc.updateAccCurrentMaxCommand(accConfig.configCurrMax)
        val temper = cue.sendTemperature && acc.updateAccTemperature(accConfig.configTemperature.coolDownTemperature, accConfig.configTemperature.maxTemperature, accConfig.configTemperature.pause)
        val coolDown = cue.sendCoolDown && acc.updateAccCoolDown(accConfig.configCoolDown?.charge, accConfig.configCoolDown?.pause )
        val resetUnplugged = cue.sendResetUnplugged && acc.updateResetUnplugged(accConfig.configResetUnplugged)
        val resetBSOnPause = cue.sendResetBsOnPause && acc.updateResetOnPause(accConfig.configResetBsOnPause)
        val onBootUpdateSuccessful = cue.sendOnBoot && acc.updateAccOnBoot(accConfig.configOnBoot)
        val onPlugged = cue.sendOnPlug && acc.updateAccOnPlugged(accConfig.configOnPlug)
        val chargingSwitch = cue.sendChargeSwitch && acc.updateAccChargingSwitch(accConfig.configChargeSwitch, accConfig.configIsAutomaticSwitchingEnabled)
        val prioritizeBatteryIdleMode = cue.sendBatteryIdleMode && acc.updatePrioritizeBatteryIdleMode(accConfig.prioritizeBatteryIdleMode)

        LogExt().d(TAG, (if(!cue.sendCapacity) "[off]" else if (capacityUpdate) "[ok]" else "[fail]")+" capacity: ${accConfig.configCapacity}")
        LogExt().d(TAG, (if(!cue.sendVoltage) "[off]" else if (voltControl) "[ok]" else "[fail]")+" voltage: ${accConfig.configVoltage}")
        LogExt().d(TAG, (if(!cue.sendCurrMax) "[off]" else if (currentMax) "[ok]" else "[fail]")+" currentMax: ${accConfig.configCurrMax}")
        LogExt().d(TAG, (if(!cue.sendTemperature) "[off]" else if (temper) "[ok]" else "[fail]")+" temperature: ${accConfig.configTemperature}")
        LogExt().d(TAG, (if(!cue.sendCoolDown) "[off]" else if (coolDown) "[ok]" else "[fail]")+" coolDown: ${accConfig.configCoolDown}")
        LogExt().d(TAG, (if(!cue.sendResetUnplugged) "[off]" else if (resetUnplugged) "[ok]" else "[fail]")+" resetUnplugged: ${accConfig.configResetUnplugged}")
        LogExt().d(TAG, (if(!cue.sendResetBsOnPause) "[off]" else if (resetBSOnPause) "[ok]" else "[fail]")+" resetBSOnPause: ${accConfig.configResetBsOnPause}")
        LogExt().d(TAG, (if(!cue.sendOnBoot) "[off]" else if (onBootUpdateSuccessful) "[ok]" else "[fail]")+" onBoot: ${accConfig.configOnBoot}")
        LogExt().d(TAG, (if(!cue.sendOnPlug) "[off]" else if (onPlugged) "[ok]" else "[fail]")+" onPlugged: ${accConfig.configOnPlug}")
        LogExt().d(TAG, (if(!cue.sendChargeSwitch) "[off]" else if (chargingSwitch) "[ok]" else "[fail]")+" chargingSwitch: ${accConfig.configChargeSwitch}")
        LogExt().d(TAG, (if(!cue.sendBatteryIdleMode) "[off]" else if (prioritizeBatteryIdleMode) "[ok]" else "[fail]")+" batteryIdleMode: ${accConfig.prioritizeBatteryIdleMode}")

        val temp = ConfigUpdateResult(
            capacityUpdateSuccessful = if (cue.sendCapacity) (if (capacityUpdate) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
            voltControlUpdateSuccessful = if (cue.sendVoltage) (if (voltControl) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
            currentMaxUpdateSuccessful = if (cue.sendCurrMax) (if (currentMax) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
            tempUpdateSuccessful = if (cue.sendTemperature) (if (temper) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
            coolDownUpdateSuccessful = if (cue.sendCoolDown) (if (coolDown) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
            resetUnpluggedUpdateSuccessful = if (cue.sendResetUnplugged) (if (resetUnplugged) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
            resetBSOnPauseSuccessful = if (cue.sendResetBsOnPause) (if (resetBSOnPause) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
            onBootUpdateSuccessful = if (cue.sendOnBoot) (if (onBootUpdateSuccessful) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
            onPluggedUpdateSuccessful = if (cue.sendOnPlug) (if (onPlugged) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
            chargingSwitchUpdateSuccessful = if (cue.sendChargeSwitch) (if (chargingSwitch) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
            prioritizeBatteryIdleModeSuccessful = if (cue.sendBatteryIdleMode) (if (prioritizeBatteryIdleMode) ConfigUpdateStatus.STATUS_OK else ConfigUpdateStatus.STATUS_FAIL ) else ConfigUpdateStatus.STATUS_OFF,
        )

        LogExt().d(TAG, "ConfigUpdateResult()=${temp.isSuccessful()}")

        temp
    }

    fun concatenateCommands(acc: AccInterface): String
    {
        return arrayOf(
            acc.getUpdateAccCapacityCommand(
                accConfig.configCapacity.shutdown,
                accConfig.configCoolDown?.atPercent ?: 101,
                accConfig.configCapacity.resume,
                accConfig.configCapacity.pause ),
            acc.getUpdateAccVoltControlCommand(
                accConfig.configVoltage.controlFile,
                accConfig.configVoltage.max ),
            acc.getUpdateAccCurrentMaxCommand(accConfig.configCurrMax),
            acc.getUpdateAccTemperatureCommand(
                accConfig.configTemperature.coolDownTemperature,
                accConfig.configTemperature.maxTemperature,
                accConfig.configTemperature.pause ),
            acc.getUpdateAccCoolDownCommand(
                accConfig.configCoolDown?.charge,
                accConfig.configCoolDown?.pause ),
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
 * Enum class for interpreting update results
 */
enum class ConfigUpdateStatus { STATUS_OFF, STATUS_OK, STATUS_FAIL }

/**
 * Data class for returning and interpreting update results when applying new values.
 */
data class ConfigUpdateResult(
    val capacityUpdateSuccessful: ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF,
    val voltControlUpdateSuccessful: ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF,
    val currentMaxUpdateSuccessful:  ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF,
    val tempUpdateSuccessful: ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF,
    val coolDownUpdateSuccessful:  ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF,
    val resetUnpluggedUpdateSuccessful:  ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF,
    val resetBSOnPauseSuccessful:  ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF,
    val onBootUpdateSuccessful:  ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF,
    val onPluggedUpdateSuccessful:  ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF,
    val chargingSwitchUpdateSuccessful:  ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF,
    val prioritizeBatteryIdleModeSuccessful: ConfigUpdateStatus = ConfigUpdateStatus.STATUS_OFF
) {

    fun isSuccessful(): Boolean // check only 8/11 parameters -(?.?)=
    {
        return capacityUpdateSuccessful != ConfigUpdateStatus.STATUS_FAIL
         && voltControlUpdateSuccessful != ConfigUpdateStatus.STATUS_FAIL
                && tempUpdateSuccessful != ConfigUpdateStatus.STATUS_FAIL
            && coolDownUpdateSuccessful != ConfigUpdateStatus.STATUS_FAIL
      && resetUnpluggedUpdateSuccessful != ConfigUpdateStatus.STATUS_FAIL
              && onBootUpdateSuccessful != ConfigUpdateStatus.STATUS_FAIL
           && onPluggedUpdateSuccessful != ConfigUpdateStatus.STATUS_FAIL
      && chargingSwitchUpdateSuccessful != ConfigUpdateStatus.STATUS_FAIL
    }
}
