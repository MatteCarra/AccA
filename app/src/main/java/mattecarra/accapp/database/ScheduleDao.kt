package mattecarra.accapp.database

import androidx.room.*
import mattecarra.accapp.models.ScheduleProfile

@Dao
interface ScheduleDao {
    @Insert
    suspend fun insert(schedule: ScheduleProfile): Long

    @Update
    suspend fun update(schedule: ScheduleProfile)

    @Delete
    suspend fun delete(accaProfile: ScheduleProfile)

    @Query("DELETE FROM schedules_table WHERE uid == :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM schedules_table WHERE uid == :id")
    suspend fun getScheduleById(id: Int): ScheduleProfile?

    @Query("DELETE FROM schedules_table")
    suspend fun deleteAll()
}