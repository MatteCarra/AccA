package mattecarra.accapp.acc

import android.content.Context
import android.os.Environment
import android.os.Handler
import com.topjohnwu.superuser.Shell
import mattecarra.accapp.adapters.Schedule
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.BatteryInfo
import java.io.File


interface AccInterface {
    val defaultConfig: AccConfig

    fun readConfig(): AccConfig

    fun listVoltageSupportedControlFiles(): List<String>

    fun resetBatteryStats(): Boolean

    fun getBatteryInfo(): BatteryInfo

    fun isBatteryCharging(): Boolean

    fun isAccdRunning(): Boolean

    fun abcStartDaemon(): Boolean

    fun abcRestartDaemon(): Boolean

    fun abcStopDaemon(): Boolean

    fun deleteSchedule(once: Boolean, name: String): Boolean

    fun schedule(once: Boolean, hour: Int, minute: Int, commands: List<String>): Boolean

    fun schedule(once: Boolean, hour: Int, minute: Int, commands: String): Boolean

    fun listSchedules(once: Boolean): List<Schedule>

    fun listAllSchedules(): List<Schedule>

    fun listChargingSwitches(): List<String>

    fun testChargingSwitch(chargingSwitch: String? = null): Int

    fun getCurrentChargingSwitch(): String?

    fun setChargingLimitForOneCharge(limit: Int): Boolean

    fun isAccInstalled(): Boolean

    fun installAccModule(context: Context): Shell.Result?

    fun updateAccConfig(accConfig: AccConfig): ConfigUpdateResult
}

object Acc {
    val instance: AccInterface

    private val VERSION_REGEXP = """^\s*versionCode=([\d*]+)""".toRegex(RegexOption.MULTILINE)

    private const val latestVersion = 201905111

    init {
        val config = File(Environment.getExternalStorageDirectory(), "acc/config.txt").readText()

        val version = VERSION_REGEXP.find(config)?.destructured?.component1()?.toIntOrNull() ?: latestVersion

        val constructor = try {
            val aClass = Class.forName("mattecarra.accapp.acc.v$version.AccHandler")
            aClass.getDeclaredConstructor()
        } catch (ex: ClassNotFoundException) {
            val aClass = Class.forName("mattecarra.accapp.acc.v$latestVersion.AccHandler")
            aClass.getDeclaredConstructor()
        }

        instance = constructor.newInstance() as AccInterface
    }
}