package mattecarra.accapp.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules_table")
data class ScheduleProfile(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @Embedded var accConfig: AccConfig
)
