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
        val onPluggedUpdateSuccesful: Boolean,
        val chargingSwitchUpdateSuccessFul: Boolean
    )

    /**
     * Function takes in AccConfig file and will apply it.
     * @param accConfig Configuration file to apply.
     * @return UpdateResult data class.
     */
    fun updateAcc(accConfig: AccConfig): UpdateResult {

        // Initalise new UpdateResult data class to return
        var updateResult: UpdateResult

        // Apply Capacity
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
 * @param coolDown starts cool down phase at the specified temperature.
 * @param pause pauses charging at the specified temperature.
 * @param wait seconds to wait until charging is resumed.
 */
fun updateAccTemperature(coolDown: Int, pause: Int, wait: Int) : Boolean {
    return AccUtils.updateTemp(coolDown, pause, wait)
}

/**
 * Returns the command used to update the temperature configuration in ACC.
 * @param coolDown starts cool down phase at the specified temperature.
 * @param pause pauses charging at the specified temperature.
 * @param wait seconds to wait until charging is resumed.
 */
fun getUpdateAccTemperatureCommand(coolDown: Int, pause: Int, wait: Int) : String {
    return AccUtils.updateTempCommand(coolDown, pause, wait)
}

/**
 * Updates the voltage related configuration in ACC.
 * @param voltFile path to the voltage file on the device.
 * @param voltMax maximum voltage the phone should charge at.
 */
fun updateAccVoltControl(voltFile: String?, voltMax: Int?) : Boolean {
    return AccUtils.updateVoltage(voltFile, voltMax)
}

/**
 * Updates the voltage related configuration in ACC.
 * @param voltFile path to the voltage file on the device.
 * @param voltMax maximum voltage the phone should charge at.
 */
fun updateAccVoltControlCommand(voltFile: String?, voltMax: Int?) : String {
    return AccUtils.updateVoltageCommand(voltFile, voltMax)
}