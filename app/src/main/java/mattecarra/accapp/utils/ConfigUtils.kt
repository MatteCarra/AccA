package mattecarra.accapp.utils

//import android.os.Parcel
//import android.os.Parcelable
//import androidx.room.Entity
//import kotlinx.android.parcel.Parcelize
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
 * CoolDown related functions.
 * @param charge seconds to charge for during the cool down phase.
 * @param pause seconds to pause for during the cool down phase.
 */
data class CoolDown(var charge: Int, var pause: Int) {

    fun updateAcc(): Boolean {
        return AccUtils.updateCoolDown(charge, pause)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateCoolDownCommand(charge, pause)
    }
}

/**
 * Capactiy related functions.
 * @param shutdownCapacity shutdown the device at the specified percentage.
 * @param coolDownCapacity starts the cool down phase at the specified percentage.
 * @param resumeCapacity allows charging starting from the specified capacity.
 * @param pauseCapacity pauses charging at the specified capacity.
 */
data class Capacity(var shutdownCapacity: Int, var coolDownCapacity: Int, var resumeCapacity: Int, var pauseCapacity: Int) {

    fun updateAcc(): Boolean {
        return AccUtils.updateCapacity(shutdownCapacity, coolDownCapacity, resumeCapacity, pauseCapacity)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateCapacityCommand(shutdownCapacity, coolDownCapacity, resumeCapacity, pauseCapacity)
    }
}

/**
 * Temperature related functions.
 * @param coolDownTemp starts cool down phase at the specified temperature.
 * @param pauseChargingTemp pauses charging at the specified temperature.
 * @param waitSeconds seconds to wait until charging is resumed.
 */
data class Temperature(var coolDownTemp: Int, var pauseChargingTemp: Int, var waitSeconds: Int) {

    fun updateAcc(): Boolean {
        return AccUtils.updateTemp(coolDownTemp, pauseChargingTemp, waitSeconds)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateTempCommand(coolDownTemp, pauseChargingTemp, waitSeconds)
    }
}

/**
 * Functions related to Voltage Control.
 * @param voltFile Path to the voltage file on the device.
 * @param voltMax Maximum voltage the phone should charge at.
 */
data class VoltControl(var voltFile: String?, var voltMax: Int?) {
//    constructor(parcel: Parcel) : this(parcel.readString(), parcel.readValue(Int::class.java.classLoader) as Int?)

    fun updateAcc(): Boolean {
        return AccUtils.updateVoltage(voltFile, voltMax)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateVoltageCommand(voltFile, voltMax)
    }

//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(voltFile)
//        parcel.writeValue(voltMax)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<VoltControl> {
//        override fun createFromParcel(parcel: Parcel): VoltControl {
//            return VoltControl(parcel)
//        }
//
//        override fun newArray(size: Int): Array<VoltControl?> {
//            return arrayOfNulls(size)
//        }
//    }
}

//@Parcelize
//@Entity
//data class AccConfig(
//    val capacity: Capacity,
//    var cooldown: CoolDown?,
//    val temp: Temperature,
//    val voltControl: VoltControl,
//    var resetUnplugged: Boolean,
//    var onBootExit: Boolean,
//    var onBoot: String?,
//    var onPlugged: String?,
//    var chargingSwitch: String?
//): Parcelable {
//
//    fun getCommands(): List<String> {
//        return arrayOf(
//            capacity.getUpdateAccCommand(),
//            cooldown?.getUpdateAccCommand(),
//            temp.getUpdateAccCommand(),
//            voltControl.getUpdateAccCommand(),
//            AccUtils.updateResetUnpluggedCommand(resetUnplugged),
//            AccUtils.updateOnBootExitCommand(onBootExit),
//            AccUtils.updateOnBootCommand(onBoot),
//            AccUtils.updateOnPluggedCommand(onPlugged),
//            chargingSwitch?.let { AccUtils.setChargingSwitchCommand(it) } ?: AccUtils.unsetChargingSwitchCommand(),
//            "acc -D restart"
//        ).filterNotNull()
//    }
//
//    fun updateAcc(): UpdateResult {
//        return UpdateResult(
//            capacity.updateAcc(),
//            cooldown?.updateAcc() ?: true,
//            temp.updateAcc(),
//            voltControl.updateAcc(),
//            AccUtils.updateResetUnplugged(resetUnplugged),
//            AccUtils.updateOnBootExit(onBootExit),
//            AccUtils.updateOnBoot(onBoot),
//            AccUtils.updateOnPlugged(onPlugged),
//            chargingSwitch?.let { AccUtils.setChargingSwitch(it) } ?: AccUtils.unsetChargingSwitch()
//        )
//    }
}