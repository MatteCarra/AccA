package mattecarra.accapp.djs.legacy

import com.topjohnwu.superuser.Shell
import mattecarra.accapp.djs.DjsInterface

class DjsHandler: DjsInterface {
    override fun list(): List<String> {
        return Shell.su("acc -s s-").exec().out
    }

    override fun append(line: String): Boolean {
        return Shell.su("acc -s s-").exec().isSuccess
    }

    override fun delete(pattern: String): Boolean {
        return Shell.su("acc -s s-").exec().isSuccess
    }
}