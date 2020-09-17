package mattecarra.accapp.acc._interface

import androidx.annotation.WorkerThread
import mattecarra.accapp.models.AccConfig

interface AccInterfaceV2 : AccInterfaceV1 {

    @WorkerThread
    @Throws(KotlinNullPointerException::class)
    fun parseConfig(config: String) : AccConfig
}