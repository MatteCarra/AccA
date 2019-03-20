package mattecarra.accapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mattecarra.accapp.models.Profile

@Database(entities = [Profile::class], version = 1)
public abstract class AccaRoomDatabase : RoomDatabase()

val DATABASE_NAME = "acca_database"

companion object {
    @Volatile
    private var INSTANCE: AccaRoomDatabase? = null

    fun getDatabase(context: Context): AccaRoomDatabase {

        return INSTANCE ?: synchronized(this) {
            // Create database instance here
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AccaRoomDatabase::class.java,
                DATABASE_NAME
            ).build()

            
        }
    }
}

abstract fun profileDao(): ProfileDao
}