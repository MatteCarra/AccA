package mattecarra.accapp.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "scripts_table")
data class AccaScript(
    @PrimaryKey(autoGenerate = true) var uid: Int, // NEED var for DUPLICATE INSERT
    var scName: String,
    var scDescription: String,
    var scBody: String,
    var scOutput : String,
    var scExitCode: Int
) : Serializable
