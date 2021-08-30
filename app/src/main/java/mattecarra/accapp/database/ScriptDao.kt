package mattecarra.accapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import mattecarra.accapp.models.AccaScript

@Dao
interface ScriptDao
{
    @Insert
    suspend fun insert(AccaScript: AccaScript)

    @Update
    suspend fun update(AccaScript: AccaScript)

    @Delete
    suspend fun delete(AccaScript: AccaScript)

    @Query("SELECT * FROM scripts_table WHERE scName == :name")
    fun getScriptByName(name: String): List<AccaScript>

    @Query("SELECT * FROM scripts_table WHERE uid == :id")
    suspend fun getScriptById(id: Int): AccaScript?

    @Query("DELETE FROM scripts_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM scripts_table ORDER BY uid DESC")
    suspend fun getScript(): List<AccaScript>

    @Query("SELECT * FROM scripts_table ORDER BY uid DESC")
    fun getAllScripts(): LiveData<List<AccaScript>>
}