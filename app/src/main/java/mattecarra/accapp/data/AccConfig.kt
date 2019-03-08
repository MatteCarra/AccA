package mattecarra.accapp.data

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import mattecarra.accapp.utils.AccUtils

data class UpdateResult(
    val capacityUpdateSuccessful: Boolean,
    val coolDownUpdateSuccessful: Boolean,
    val tempUpdateSuccessful: Boolean,
    val voltControlUpdateSuccessful: Boolean,
    val resetUnpluggedUpdateSuccessful: Boolean,
    val onBootExitUpdateSuccessful: Boolean,
    val onBootUpdateSuccessful: Boolean,
    val chargingSwitchUpdateSuccessFul: Boolean
)

@Parcelize
data class Cooldown(var charge: Int, var pause: Int): Parcelable {
    fun updateAcc(): Boolean {
        return AccUtils.updateCoolDown(charge, pause)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateCoolDownCommand(charge, pause)
    }
}

@Parcelize
data class Capacity(var shutdownCapacity: Int, var coolDownCapacity: Int, var resumeCapacity: Int, var pauseCapacity: Int): Parcelable {
    fun updateAcc(): Boolean {
        return AccUtils.updateCapacity(shutdownCapacity, coolDownCapacity, resumeCapacity, pauseCapacity)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateCapacityCommand(shutdownCapacity, coolDownCapacity, resumeCapacity, pauseCapacity)
    }
}

@Parcelize
data class Temp(var coolDownTemp: Int, var pauseChargingTemp: Int, var waitSeconds: Int): Parcelable {
    fun updateAcc(): Boolean {
        return AccUtils.updateTemp(coolDownTemp, pauseChargingTemp, waitSeconds)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateTempCommand(coolDownTemp, pauseChargingTemp, waitSeconds)
    }
}


data class VoltControl(var voltFile: String?, var voltMax: Int?): Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString(), parcel.readValue(Int::class.java.classLoader) as Int?)

    fun updateAcc(): Boolean {
        return AccUtils.updateVoltage(voltFile, voltMax)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateVoltageCommand(voltFile, voltMax)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(voltFile)
        parcel.writeValue(voltMax)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VoltControl> {
        override fun createFromParcel(parcel: Parcel): VoltControl {
            return VoltControl(parcel)
        }

        override fun newArray(size: Int): Array<VoltControl?> {
            return arrayOfNulls(size)
        }
    }
}

@Parcelize
data class AccConfig(
    val capacity: Capacity,
    var cooldown: Cooldown?,
    val temp: Temp,
    val voltControl: VoltControl,
    var resetUnplugged: Boolean,
    var onBootExit: Boolean,
    var onBoot: String?,
    var chargingSwitch: String?
): Parcelable {

    fun getCommands(): List<String> {
        return arrayOf(
            capacity.getUpdateAccCommand(),
            cooldown?.getUpdateAccCommand(),
            temp.getUpdateAccCommand(),
            voltControl.getUpdateAccCommand(),
            AccUtils.updateResetUnpluggedCommand(resetUnplugged),
            AccUtils.updateOnBootExitCommand(onBootExit),
            AccUtils.updateOnBootCommand(onBoot),
            chargingSwitch?.let { AccUtils.setChargingSwitchCommand(it) } ?: AccUtils.unsetChargingSwitchCommand(),
            "acc -D restart"
        ).filterNotNull()
    }

    fun updateAcc(): UpdateResult {
        return UpdateResult(
            capacity.updateAcc(),
            cooldown?.updateAcc() ?: true,
            temp.updateAcc(),
            voltControl.updateAcc(),
            AccUtils.updateResetUnplugged(resetUnplugged),
            AccUtils.updateOnBootExit(onBootExit),
            AccUtils.updateOnBoot(onBoot),
            chargingSwitch?.let { AccUtils.setChargingSwitch(it) } ?: AccUtils.unsetChargingSwitch()
        )
    }
}