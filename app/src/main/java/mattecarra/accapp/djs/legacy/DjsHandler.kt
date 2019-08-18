package mattecarra.accapp.djs.legacy

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.djs.DjsInterface
import mattecarra.accapp.djs.DjsSchedule
import mattecarra.accapp.models.Schedule

class DjsHandler: DjsInterface {
    val SCHEDULE = """^\s*([0-9]{2})([0-9]{2}) (.*)$""".toRegex(RegexOption.MULTILINE)
    val ID_REGEX = """^accaScheduleId=(\d*)""".toRegex()

    override suspend fun list(pattern: String): List<DjsSchedule> = withContext(Dispatchers.IO) {
        Shell.su("djsc --list '$pattern'").exec().out.map { line ->
            SCHEDULE.find(line)?.destructured?.let { (hour: String, minute: String, command: String) ->
                ID_REGEX.find(command)?.destructured?.component1()?.toIntOrNull()?.let { id ->
                    DjsSchedule(id, hour.toInt(), minute.toInt(), command)
                }
            }
        }.filterNotNull()
    }

    override suspend fun append(line: String): Boolean = withContext(Dispatchers.IO) {
        Shell.su("djsc --append '$line'").exec().isSuccess
    }

    override suspend fun delete(pattern: String): Boolean = withContext(Dispatchers.IO) {
        Shell.su("djsc --delete '$pattern'").exec().isSuccess
    }

    override suspend fun stop(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("djsd-stop").exec().isSuccess
    }
}