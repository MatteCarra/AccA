package mattecarra.accapp.acc

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import mattecarra.accapp.Preferences
import mattecarra.accapp.R
import mattecarra.accapp.adapters.Schedule
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.BatteryInfo
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.abs


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
    const val bundledVersion = 201907211
    private const val defaultVersionPackage = "v201905111" /* NOTE: default version has to match a package in acc (ex mattecarra.accapp.acc.*) */

    /*
    * This method returns the name of the package with a compatible AccInterface
    * Note: there won't be a package per version. There will be a package for every uncompatible version
    * Ex: if releases from 201903071->201907211 are all compatible there will only be a package, but if a new release is incompatible a new package is created
    * */
    private fun getVersionPackageName(v: Int): String {
        return when {
            v >= 201903071 -> "v201903071"
            else           -> "legacy" /* This is used for all the versions before v20190371*/
        }
    }

    @Volatile
    private var INSTANCE: AccInterface? = null

    val instance: AccInterface
        get() {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                // Create acc instance here
                return initInstance()
            }
        }

    private fun initInstance(): AccInterface {
        val constructor = try {
            val version = getAccVersion()
            val aClass = Class.forName("mattecarra.accapp.acc.${getVersionPackageName(version)}.AccHandler")
            aClass.getDeclaredConstructor()
        } catch (ex: Exception) {
            val aClass = Class.forName("mattecarra.accapp.acc.$defaultVersionPackage.AccHandler")
            aClass.getDeclaredConstructor()
        }

        INSTANCE = constructor.newInstance() as AccInterface

        return INSTANCE as AccInterface
    }

    fun isBundledAccInstalled(installationDir: File): Boolean {
        return Shell.su("test -f ${File(installationDir, "acc/acc-init.sh").absolutePath}").exec().isSuccess
    }

    fun isInstalledAccOutdated(): Boolean {
        return getAccVersion() < bundledVersion
    }

    //TODO run this every time an acc instance is created to ensure that acc is available.
    fun initBundledAcc(installationDir: File): Boolean {
        return if(isBundledAccInstalled(installationDir))
            Shell.su("sh ${File(installationDir, "acc/acc-init.sh").absolutePath}").exec().isSuccess
        else
            false
    }

    fun getAccVersion(): Int {
        return Shell.su("acc --version").exec().out.joinToString(separator = "\n").trim().toIntOrNull() ?: bundledVersion
    }

    suspend fun installBundledAccModule(context: Context): Shell.Result?  = withContext(Dispatchers.IO) {
        try {
            val bundleFile = File(context.filesDir, "acc_bundle.tar.gz")

            context.resources.openRawResource(R.raw.acc_bundle).use { out ->
                FileOutputStream(bundleFile).use {
                    out.copyTo(it)
                }
            }

            installLocalAccModule(context)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            null
        }
    }

    suspend fun installAccModuleVersion(context: Context, version: String): Shell.Result?  = withContext(Dispatchers.IO) {
        try {
            val bundleFile = File(context.filesDir, "acc_bundle.tar.gz")

            BufferedInputStream(URL("https://github.com/VR-25/acc/archive/$version.tar.gz").openStream())
                .use { inStream ->
                    FileOutputStream(bundleFile)
                        .use {
                            val buf = ByteArray(1024)
                            var bytesRead = inStream.read(buf, 0, 1024)

                            while (bytesRead != -1) {
                                it.write(buf, 0, bytesRead)
                                bytesRead = inStream.read(buf, 0, 1024)
                            }
                        }
                }

            installLocalAccModule(context)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            null
        }
    }

    /*
    * This function assumes that acc tar gz is already in place
    */
    private suspend fun installLocalAccModule(context: Context): Shell.Result? = withContext(Dispatchers.IO){
        try {
            val installShFile = File(context.filesDir, "install-tarball.sh")

            context.resources.openRawResource(R.raw.install).use { installer ->
                FileOutputStream(installShFile).use {
                    installer.copyTo(it)
                }
            }

            val logDir = File(context.filesDir, "logs")
            if(!logDir.exists())
                logDir.mkdir()

            val res = Shell.su("sh -x ${installShFile.absolutePath} > ${File(logDir, "acc-install.log").absolutePath} 2>&1").exec()
            initInstance()
            calibrateMeasurements(context)
            res
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            null
        }
    }

    private suspend fun calibrateMeasurements(context: Context) = withContext(Dispatchers.IO) {
        var microVolts = 0
        var microAmpere = 0

        for (i in 0..10) {
            val batteryInfo = Acc.instance.getBatteryInfo()
            if(batteryInfo.getRawVoltageNow() > 1000000)
                microVolts++
            if(abs(batteryInfo.getRawCurrentNow()) > 10000)
                microAmpere++

            delay(250)
        }

        val preferences = Preferences(context)
        preferences.uACurrent = microAmpere >= 6
        preferences.uVMeasureUnit = microVolts >= 6
    }

    suspend fun listAccVersions(): List<String> = withContext(Dispatchers.IO) {
        (try {
            JsonParser()
                .parse(URL("https://api.github.com/repos/VR-25/acc/tags").readText())
                .asJsonArray
        } catch (ignored: java.lang.Exception) {
            JsonArray()
        }).map {
            it.asJsonObject["name"].asString
        }
    }
}
