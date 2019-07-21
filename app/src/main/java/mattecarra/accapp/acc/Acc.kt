package mattecarra.accapp.acc

import android.content.Context
import com.topjohnwu.superuser.Shell
import mattecarra.accapp.R
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

    fun getCurrentChargingSwitch(config: String): String?

    fun setChargingLimitForOneCharge(limit: Int): Boolean

    fun updateAccConfig(accConfig: AccConfig): ConfigUpdateResult

    //reset unplugged command
    fun updateResetUnplugged(resetUnplugged: Boolean): Boolean

    /**
     * Updates the cool down charge and pause durations.
     * @param charge seconds to charge for during the cool down phase.
     * @param pause seconds to pause for during the cool down phase.
     * @return boolean if the command was successful.
     */
    fun updateAccCoolDown(charge: Int?, pause: Int?) : Boolean

    /**
     * Updates the capacity related settings of ACC.
     * @param shutdown shutdown the device at the specified percentage.
     * @param coolDown starts the cool down phase at the specified percentage.
     * @param resume allows charging starting from the specified capacity.
     * @param pause pauses charging at the specified capacity.
     * @return boolean if the command was successful.
     */
    fun updateAccCapacity(shutdown: Int, coolDown: Int, resume: Int, pause: Int) : Boolean

    /**
     * Updates the temperature related configuration in ACC.
     * @param coolDownTemperature starts cool down phase at the specified temperature.
     * @param pauseTemperature pauses charging at the specified temperature.
     * @param wait seconds to wait until charging is resumed.
     * @return the boolean result of the command's execution.
     */
    fun updateAccTemperature(coolDownTemperature: Int, temperatureMax: Int, wait: Int) : Boolean

    /**
     * Updates the voltage related configuration in ACC.
     * @param voltFile path to the voltage file on the device.
     * @param voltMax maximum voltage the phone should charge at.
     * @return the boolean result of the command's execution.
     */
    fun updateAccVoltControl(voltFile: String?, voltMax: Int?) : Boolean

    /**
     * Updates the on boot exit (boolean) configuration in ACC.
     * @param enabled boolean: if OnBootExit should be enabled.
     * @return the boolean result of the command's execution.
     */
    fun updateAccOnBootExit(enabled: Boolean) : Boolean

    /**
     * Updates the OnBoot command configuration in ACC.
     * @param command the command to be run after the device starts (daemon starts).
     * @return the boolean result of the command's execution.
     */
    fun updateAccOnBoot(command: String?) : Boolean

    /**
     * Updates the OnPlugged configuration in ACC.
     * @param command the command to be run when the device is plugged in.
     * @return the boolean result of the command's execution.
     */
    fun updateAccOnPlugged(command: String?) : Boolean
    fun updateAccChargingSwitch(switch: String?) : Boolean
}

object Acc {
    const val bundledVersion = 201907210
    private const val defaultVersion = 201905111 /* NOTE: default version has to match a package in acc (ex mattecarra.accapp.acc.v*) */

    /*
    *
    * */
    private fun getVersionPackageName(v: Int): Int {
        return when {
            v >= 201903071 -> 201903071
            else           -> 201905111
        }
    }

    val instance: AccInterface by lazy {
        val constructor = try {
            val version = Shell.su("""acc --config sed -n 's/^versionCode=//p'""").exec().out.joinToString(separator = "\n").trim().toIntOrNull() ?: defaultVersion
            val aClass = Class.forName("mattecarra.accapp.acc.v${getVersionPackageName(version)}.AccHandler")
            aClass.getDeclaredConstructor()
        } catch (ex: Exception) {
            val aClass = Class.forName("mattecarra.accapp.acc.v$defaultVersion.AccHandler")
            aClass.getDeclaredConstructor()
        }

        constructor.newInstance() as AccInterface
    }

    fun isBundledAccInstalled(installationDir: File): Boolean {
        return Shell.su("test -f ${File(installationDir, "acc/acc-init.sh").absolutePath}").exec().isSuccess
    }

    fun isInstalledAccOutdated(): Boolean {
        return getAccVersion() ?: 0 < bundledVersion ?: 0
    }

    //TODO run this every time an acc instance is created to ensure that acc is available.
    fun initBundledAcc(installationDir: File): Boolean {
        return if(isBundledAccInstalled(installationDir))
            Shell.su("sh ${File(installationDir, "acc/acc-init.sh").absolutePath}").exec().isSuccess
        else
            false
    }

    private fun getAccVersion(): Int? {
        return Shell.su("acc --version").exec().out.joinToString(separator = "\n").trim().toIntOrNull() ?: defaultVersion
    }

    fun isAccInstalled(): Boolean {
        return Shell.su("which acc > /dev/null").exec().isSuccess
    }

    fun installBundledAccModule(context: Context): Shell.Result? {
        return try {
            val bundleFile = File(context.filesDir, "acc_bundle.tar.gz")
            val installShFile = File(context.filesDir, "install-tarball.sh")

            context.resources.openRawResource(R.raw.acc_bundle).use { out ->
                FileOutputStream(bundleFile).use {
                    out.copyTo(it)
                }
            }

            context.resources.openRawResource(R.raw.install).use { installer ->
                FileOutputStream(installShFile).use {
                    installer.copyTo(it)
                }
            }

            Shell.su("sh -x ${installShFile.absolutePath} > /data/local/tmp/acc-install.log 2>&1").exec()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            null
        }
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
