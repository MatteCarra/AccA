package mattecarra.accapp.acc

import android.content.Context
import android.os.Environment
import android.os.Handler
import com.topjohnwu.superuser.Shell
import mattecarra.accapp.adapters.Schedule
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.BatteryInfo
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL


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

    fun updateAccConfig(accConfig: AccConfig): ConfigUpdateResult
}

object Acc {
    private val VERSION_REGEXP = """^\s*versionCode=([\d*]+)""".toRegex(RegexOption.MULTILINE)

    private const val latestVersion = 201905111

    val instance: AccInterface by lazy {
        val constructor = try {
            val configFile =
                if(File(Environment.getExternalStorageDirectory(), "acc/acc.conf").exists())
                    File(Environment.getExternalStorageDirectory(), "acc/acc.conf")
                else
                    File(Environment.getExternalStorageDirectory(), "acc/config.txt")

            val config = configFile.readText()

            val version = VERSION_REGEXP.find(config)?.destructured?.component1()?.toIntOrNull() ?: latestVersion

            val aClass = Class.forName("mattecarra.accapp.acc.v$version.AccHandler")
            aClass.getDeclaredConstructor()
        } catch (ex: Exception) {
            val aClass = Class.forName("mattecarra.accapp.acc.v$latestVersion.AccHandler")
            aClass.getDeclaredConstructor()
        }

        constructor.newInstance() as AccInterface
    }

    fun isAccInstalled(): Boolean {
        return Shell.su("which acc > /dev/null").exec().isSuccess
    }

    fun installAccModule(context: Context): Shell.Result? {
        try {
            val scriptFile = File(context.filesDir, "install-latest.sh")
            val path = scriptFile.absolutePath

            BufferedInputStream(URL("https://raw.githubusercontent.com/VR-25/acc/master/install-latest.sh").openStream())
                .use { inStream ->
                    FileOutputStream(scriptFile)
                        .use {
                            val buf = ByteArray(1024)
                            var bytesRead = inStream.read(buf, 0, 1024)

                            while (bytesRead != -1) {
                                it.write(buf, 0, bytesRead)
                                bytesRead = inStream.read(buf, 0, 1024)
                            }
                        }
                }

            return Shell.su("sh $path").exec()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            return null
        }
    }
}
