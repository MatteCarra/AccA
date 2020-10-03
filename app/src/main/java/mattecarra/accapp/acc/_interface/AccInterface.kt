package mattecarra.accapp.acc._interface

import androidx.annotation.WorkerThread
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.acc.ConfigUpdateResult
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.BatteryInfo

interface AccInterface {
    val version: Int

    suspend fun readConfig(): AccConfig

    suspend fun readDefaultConfig(): AccConfig

    suspend fun listVoltageSupportedControlFiles(): List<String>

    suspend fun resetBatteryStats(): Boolean

    suspend fun getBatteryInfo(): BatteryInfo

    suspend fun isBatteryCharging(): Boolean

    suspend fun isAccdRunning(): Boolean

    suspend fun abcStartDaemon(): Boolean

    suspend fun abcRestartDaemon(): Boolean

    suspend fun abcStopDaemon(): Boolean

    suspend fun listChargingSwitches(): List<String>

    suspend fun testChargingSwitch(chargingSwitch: String? = null): Int

    fun getCurrentChargingSwitch(config: String): String?

    fun isAutomaticSwitchEnabled(config: String): Boolean

    fun isPrioritizeBatteryIdleMode(config: String): Boolean

    suspend fun setChargingLimitForOneCharge(limit: Int): Boolean

    suspend fun isBatteryIdleSupported(): Pair<Int, Boolean>

    suspend fun updateAccConfig(accConfig: AccConfig): ConfigUpdateResult

    /**
     * Updates the OnBoot command configuration in ACC.
     * @param command the command to be run after the device starts (daemon starts).
     * @return the boolean result of the command's execution.
     */
    fun getUpdateAccOnBootCommand(command: String?): String
    suspend fun updateAccOnBoot(command: String?) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccOnBootCommand(command)).exec().isSuccess
    }

    /**
     * Updates the on boot exit (boolean) configuration in ACC.
     * @param enabled boolean: if OnBootExit should be enabled.
     * @return the boolean result of the command's execution.
     */
    fun getUpdateAccOnBootExitCommand(enabled: Boolean): String
    suspend fun updateAccOnBootExit(enabled: Boolean) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccOnBootExitCommand(enabled)).exec().isSuccess
    }

    /**
     * Updates the voltage related configuration in ACC.
     * @param voltFile path to the voltage file on the device.
     * @param voltMax maximum voltage the phone should charge at.
     * @return the boolean result of the command's execution.
     */
    fun getUpdateAccVoltControlCommand(voltFile: String?, voltMax: Int?): String
    suspend fun updateAccVoltControl(voltFile: String?, voltMax: Int?) : Boolean = withContext(
        Dispatchers.IO) {
        Shell.su(getUpdateAccVoltControlCommand(voltFile, voltMax)).exec().isSuccess
    }

    fun getUpdateAccCurrentMaxCommand(currMax: Int?): String
    suspend fun updateAccCurrentMaxCommand(currMax: Int?) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccCurrentMaxCommand(currMax)).exec().isSuccess
    }

    /**
     * Updates the temperature related configuration in ACC.
     * @param coolDownTemperature starts cool down phase at the specified temperature.
     * @param temperatureMax pauses charging at the specified temperature.
     * @param wait seconds to wait until charging is resumed.
     * @return the boolean result of the command's execution.
     */
    fun getUpdateAccTemperatureCommand(coolDownTemperature: Int, temperatureMax: Int, wait: Int): String
    suspend fun updateAccTemperature(coolDownTemperature: Int, temperatureMax: Int, wait: Int) : Boolean = withContext(
        Dispatchers.IO) {
        Shell.su(getUpdateAccTemperatureCommand(coolDownTemperature, temperatureMax, wait)).exec().isSuccess
    }

    /**
     * Updates the capacity related settings of ACC.
     * @param shutdown shutdown the device at the specified percentage.
     * @param coolDown starts the cool down phase at the specified percentage.
     * @param resume allows charging starting from the specified capacity.
     * @param pause pauses charging at the specified capacity.
     * @return boolean if the command was successful.
     */
    fun getUpdateAccCapacityCommand(shutdown: Int, coolDown: Int, resume: Int, pause: Int): String
    suspend fun updateAccCapacity(shutdown: Int, coolDown: Int, resume: Int, pause: Int) : Boolean = withContext(
        Dispatchers.IO) {
        Shell.su(getUpdateAccCapacityCommand(shutdown, coolDown, resume, pause)).exec().isSuccess
    }

    /**
     * Updates the cool down charge and pause durations.
     * @param charge seconds to charge for during the cool down phase.
     * @param pause seconds to pause for during the cool down phase.
     * @return boolean if the command was successful.
     */
    fun getUpdateAccCoolDownCommand(charge: Int?, pause: Int?): String
    suspend fun updateAccCoolDown(charge: Int?, pause: Int?) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccCoolDownCommand(charge, pause)).exec().isSuccess
    }

    fun getUpdateResetUnpluggedCommand(resetUnplugged: Boolean): String
    suspend fun updateResetUnplugged(resetUnplugged: Boolean): Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateResetUnpluggedCommand(resetUnplugged)).exec().isSuccess
    }

    fun getUpdateResetOnPauseCommand(resetOnPause: Boolean): String
    suspend fun updateResetOnPause(resetOnPause: Boolean): Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateResetOnPauseCommand(resetOnPause)).exec().isSuccess
    }

    fun getUpdateAccChargingSwitchCommand(switch: String?, automaticSwitchingEnabled: Boolean): String
    suspend fun updateAccChargingSwitch(switch: String?, automaticSwitchingEnabled: Boolean) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccChargingSwitchCommand(switch, automaticSwitchingEnabled)).exec().isSuccess
    }

    /**
     * Updates the OnPlugged configuration in ACC.
     * @param command the command to be run when the device is plugged in.
     * @return the boolean result of the command's execution.
     */
    fun getUpdateAccOnPluggedCommand(command: String?): String
    suspend fun updateAccOnPlugged(command: String?) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccOnPluggedCommand(command)).exec().isSuccess
    }

    fun getUpgradeCommand(version: String): String
    suspend fun upgrade(version: String): Shell.Result? = withContext(Dispatchers.IO){
        val res = Shell.su(getUpgradeCommand(version)).exec()
        Acc.createAccInstance()
        res
    }

    fun getUpdatePrioritizeBatteryIdleModeCommand(enabled: Boolean): String
    suspend fun updatePrioritizeBatteryIdleMode(enabled: Boolean): Boolean = withContext(Dispatchers.IO){
        Shell.su(getUpdatePrioritizeBatteryIdleModeCommand(enabled)).exec().isSuccess
    }


    fun getAddChargingSwitchCommand(switch: String): String
    suspend fun addChargingSwitch(switch: String): Boolean = withContext(Dispatchers.IO) {
        Shell.su(getAddChargingSwitchCommand(switch)).exec().isSuccess
    }
}