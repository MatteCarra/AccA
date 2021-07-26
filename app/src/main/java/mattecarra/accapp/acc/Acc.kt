package mattecarra.accapp.acc

import android.content.Context
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import mattecarra.accapp.CurrentUnit
import mattecarra.accapp.Preferences
import mattecarra.accapp.R
import mattecarra.accapp.VoltageUnit
import mattecarra.accapp.acc._interface.AccInterface
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.abs

object Acc {
    const val bundledVersion = 202107240
    private val FILES_DIR = "/data/data/mattecarra.accapp/files"

    /*
    * This method returns the name of the package with a compatible AccInterface
    * Note: there won't be a package per version. There will be a package for every uncompatible version
    * Ex: if releases from 201903071->201907211 are all compatible there will only be a package, but if a new release is incompatible a new package is created
    * */
    private fun getVersionPackageName(v: Int): String {
        return when {
            v >= 202007220 -> "v202007220"
            v >= 202007030 -> "v202007030"
            v >= 202006140 -> "v202006140"
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
                initAcc(File(FILES_DIR))
                return createAccInstance()
            }
        }

    internal fun createAccInstance(version: Int = getAccVersion() ?: bundledVersion): AccInterface{
        return try {
            val aClass = Class.forName("mattecarra.accapp.acc.${getVersionPackageName(version)}.AccHandler")
            INSTANCE = (aClass.getDeclaredConstructor(Int::class.java).newInstance(bundledVersion) as AccInterface)
            INSTANCE as AccInterface
        } catch (ex: Exception) {
            ex.printStackTrace()
            createAccInstance(bundledVersion)
        }
    }

    fun isAccInstalled(installationDir: File): Boolean {
        return Shell.su("test -f ${File(installationDir, "acc/service.sh").absolutePath}  || test -f ${File(installationDir, "acc/acc-init.sh").absolutePath}").exec().isSuccess
    }

    fun isInstalledAccOutdated(): Boolean {
        return getAccVersion()?.let { it < bundledVersion } ?: true
    }

    fun initAcc(installationDir: File): Boolean {
        return if(isAccInstalled(installationDir))
            Shell.su("/dev/acca --daemon 2>/dev/null || if test -f ${File(installationDir, "acc/service.sh").absolutePath}; then ${File(installationDir, "acc/service.sh").absolutePath}; else ${File(installationDir, "acc/acc-init.sh").absolutePath}; fi").exec().isSuccess
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

            val version = getAccVersion() ?: throw java.lang.Exception("ACC installation failed")
            if(version >= 202002292) {
                val preferences = Preferences(context)
                preferences.currentUnitOfMeasure = CurrentUnit.A
                preferences.voltageUnitOfMeasure = VoltageUnit.V
            } else if(version >= 202002290) {
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

    private fun getAccVersion(): Int? {
        return Shell.su("/dev/.vr25/acc/acc --version").exec().out.joinToString(separator = "\n").split("(").last().split(")").first().trim().toIntOrNull() ?: getAccVersionLegacy()
    }

    private fun getAccVersionLegacy(): Int? {
        return Shell.su("acc --version").exec().out.joinToString(separator = "\n").split("(").last().split(")").first().trim().toIntOrNull()
    }
}
