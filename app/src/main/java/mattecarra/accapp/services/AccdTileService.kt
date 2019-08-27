package mattecarra.accapp.services

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import kotlin.coroutines.CoroutineContext

@TargetApi(Build.VERSION_CODES.N)
class AccdTileService: TileService(), CoroutineScope {
    private val LOG_TAG = "AccdTileService"

    protected lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate() {
        super.onCreate()
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onClick() {
        super.onClick()

        launch {
            val accdRunning = Acc.instance.isAccdRunning()

            _updateTile(!accdRunning)

            //This will give the user the feeling that his actions are handled immediately. I hope no one will spam on the button.
            //otherwise this enable/disable cycle will take forever
            //I've considered to disable the tile while it's updating but it doesn't look great and google is not doing either with wifi.
            synchronized(this) {
                if (accdRunning) {
                    val tile = qsTile
                    tile.label = getString(R.string.wait) //stop deamon a bit, so I moved _updateTile before that
                    tile.updateTile()

                    //TODO add a mutex instead of relaunching the coroutine
                    launch {
                        Acc.instance.abcStopDaemon()
                        tile.label = getString(R.string.tile_acc_disabled)
                        tile.updateTile()
                    }
                } else {
                    launch {
                        Acc.instance.abcStartDaemon()
                    }
                }
            }
        }
    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        launch {
            Acc.instance.abcStartDaemon()
            // Do something when the user removes the Tile
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
        // Do something when the user add the Tile
    }

    override fun onStartListening() {
        super.onStartListening()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //TODO create a notification and ask for read external storage permission
        }else {
            updateTile()
        }
    }

    private fun updateTile() {
        if(Shell.rootAccess()) {
            _updateTile()
        } else {
            val tile = qsTile
            tile.label = getString(R.string.tile_acc_no_root)
            tile.state = Tile.STATE_UNAVAILABLE
            tile.icon = Icon.createWithResource(this, R.drawable.ic_battery_charging_full)
            tile.updateTile() // you need to call this method to apply changes
        }
    }

    private fun _updateTile(accdRunning: Boolean? = null, charging: Boolean? = null){
        launch {
            val mAccdRunning = accdRunning?: Acc.instance.isAccdRunning()
            val mCharging = charging ?: Acc.instance.isBatteryCharging()

            Log.d(LOG_TAG, "_updateTile $mAccdRunning $mCharging")

            val tile = qsTile
            tile.label = getString(if(mAccdRunning) R.string.tile_acc_enabled else R.string.tile_acc_disabled)
            tile.state = if(mAccdRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.icon =
                Icon.createWithResource(this@AccdTileService,
                    if(mAccdRunning)
                        if(mCharging)
                            R.drawable.ic_battery_charging_80
                        else
                            R.drawable.ic_battery_80
                    else
                        if(mCharging)
                            R.drawable.ic_battery_charging_full
                        else
                            R.drawable.ic_battery_full
                )
            tile.updateTile() // you need to call this method to apply changes
        }
    }
}