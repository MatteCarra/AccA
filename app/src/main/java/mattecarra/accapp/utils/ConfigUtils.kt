package mattecarra.accapp.utils

import mattecarra.accapp.models.AccConfig

object ConfigUtils {

    /**
     * Data class for returning and interpreting update results when applying new values.
     */
    data class UpdateResult(
        val capacityUpdateSuccessful: Boolean,
        val voltControlUpdateSuccessful: Boolean,
        val tempUpdateSuccessful: Boolean,
        val coolDownUpdateSuccessful: Boolean,
        val resetUnpluggedUpdateSuccessful: Boolean,
        val onBootExitUpdateSuccessful: Boolean,
        val onBootUpdateSuccessful: Boolean,
        val onPluggedUpdateSuccessful: Boolean,
        val chargingSwitchUpdateSuccessFul: Boolean
    )

    /**
     * Function takes in AccConfig file and will apply it.
     * @param accConfig Configuration file to apply.
     * @return UpdateResult data class.
     */
    fun updateAcc(accConfig: AccConfig): UpdateResult {
        // Initalise new UpdateResult data class to return
        return UpdateResult(
            updateAccCapacity(accConfig.configCapacity.shutdown, accConfig.configCoolDown.atPercent,
                accConfig.configCapacity.resume, accConfig.configCapacity.pause),
            updateAccVoltControl(accConfig.configVoltage.controlFile, accConfig.configVoltage.max),
            updateAccTemperature(accConfig.configTemperature.coolDownTemperature, accConfig.configTemperature.maxTemperature, accConfig.configTemperature.pause),
            accConfig.configCoolDown.charge?.let { charge ->
                accConfig.configCoolDown.pause?.let { pause ->
                    updateAccCoolDown(charge, pause)
                }
            } ?: true,
            accConfig.configResetUnplugged,
            updateAccOnBootExit(accConfig.configOnBootExit),
            updateAccOnBoot(accConfig.configOnBoot),
            updateAccOnPlugged(accConfig.configOnPlug),
            updateAccChargingSwitch(accConfig.configChargeSwitch)
        )
    }
}

/**
 * Updates the cool down charge and pause durations.
 * @param charge seconds to charge for during the cool down phase.
 * @param pause seconds to pause for during the cool down phase.
 * @return boolean if the command was successful.
 */
fun updateAccCoolDown(charge: Int, pause: Int) : Boolean {
    return AccUtils.updateCoolDown(charge, pause)
}

/**
 * Returns the cool down update command with provided values.
 * @param charge seconds to charge for during the cool down phase.
 * @param pause seconds to pause for during the cool down phase.
 * @return ACC command string with the provided values.
 */
fun getUpdateAccCoolDownCommand(charge: Int, pause: Int) : String {
    return AccUtils.updateCoolDownCommand(charge, pause)
}

/**
 * Updates the capacity related settings of ACC.
 * @param shutdown shutdown the device at the specified percentage.
 * @param coolDown starts the cool down phase at the specified percentage.
 * @param resume allows charging starting from the specified capacity.
 * @param pause pauses charging at the specified capacity.
 * @return boolean if the command was successful.
 */
fun updateAccCapacity(shutdown: Int, coolDown: Int, resume: Int, pause: Int) : Boolean {
    return AccUtils.updateCapacity(shutdown, coolDown, resume, pause)
}

/**
 * Returns the command used to update the capacity configuration in ACC.
 * @param shutdown shutdown the device at the specified percentage.
 * @param coolDown starts the cool down phase at the specified percentage.
 * @param resume allows charging starting from the specified capacity.
 * @param pause pauses charging at the specified capacity.
 * @return ACC command string with the provided values.
 */
fun getUpdateAccCapacityCommand(shutdown: Int, coolDown: Int, resume: Int, pause: Int): String {
    return AccUtils.updateCapacityCommand(shutdown, coolDown, resume, pause)
}

/**
 * Updates the temperature related configuration in ACC.
 * @param coolDownTemperature starts cool down phase at the specified temperature.
 * @param pauseTemperature pauses charging at the specified temperature.
 * @param wait seconds to wait until charging is resumed.
 * @return the boolean result of the command's execution.
 */
fun updateAccTemperature(coolDownTemperature: Int, pauseTemperature: Int, wait: Int) : Boolean {
    return AccUtils.updateTemp(coolDownTemperature, pauseTemperature, wait)
}

/**
 * Returns the command used to update the temperature configuration in ACC.
 * @param coolDown starts cool down phase at the specified temperature.
 * @param pause pauses charging at the specified temperature.
 * @param wait seconds to wait until charging is resumed.
 * @return command string for updating the temperature configuration in ACC.
 */
fun getUpdateAccTemperatureCommand(coolDown: Int, pause: Int, wait: Int) : String {
    return AccUtils.updateTempCommand(coolDown, pause, wait)
}

/**
 * Updates the voltage related configuration in ACC.
 * @param voltFile path to the voltage file on the device.
 * @param voltMax maximum voltage the phone should charge at.
 * @return the boolean result of the command's execution.
 */
fun updateAccVoltControl(voltFile: String?, voltMax: Int?) : Boolean {
    return AccUtils.updateVoltage(voltFile, voltMax)
}

/**
 * Returns the command used to update the voltage related configuration in ACC.
 * @param voltFile path to the voltage file on the device.
 * @param voltMax maximum voltage the phone should charge at.
 * @return command used to update the voltageFile & voltageMax.
 */
fun getUpdateAccVoltControlCommand(voltFile: String?, voltMax: Int?) : String {
    return AccUtils.updateVoltageCommand(voltFile, voltMax)
}

/**
 * Updates the on boot exit (boolean) configuration in ACC.
 * @param enabled boolean: if OnBootExit should be enabled.
 * @return the boolean result of the command's execution.
 */
fun updateAccOnBootExit(enabled: Boolean) : Boolean {
    return AccUtils.updateOnBootExit(enabled)
}

/**
 * Returns the command used to update the onBootExit configuration in ACC.
 * @param enabled boolean: if OnBootExit should be enabled.
 * @return command used to update the onBootExit configuration.
 */
fun getUpdateAccOnBootExitCommand(enabled: Boolean) : String {
    return AccUtils.updateOnBootExitCommand(enabled)
}

/**
 * Updates the OnBoot command configuration in ACC.
 * @param command the command to be run after the device starts (daemon starts).
 * @return the boolean result of the command's execution.
 */
fun updateAccOnBoot(command: String?) : Boolean {
    return AccUtils.updateOnBoot(command)
}

/**
 * Returns the command used to update the OnBoot command configuration in ACC.
 * @param command the command to be run after the device starts (daemon starts).
 * @return command used to update the OnBoot command.
 */
fun getUpdateAccOnBootCommand(command: String?) : String {
    return AccUtils.updateOnBootCommand(command)
}

/**
 * Updates the OnPlugged configuration in ACC.
 * @param command the command to be run when the device is plugged in.
 * @return the boolean result of the command's execution.
 */
fun updateAccOnPlugged(command: String?) : Boolean {
    return AccUtils.updateOnPlugged(command)
}

/**
 * Returns the command used to update the OnPlugged configuration in ACC.
 * @param command the command to be run when the device is plugged in.
 * @return command used to update the OnPlugged configuration.
 */
fun getUpdateAccOnPluggedCommand(command: String?) : String {
    return AccUtils.updateOnPluggedCommand(command)
}

fun updateAccChargingSwitch(switch: String?) : Boolean {
    if (switch.isNullOrBlank()) {
        return AccUtils.unsetChargingSwitch()
    }
    return AccUtils.setChargingSwitch(switch)
}