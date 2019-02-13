package mattecarra.accapp.data

import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize
import mattecarra.accapp.AccUtils

@Parcelize
class Cooldown(var charge: Int, var pause: Int): Parcelable {
    fun updateAcc() {
        AccUtils.updateCoolDown(charge, pause)
    }
}

@Parcelize
data class Capacity(var shutdownCapacity: Int, var coolDownCapacity: Int, var resumeCapacity: Int, var pauseCapacity: Int): Parcelable {
    fun updateAcc() {
        AccUtils.updateCapacity(shutdownCapacity, coolDownCapacity, resumeCapacity, pauseCapacity)
    }
}

@Parcelize
class Temp(var coolDownTemp: Int, var pauseChargingTemp: Int, var waitSeconds: Int): Parcelable {
    fun updateAcc() {
        AccUtils.updateTemp(coolDownTemp, pauseChargingTemp, waitSeconds)
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
    fun updateAcc() {
        capacity.updateAcc()
        cooldown?.updateAcc()
        temp.updateAcc()
        AccUtils.updateResetUnplugged(resetUnplugged)
        AccUtils.updateOnBootExit(onBootExit)
        AccUtils.updateOnBoot(onBoot)
    }
}