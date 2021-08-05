package mattecarra.accapp.djs

import android.content.Context
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder

interface DjsInterface {
    suspend fun list(pattern: String = "."): List<DjsSchedule>

    suspend fun append(line: String): Boolean

    suspend fun append(schedule: DjsSchedule): Boolean {
        val command = StringBuilder()
        if(!schedule.isEnabled)
            command.append("//")

        return append(command.append("${schedule.time} ${schedule.command}").toString())
    }

    suspend fun edit(pattern: String, newLine: String): Boolean

    suspend fun edit(schedule: DjsSchedule): Boolean {
        val command = StringBuilder()
        if(!schedule.isEnabled)
            command.append("//")

        return edit(
            ": accaScheduleId${schedule.scheduleProfileId}",
            command.append("${schedule.time} ${schedule.command}").toString()
        )
    }

    suspend fun delete(pattern: String): Boolean

    suspend fun deleteById(id: Int): Boolean {
        return delete(": accaScheduleId$id")
    }

    suspend fun delete(schedule: DjsSchedule): Boolean {
        return deleteById(schedule.scheduleProfileId)
    }

    suspend fun stop(): Boolean
}

object Djs {
    const val bundledVersion = 202108020

    /*
    * This method returns the name of the package with a compatible AccInterface
    * Note: there won't be a package per version. There will be a package for every uncompatible version
    * Ex: if releases from 201903071->201907211 are all compatible there will only be a package, but if a new release is incompatible a new package is created
    * */
    private fun getDjsInterfaceForversion(v: Int): DjsInterface {
        return when {
            v >= 202107280 -> mattecarra.accapp.djs.v202107280.DjsHandler()
            else           -> mattecarra.accapp.djs.legacy.DjsHandler()
        }
    }

    @Volatile
    private var INSTANCE: DjsInterface? = null

    val instance: DjsInterface
        get() {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                // Create djs instance here
                return createDjsInstance()
            }
        }

    private fun createDjsInstance(version: Int = getDjsVersion() ?: bundledVersion): DjsInterface {
        INSTANCE = getDjsInterfaceForversion(version)
        return INSTANCE as DjsInterface
    }

    fun isDjsInstalled(installationDir: File): Boolean {
        return Shell.su("test -f ${File(installationDir, "djs/service.sh").absolutePath}  || test -f ${File(installationDir, "djs/djs-init.sh").absolutePath}").exec().isSuccess
    }

    fun initDjs(installationDir: File): Boolean {
        return if(isDjsInstalled(installationDir))
            Shell.su("if test -f ${File(installationDir, "djs/service.sh").absolutePath}; then ${File(installationDir, "djs/service.sh").absolutePath}; else ${File(installationDir, "djs/djs-init.sh").absolutePath}; fi").exec().isSuccess
        else
            false
    }

    fun isInstalledDjsOutdated(): Boolean {
        return getDjsVersion()?.let { it < bundledVersion } ?: true
    }

    suspend fun installBundledAccModule(context: Context): Shell.Result?  = withContext(Dispatchers.IO) {
        try {
            val bundleFile = File(context.filesDir, "djs_bundle.tar.gz")

            context.resources.openRawResource(R.raw.djs_bundle).use { out ->
                FileOutputStream(bundleFile).use {
                    out.copyTo(it)
                }
            }

            installLocalDjsModule(context)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            null
        }
    }

    /*
    * This function assumes that acc tar gz is already in place
    */
    private suspend fun installLocalDjsModule(context: Context): Shell.Result? = withContext(Dispatchers.IO){
        try {
            val installShFile = File(context.filesDir, "install-tarball.sh")

            context.resources.openRawResource(R.raw.install).use { installer ->
                FileOutputStream(installShFile).use {
                    installer.copyTo(it)
                }
            }

            val res = Shell.su("sh ${installShFile.absolutePath} djs").exec()

            val version = getDjsVersion() ?: throw java.lang.Exception("DJS installation failed")

            createDjsInstance(version)
            res
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            null
        }
    }

    suspend fun uninstallDjs(installationDir: File): Shell.Result? = withContext(Dispatchers.IO) {
        Shell.su("sh ${File(installationDir, "djs/uninstall.sh").absolutePath}").exec()
    }

    private fun getDjsVersion(): Int? {
        return Shell.su("/dev/.vr25/djs/djs-version").exec().out.joinToString(separator = "\n").trim().toIntOrNull()
    }
}