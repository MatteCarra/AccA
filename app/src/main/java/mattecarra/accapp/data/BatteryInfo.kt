package mattecarra.accapp.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * A POJO for recording and reading data from 'acc -i'.
 *
 * @param name
 * @param isInputSuspend
 * @param status Status of the charger (Charging or Discharging).
 * @param health Physical health of the battery.
 * @param present
 * @param chargeType Speed of the charging (Fast or Slow).
 * @param capacity Returns current battery percentage.
 * @param chargerTemp Returns charger temperature.
 * @param chargerTempMax Returns max (reported?) charger temperature.
 * @param isInputCurrentLimited Returns if the input is current limited.
 * @param voltageNow Battery's measured voltage.
 * @param voltageMax
 * @param voltageQnovo
 * @param currentNow Battery's measured current.
 * @param currentQnovo
 * @param constantChargeCurrentMax
 * @param temperature Temperature of the battery.
 * @param technology Returns the battery technology.
 * @param isStepChargingEnabled
 * @param isSwJeitaEnabled
 * @param isTaperControlEnabled
 * @param isChargeDisabled Returns if charging is disabled by the module.
 * @param isChargeDone Returns if the device is done charging.
 * @param isParallelDisabled
 * @param setShipMode
 * @param dieHealth
 * @param rerunAicl Returns if you need to re-run AICL.
 * @param dpDm
 * @param chargeControlLimitMax
 * @param chargeControlLimit
 * @param inputCurrentMax
 * @param cycleCount Returns the number of charge cycles completed by the battery.
 */
@Parcelize
class BatteryInfo(val name: String,
                  val isInputSuspend: Boolean,
                  val status: String,
                  val health: String,
                  val present: Int,
                  val chargeType: String,
                  val capacity: Int,
                  val chargerTemp: Int,
                  val chargerTempMax: Int,
                  val isInputCurrentLimited: Boolean,
                  val voltageNow: Int,
                  val voltageMax: Int,
                  val voltageQnovo: Int,
                  val currentNow: Int,
                  val currentQnovo: Int,
                  val constantChargeCurrentMax: Int,
                  val temperature: Int,
                  val technology: String,
                  val isStepChargingEnabled: Boolean,
                  val isSwJeitaEnabled: Boolean,
                  val isTaperControlEnabled: Boolean,
                  val isChargeDisabled: Boolean,
                  val isChargeDone: Boolean,
                  val isParallelDisabled: Boolean,
                  val setShipMode: Boolean,
                  val dieHealth: String,
                  val rerunAicl: Boolean,
                  val dpDm: Boolean,
                  val chargeControlLimitMax: Int,
                  val chargeControlLimit: Int,
                  val inputCurrentMax: Int,
                  val cycleCount: Int): Parcelable {

    /**
     * Returns voltage now as float.
     * @return current battery operating voltage.
     */
    fun getVoltageNow(): Float {

        if (voltageNow > 1000000) {
            return voltageNow / 1000000f
        } else {
            return voltageNow / 1000f
        }
    }

    /**
     * Returns inverted, friendly value for CURRENT_NOW. Divided by 1000 to get mAh, from uAh.
     * @return current mAh draw.
     */
    fun getSimpleCurrentNow(): Int {

        return (currentNow/1000)
    }
}