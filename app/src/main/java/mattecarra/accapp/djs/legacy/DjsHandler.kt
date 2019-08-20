package mattecarra.accapp.djs.legacy

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mattecarra.accapp.djs.DjsInterface
import mattecarra.accapp.djs.DjsSchedule
import mattecarra.accapp.models.Schedule

class DjsHandler: DjsInterface {
    val SCHEDULE = """^\s*([0-9]{4}|boot) (.*)""".toRegex(RegexOption.MULTILINE)
    val ID_REGEX = """^: accaScheduleId(\d*)""".toRegex()
    val EXECUTE_ONCE_MATCH_REGEX = { id: Int -> """djsc --delete : accaScheduleId$id""".toPattern() }

    override suspend fun list(pattern: String): List<DjsSchedule> = withContext(Dispatchers.IO) {
        Shell.su("djsc --list '$pattern'").exec().out.map { line ->
            SCHEDULE.find(line)?.destructured?.let { (time: String, command: String) ->
                ID_REGEX.find(command)?.destructured?.component1()?.toIntOrNull()?.let { id ->
                    val test = EXECUTE_ONCE_MATCH_REGEX(id).matcher(command).find()
                    DjsSchedule(id, time, test, command)
                }
            }
        }.filterNotNull()
    }

    override suspend fun append(line: String): Boolean = withContext(Dispatchers.IO) {
        Shell.su("djsc --append '$line'").exec().isSuccess
    }

    override fun getDeleteCommand(pattern: String): String = "djsc --delete '$pattern'"


    override suspend fun delete(pattern: String): Boolean = withContext(Dispatchers.IO) {
        Shell.su(getDeleteCommand(pattern)).exec().isSuccess
    }

    override suspend fun stop(): Boolean = withContext(Dispatchers.IO) {
        Shell.su("djsd-stop").exec().isSuccess
    }
}