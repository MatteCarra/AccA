package mattecarra.accapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile

@Database(entities = [AccaProfile::class], version = 1)
@TypeConverters(ConfigConverter::class)
abstract class AccaRoomDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    // abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AccaRoomDatabase? = null

        const val DATABASE_NAME = "acca_database"

        fun getDatabase(context: Context): AccaRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                // Create database instance here
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AccaRoomDatabase::class.java,
                    DATABASE_NAME
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        Thread(Runnable { prepopulateDb(context, getDatabase(context)) }).start()
                    }
                }).build()

                return INSTANCE as AccaRoomDatabase
            }
        }

        private fun prepopulateDb(context: Context, db: AccaRoomDatabase) {
            //TODO
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

}