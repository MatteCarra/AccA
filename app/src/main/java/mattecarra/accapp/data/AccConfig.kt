package mattecarra.accapp.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import mattecarra.accapp.utils.AccUtils

@Parcelize
data class Cooldown(var charge: Int, var pause: Int): Parcelable {
    fun updateAcc() {
        AccUtils.updateCoolDown(charge, pause)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateCoolDownCommand(charge, pause)
    }
}

@Parcelize
data class Capacity(var shutdownCapacity: Int, var coolDownCapacity: Int, var resumeCapacity: Int, var pauseCapacity: Int): Parcelable {
    fun updateAcc() {
        AccUtils.updateCapacity(shutdownCapacity, coolDownCapacity, resumeCapacity, pauseCapacity)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateCapacityCommand(shutdownCapacity, coolDownCapacity, resumeCapacity, pauseCapacity)
    }
}

@Parcelize
data class Temp(var coolDownTemp: Int, var pauseChargingTemp: Int, var waitSeconds: Int): Parcelable {
    fun updateAcc() {
        AccUtils.updateTemp(coolDownTemp, pauseChargingTemp, waitSeconds)
    }

    fun getUpdateAccCommand(): String {
        return AccUtils.updateTempCommand(coolDownTemp, pauseChargingTemp, waitSeconds)
    }
}

@Parcelize
data class AccConfig(
    val capacity: Capacity,
    var cooldown: Cooldown?,
    val temp: Temp,
    var resetUnplugged: Boolean,
    var onBootExit: Boolean,
    var onBoot: String?
): Parcelable {

    fun getCommands(): List<String> {
        return arrayOf(
            capacity.getUpdateAccCommand(),
            cooldown?.getUpdateAccCommand(),
            temp.getUpdateAccCommand(),
            AccUtils.updateResetUnpluggedCommand(resetUnplugged),
            AccUtils.updateOnBootExitCommand(onBootExit),
            AccUtils.updateOnBootCommand(onBoot),
            "acc -D restart"
        ).filterNotNull()
    }

    fun updateAcc() {
        capacity.updateAcc()
        cooldown?.updateAcc()
        temp.updateAcc()
        AccUtils.updateResetUnplugged(resetUnplugged)
        AccUtils.updateOnBootExit(onBootExit)
        AccUtils.updateOnBoot(onBoot)
        if(onBoot != null && AccUtils.isAccdRunning())
            AccUtils.accRestartDeamon()
    }
}