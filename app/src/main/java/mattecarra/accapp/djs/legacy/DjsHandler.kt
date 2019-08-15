package mattecarra.accapp.djs.legacy

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.djs.DjsInterface
import mattecarra.accapp.models.Schedule

class DjsHandler: DjsInterface {
    val SCHEDULE = """^\s*([0-9]{2})([0-9]{2}) (.*)$""".toRegex(RegexOption.MULTILINE)

    override suspend fun list(pattern: String): List<Schedule> = withContext(Dispatchers.IO) {
        Shell.su("djsc --list \"$pattern\"").exec().out.map { line ->
            SCHEDULE.find(line)?.destructured?.let {(hour: String, minute: String, command: String) ->
                Schedule(hour.toInt(), minute.toInt(), command)
            }
        }.filterNotNull()
    }

    override suspend fun append(line: String): Boolean = withContext(Dispatchers.IO) {
        Shell.su("djsc --append \"$line\"").exec().isSuccess
    }

    override suspend fun delete(pattern: String): Boolean = withContext(Dispatchers.IO) {
        Shell.su("djsc --delete \"$pattern\"").exec().isSuccess
    }
}