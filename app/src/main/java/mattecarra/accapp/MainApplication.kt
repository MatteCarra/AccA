package mattecarra.accapp

import android.app.Application
import android.content.Context
import com.topjohnwu.superuser.Shell

class MainApplication: Application() {
    companion object {
        init {
            Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
            Shell.Config.verboseLogging(false /*BuildConfig.DEBUG*/)
            Shell.Config.setTimeout(10)
        }
    }
}