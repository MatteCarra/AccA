package mattecarra.accapp.djs

import android.content.Context
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.R
import java.io.File
import java.io.FileOutputStream

interface DjsInterface {
    fun list(pattern: String = "."): List<String>

    fun append(line: String): Boolean

    fun delete(pattern: String): Boolean
}

object Djs {
    const val bundledVersion = 201907010
    private const val defaultVersionPackage = "v201907010" /* NOTE: default version has to match a package in acc (ex mattecarra.accapp.djs.*) */

    /*
    * This method returns the name of the package with a compatible AccInterface
    * Note: there won't be a package per version. There will be a package for every uncompatible version
    * Ex: if releases from 201903071->201907211 are all compatible there will only be a package, but if a new release is incompatible a new package is created
    * */
    private fun getVersionPackageName(v: Int): String {
        return when {
            else           -> "legacy"
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

    private fun createDjsInstance(): DjsInterface {
        val constructor = try {
            val version = getDjsVersion()
            val aClass = Class.forName("mattecarra.accapp.djs.${getVersionPackageName(version)}.DjsHandler")
            aClass.getDeclaredConstructor()
        } catch (ex: Exception) {
            val aClass = Class.forName("mattecarra.accapp.djs.$defaultVersionPackage.DjsHandler")
            aClass.getDeclaredConstructor()
        }

        INSTANCE = constructor.newInstance() as DjsInterface

        return INSTANCE as DjsInterface
    }

    fun isBundledDjsInstalled(installationDir: File): Boolean {
        return Shell.su("test -f ${File(installationDir, "djs/djs-init.sh").absolutePath}").exec().isSuccess
    }

    fun initBundledDjs(installationDir: File): Boolean {
        return if(isBundledDjsInstalled(installationDir))
            Shell.su("sh ${File(installationDir, "djs/djs-init.sh").absolutePath}").exec().isSuccess
        else
            false
    }

    fun isInstalledDjsOutdated(): Boolean {
        return getDjsVersion() < bundledVersion
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
    private suspend fun installLocalDjsModule(context: Context): Shell.Result? = withContext(
        Dispatchers.IO){
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

            val res = Shell.su("sh -x ${installShFile.absolutePath} djs > ${File(logDir, "djs-install.log").absolutePath} 2>&1").exec()
            createDjsInstance()
            res
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            null
        }
    }

    private fun getDjsVersion(): Int {
        return bundledVersion //There's no way to get djs version yet
    }
}