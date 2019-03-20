package mattecarra.accapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import mattecarra.accapp.models.Profile

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles_table ORDER BY uid DESC")
    fun getAllProfiles(): LiveData<List<Profile>>

    @Insert
    fun insert(profile: Profile)

    @Query("DELETE FROM profiles_table")
    fun deleteAll()
}