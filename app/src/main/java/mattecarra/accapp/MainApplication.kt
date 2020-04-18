package mattecarra.accapp

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.topjohnwu.superuser.Shell

class MainApplication: MultiDexApplication() {
    companion object {
        init {
            Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
            Shell.Config.verboseLogging(BuildConfig.DEBUG)
            Shell.Config.setTimeout(10)
        }
    }
}