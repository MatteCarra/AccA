package mattecarra.accapp.utils

import com.topjohnwu.superuser.Shell
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
        val onBootUpdateSuccessful: Boolean,
        val onPluggedUpdateSuccessful: Boolean,
        val chargingSwitchUpdateSuccessful: Boolean
    ) {
        fun debug() {
            if(!capacityUpdateSuccessful) println("Update capacity update failed")

            if(!voltControlUpdateSuccessful) println("Volt control update failed")

            if(!tempUpdateSuccessful) println("Temp update update failed")

            if(!coolDownUpdateSuccessful) println("Cooldown update update failed")

            if(!resetUnpluggedUpdateSuccessful) println("Reset unplugged update failed")

            if(!onBootUpdateSuccessful) println("onBoot update failed")

            if(!onPluggedUpdateSuccessful) println("onPlugged update failed")

            if(!chargingSwitchUpdateSuccessful) println("Charging switch update failed")
        }

        fun isSuccessful(): Boolean {
            return capacityUpdateSuccessful && voltControlUpdateSuccessful && tempUpdateSuccessful && coolDownUpdateSuccessful && resetUnpluggedUpdateSuccessful && onBootUpdateSuccessful && onPluggedUpdateSuccessful && chargingSwitchUpdateSuccessful
        }
    }

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
            updateAccCoolDown(accConfig.configCoolDown.charge, accConfig.configCoolDown.pause),
            updateResetUnplugged(accConfig.configResetUnplugged),
            updateAccOnBoot(accConfig.configOnBoot),
            updateAccOnPlugged(accConfig.configOnPlug),
            updateAccChargingSwitch(accConfig.configChargeSwitch)
        )
    }
}

//reset unplugged command
fun updateResetUnplugged(resetUnplugged: Boolean): Boolean {
    return Shell.su("acc -s resetBsOnUnplug $resetUnplugged").exec().isSuccess
}

/**
 * Updates the cool down charge and pause durations.
 * @param charge seconds to charge for during the cool down phase.
 * @param pause seconds to pause for during the cool down phase.
 * @return boolean if the command was successful.
 */
fun updateAccCoolDown(charge: Int?, pause: Int?) : Boolean {
    return charge?.let { charge ->
        pause?.let { pause ->
            Shell.su("acc -s coolDownRatio $charge/$pause").exec().isSuccess
        }
    } ?: true
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
    return Shell.su("acc -s capacity $shutdown,$coolDown,$resume-$pause").exec().isSuccess
}

/**
 * Updates the temperature related configuration in ACC.
 * @param coolDownTemperature starts cool down phase at the specified temperature.
 * @param pauseTemperature pauses charging at the specified temperature.
 * @param wait seconds to wait until charging is resumed.
 * @return the boolean result of the command's execution.
 */
fun updateAccTemperature(coolDownTemperature: Int, pauseTemperature: Int, wait: Int) : Boolean {
    return Shell.su("acc -s temperature ${coolDownTemperature*10}-${pauseTemperature*10}_$wait").exec().isSuccess
}

/**
 * Updates the voltage related configuration in ACC.
 * @param voltFile path to the voltage file on the device.
 * @param voltMax maximum voltage the phone should charge at.
 * @return the boolean result of the command's execution.
 */
fun updateAccVoltControl(voltFile: String?, voltMax: Int?) : Boolean {
    return Shell.su(
        if(voltFile != null && voltMax != null)
            "acc --set chargingVoltageLimit $voltFile:$voltMax"
        else if(voltMax != null)
            "acc --set chargingVoltageLimit $voltMax"
        else
            "acc --set chargingVoltageLimit"
    ).exec().isSuccess
}

/**
 * Updates the on boot exit (boolean) configuration in ACC.
 * @param enabled boolean: if OnBootExit should be enabled.
 * @return the boolean result of the command's execution.
 */
fun updateAccOnBootExit(enabled: Boolean) : Boolean {
    return Shell.su("acc -s onBootExit $enabled").exec().isSuccess
}

/**
 * Updates the OnBoot command configuration in ACC.
 * @param command the command to be run after the device starts (daemon starts).
 * @return the boolean result of the command's execution.
 */
fun updateAccOnBoot(command: String?) : Boolean {
    return Shell.su("acc -s applyOnBoot${command?.let{ " $it" } ?: ""}").exec().isSuccess
}

/**
 * Updates the OnPlugged configuration in ACC.
 * @param command the command to be run when the device is plugged in.
 * @return the boolean result of the command's execution.
 */
fun updateAccOnPlugged(command: String?) : Boolean {
    return Shell.su("acc -s applyOnPlug${command?.let{ " $it" } ?: ""}").exec().isSuccess
}

fun updateAccChargingSwitch(switch: String?) : Boolean {
    if (switch.isNullOrBlank()) {
        return Shell.su("acc -s s-").exec().isSuccess
    }

    return Shell.su("acc --set switch $switch").exec().isSuccess
}