package mattecarra.accapp.acc

import android.content.Context
import androidx.annotation.WorkerThread
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import mattecarra.accapp.CurrentUnit
import mattecarra.accapp.Preferences
import mattecarra.accapp.R
import mattecarra.accapp.VoltageUnit
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.BatteryInfo
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.abs


interface AccInterface {
    suspend fun readConfig(): AccConfig

    suspend fun readDefaultConfig(): AccConfig

    suspend fun listVoltageSupportedControlFiles(): List<String>

    suspend fun resetBatteryStats(): Boolean

    suspend fun getBatteryInfo(): BatteryInfo

    suspend fun isBatteryCharging(): Boolean

    suspend fun isAccdRunning(): Boolean

    suspend fun abcStartDaemon(): Boolean

    suspend fun abcRestartDaemon(): Boolean

    suspend fun abcStopDaemon(): Boolean

    suspend fun listChargingSwitches(): List<String>

    suspend fun testChargingSwitch(chargingSwitch: String? = null): Int

    @WorkerThread
    fun getCurrentChargingSwitch(config: String): String?

    @WorkerThread
    fun isPrioritizeBatteryIdleMode(config: String): Boolean

    suspend fun setChargingLimitForOneCharge(limit: Int): Boolean

    suspend fun isBatteryIdleSupported(): Pair<Int, Boolean>

    suspend fun updateAccConfig(accConfig: AccConfig): ConfigUpdateResult

    /**
     * Updates the OnBoot command configuration in ACC.
     * @param command the command to be run after the device starts (daemon starts).
     * @return the boolean result of the command's execution.
     */
    fun getUpdateAccOnBootCommand(command: String?): String
    suspend fun updateAccOnBoot(command: String?) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccOnBootCommand(command)).exec().isSuccess
    }

    /**
     * Updates the on boot exit (boolean) configuration in ACC.
     * @param enabled boolean: if OnBootExit should be enabled.
     * @return the boolean result of the command's execution.
     */
    fun getUpdateAccOnBootExitCommand(enabled: Boolean): String
    suspend fun updateAccOnBootExit(enabled: Boolean) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccOnBootExitCommand(enabled)).exec().isSuccess
    }

    /**
     * Updates the voltage related configuration in ACC.
     * @param voltFile path to the voltage file on the device.
     * @param voltMax maximum voltage the phone should charge at.
     * @return the boolean result of the command's execution.
     */
    fun getUpdateAccVoltControlCommand(voltFile: String?, voltMax: Int?): String
    suspend fun updateAccVoltControl(voltFile: String?, voltMax: Int?) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccVoltControlCommand(voltFile, voltMax)).exec().isSuccess
    }

    fun getUpdateAccCurrentMaxCommand(currMax: Int?): String
    suspend fun updateAccCurrentMaxCommand(currMax: Int?) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccCurrentMaxCommand(currMax)).exec().isSuccess
    }

    /**
     * Updates the temperature related configuration in ACC.
     * @param coolDownTemperature starts cool down phase at the specified temperature.
     * @param temperatureMax pauses charging at the specified temperature.
     * @param wait seconds to wait until charging is resumed.
     * @return the boolean result of the command's execution.
     */
    fun getUpdateAccTemperatureCommand(coolDownTemperature: Int, temperatureMax: Int, wait: Int): String
    suspend fun updateAccTemperature(coolDownTemperature: Int, temperatureMax: Int, wait: Int) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccTemperatureCommand(coolDownTemperature, temperatureMax, wait)).exec().isSuccess
    }

    /**
     * Updates the capacity related settings of ACC.
     * @param shutdown shutdown the device at the specified percentage.
     * @param coolDown starts the cool down phase at the specified percentage.
     * @param resume allows charging starting from the specified capacity.
     * @param pause pauses charging at the specified capacity.
     * @return boolean if the command was successful.
     */
    fun getUpdateAccCapacityCommand(shutdown: Int, coolDown: Int, resume: Int, pause: Int): String
    suspend fun updateAccCapacity(shutdown: Int, coolDown: Int, resume: Int, pause: Int) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccCapacityCommand(shutdown, coolDown, resume, pause)).exec().isSuccess
    }

    /**
     * Updates the cool down charge and pause durations.
     * @param charge seconds to charge for during the cool down phase.
     * @param pause seconds to pause for during the cool down phase.
     * @return boolean if the command was successful.
     */
    fun getUpdateAccCoolDownCommand(charge: Int?, pause: Int?): String
    suspend fun updateAccCoolDown(charge: Int?, pause: Int?) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccCoolDownCommand(charge, pause)).exec().isSuccess
    }

    fun getUpdateResetUnpluggedCommand(resetUnplugged: Boolean): String
    suspend fun updateResetUnplugged(resetUnplugged: Boolean): Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateResetUnpluggedCommand(resetUnplugged)).exec().isSuccess
    }

    fun getUpdateResetOnPauseCommand(resetOnPause: Boolean): String
    suspend fun updateResetOnPause(resetOnPause: Boolean): Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateResetOnPauseCommand(resetOnPause)).exec().isSuccess
    }

    fun getUpdateAccChargingSwitchCommand(switch: String?): String
    suspend fun updateAccChargingSwitch(switch: String?) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccChargingSwitchCommand(switch)).exec().isSuccess
    }

    /**
     * Updates the OnPlugged configuration in ACC.
     * @param command the command to be run when the device is plugged in.
     * @return the boolean result of the command's execution.
     */
    fun getUpdateAccOnPluggedCommand(command: String?): String
    suspend fun updateAccOnPlugged(command: String?) : Boolean = withContext(Dispatchers.IO) {
        Shell.su(getUpdateAccOnPluggedCommand(command)).exec().isSuccess
    }

    fun getUpgradeCommand(version: String): String
    suspend fun upgrade(version: String): Shell.Result? = withContext(Dispatchers.IO){
        val res = Shell.su(getUpgradeCommand(version)).exec()
        Acc.createAccInstance()
        res
    }

    fun getUpdatePrioritizeBatteryIdleModeCommand(enabled: Boolean): String
    suspend fun updatePrioritizeBatteryIdleMode(enabled: Boolean): Boolean = withContext(Dispatchers.IO){
        Shell.su(getUpdatePrioritizeBatteryIdleModeCommand(enabled)).exec().isSuccess
    }


    fun getAddChargingSwitchCommand(switch: String): String
    suspend fun addChargingSwitch(switch: String): Boolean = withContext(Dispatchers.IO) {
        Shell.su(getAddChargingSwitchCommand(switch)).exec().isSuccess
    }
}

object Acc {
    const val bundledVersion = 202005010
    private val defaultVersionPackage = mattecarra.accapp.acc.v201910132.AccHandler::class.java //Default AccHandler, used wen version is not recognized

    /*
    * This method returns the name of the package with a compatible AccInterface
    * Note: there won't be a package per version. There will be a package for every uncompatible version
    * Ex: if releases from 201903071->201907211 are all compatible there will only be a package, but if a new release is incompatible a new package is created
    * */
    private fun getVersionPackageName(v: Int): String {
        return when {
            v >= 202002290 -> "v202002290"
            v >= 202002170 -> "v202002170"
            v >= 201910130 -> "v201910132"
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
                return createAccInstance()
            }
        }

    internal fun createAccInstance(): AccInterface {
        val constructor = try {
            val version = getAccVersion()
            val aClass = Class.forName("mattecarra.accapp.acc.${getVersionPackageName(version)}.AccHandler")
            aClass.getDeclaredConstructor()
        } catch (ex: Exception) {
            defaultVersionPackage.getDeclaredConstructor()
        }

        INSTANCE = constructor.newInstance() as AccInterface

        return INSTANCE as AccInterface
    }

    fun isAccInstalled(installationDir: File): Boolean {
        return Shell.su("test -f ${File(installationDir, "acc/acc-init.sh").absolutePath}").exec().isSuccess
    }

    fun isInstalledAccOutdated(): Boolean {
        return getAccVersion() < bundledVersion
    }

    //TODO run this every time an acc instance is created to ensure that acc is available.
    fun initAcc(installationDir: File): Boolean {
        return if(isAccInstalled(installationDir))
            Shell.su("sh ${File(installationDir, "acc/acc-init.sh").absolutePath}").exec().isSuccess
        else
            false
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

            val res = Shell.su("sh ${installShFile.absolutePath} acc").exec()
            createAccInstance()

            if(getAccVersion() >= 202002292) {
                val preferences = Preferences(context)
                preferences.currentUnitOfMeasure = CurrentUnit.A
                preferences.voltageUnitOfMeasure = VoltageUnit.V
            } else if(getAccVersion() >= 202002290) {
                val preferences = Preferences(context)
                preferences.currentUnitOfMeasure = CurrentUnit.mA
                preferences.voltageUnitOfMeasure = VoltageUnit.V
            } else {
                calibrateMeasurements(context)
            }

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
        preferences.currentUnitOfMeasure = if(microAmpere >= 6) CurrentUnit.uA else CurrentUnit.mA
        preferences.voltageUnitOfMeasure = if(microVolts >= 6)  VoltageUnit.uV else VoltageUnit.mV
    }

    fun getAccVersion(): Int {
        return Shell.su("acc --version").exec().out.joinToString(separator = "\n").split("(").last().split(")").first().trim().toIntOrNull() ?: bundledVersion
    }
}
