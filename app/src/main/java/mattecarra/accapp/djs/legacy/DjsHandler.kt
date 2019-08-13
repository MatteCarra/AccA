package mattecarra.accapp.djs.legacy

import com.topjohnwu.superuser.Shell
import mattecarra.accapp.djs.DjsInterface

class DjsHandler: DjsInterface {
    override fun list(pattern: String): List<String> {
        return Shell.su("djsc -l $pattern").exec().out
    }

    override fun append(line: String): Boolean {
        return Shell.su("djsc -a $line").exec().isSuccess
    }

    override fun delete(pattern: String): Boolean {
        return Shell.su("djsc --delete $pattern").exec().isSuccess
    }
}