package mattecarra.accapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import mattecarra.accapp.models.ProfileEntity

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles_table ORDER BY uid DESC")
    fun getAllProfiles(): LiveData<List<ProfileEntity>>

    @Insert
    fun insert(profileEntity: ProfileEntity)

    @Update
    fun update(profileEntity: ProfileEntity)

    @Delete
    fun delete(profileEntity: ProfileEntity)

    @Query("SELECT * FROM profiles_table WHERE profileName == :name")
    fun getProfileByName(name: String): List<ProfileEntity>

    @Query("DELETE FROM profiles_table")
    fun deleteAll()
}