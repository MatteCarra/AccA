package mattecarra.accapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import mattecarra.accapp.models.AccaProfile

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles_table ORDER BY uid DESC")
    fun getAllProfiles(): LiveData<List<AccaProfile>>

    @Insert
    suspend fun insert(accaProfile: AccaProfile)

    @Update
    suspend fun update(accaProfile: AccaProfile)

    @Delete
    suspend fun delete(accaProfile: AccaProfile)

    @Query("SELECT * FROM profiles_table WHERE profileName == :name")
    fun getProfileByName(name: String): List<AccaProfile>

    @Query("SELECT * FROM profiles_table WHERE uid == :id")
    suspend fun getProfileById(id: Int): AccaProfile

    @Query("DELETE FROM profiles_table")
    suspend fun deleteAll()
}