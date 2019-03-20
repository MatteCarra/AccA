package mattecarra.accapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import mattecarra.accapp.models.Profile

@Database(entities = [Profile::class], version = 1)
public abstract class RoomAccaDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: RoomAccaDatabase? = null

        fun getDatabase(context: Context): 
    }

    abstract fun profileDao(): ProfileDao
}