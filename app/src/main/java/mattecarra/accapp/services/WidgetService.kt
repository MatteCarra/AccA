package mattecarra.accapp.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import mattecarra.accapp.receivers.AdvWidgetReceiver
import mattecarra.accapp.utils.LogExt
import xml.*

interface OnAdvWidgetInterface
{
    fun onScreen(screenOn: Boolean)
    fun onPowerState(connectOn: Boolean)
}

class WidgetService : Service(), OnAdvWidgetInterface
{
    private var mAdvWidgetReceiver: AdvWidgetReceiver? = null
    private lateinit var mScreenService: PowerManager
    private lateinit var mWidgetHandler: Handler
    private var isPowerConnected = false
    private var isScreenOn = false

    override fun onBind(intent: Intent?): IBinder?
    {
        return null
    }

    override fun onCreate()
    {
        super.onCreate()

        LogExt().d(javaClass.simpleName, ".onCreate()")
        mScreenService = getSystemService(POWER_SERVICE) as PowerManager
        mWidgetHandler = HandlerCompat.createAsync(Looper.getMainLooper())
        isScreenOn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) mScreenService.isInteractive else mScreenService.isScreenOn
        registerWidgetReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        super.onStartCommand(intent, flags, startId)
        LogExt().d(javaClass.simpleName, ".onStartCommand(): "+intent?.action)

        when(intent?.action)
        {
            WIDGET_ALL_ENABLED ->
            {
                registerWidgetReceiver()
            }

            WIDGET_ONE_UPDATE ->
            {
                if (mAdvWidgetReceiver != null && isScreenOn) // HAS widget + ScreenON + PowerON = Update)
                {
                    isPowerConnected = intent.getBooleanExtra("isCharging", isPowerConnected)

                    if (isPowerConnected) // power connected
                    {
                        LogExt().d(javaClass.simpleName, ".onStartCommand(): Screen+Power=True, send MSG with 2000 ms delay ")
                        mWidgetHandler.sendMessageDelayed(Message.obtain(mWidgetHandler, Runnable {
                            mWidgetHandler.removeCallbacksAndMessages(null)
                            LogExt().d(javaClass.simpleName, "MainLooperRunnable(): Clear all MSG, send WIDGET_ONE_UPDATE")
                            sendBroadcast(Intent(this, BatteryInfoWidget::class.java).setAction(WIDGET_ONE_UPDATE).putExtras(intent))
                        }), 2500)
                    }
                    else // NO connected
                    {
                        LogExt().d(javaClass.simpleName, ".onStartCommand(): Power=False, send WIDGET_ONE_UPDATE")
                        sendBroadcast(Intent(this, BatteryInfoWidget::class.java).setAction(WIDGET_ONE_UPDATE).putExtras(intent))
                    }
                }
            }

            WIDGET_ALL_DISABLED ->
            {
                stopSelf()
            }
        }

        return START_STICKY;
    }

    override fun onDestroy()
    {
        super.onDestroy()

        LogExt().d(javaClass.simpleName, ".onDestroy()")
        unregisterWidgetReceiver()
        mWidgetHandler.removeCallbacksAndMessages(null)
    }

    //-----------------------------------------------------------------

    private fun registerWidgetReceiver()
    {
        if (mAdvWidgetReceiver == null && BatteryInfoWidget().getAppWidgetIds(this).isNotEmpty())
        {
            LogExt().d(javaClass.simpleName, ".registerWidgetReceiver()")
            mAdvWidgetReceiver = AdvWidgetReceiver()
            val widgetFilter = IntentFilter()
            widgetFilter.addAction(Intent.ACTION_BOOT_COMPLETED)
            widgetFilter.addAction(Intent.ACTION_POWER_CONNECTED)
            widgetFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
            widgetFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
            widgetFilter.addAction(Intent.ACTION_SCREEN_ON)
            widgetFilter.addAction(Intent.ACTION_SCREEN_OFF)
            registerReceiver(mAdvWidgetReceiver, widgetFilter)
            mAdvWidgetReceiver?.setEventInterface(this)
        }
    }

    private fun unregisterWidgetReceiver()
    {
        LogExt().d(javaClass.simpleName, ".unregisterWidgetReceiver()")
        if (mAdvWidgetReceiver != null) unregisterReceiver(mAdvWidgetReceiver)
        mAdvWidgetReceiver?.setEventInterface(null)
        mAdvWidgetReceiver = null
    }

    //--------------------------------------------------------------------

    override fun onScreen(screenOn: Boolean)
    {
        isScreenOn = screenOn
        if (isScreenOn && isPowerConnected) sendBroadcast(Intent(this, BatteryInfoWidget::class.java).setAction(WIDGET_ALL_UPDATE))
    }

    override fun onPowerState(connectOn: Boolean)
    {
        isPowerConnected = connectOn
        if (isPowerConnected && isScreenOn) sendBroadcast(Intent(this, BatteryInfoWidget::class.java).setAction(WIDGET_ALL_UPDATE))
    }

    //--------------------------------------------------------------------

    fun runSelfIntent(context: Context, intent: Intent)
    {
        try
        {
            context.startService(intent.setClass(context, WidgetService::class.java))
        }
        catch (ignored: Exception)
        {
            try
            {
                LogExt().w(javaClass.simpleName, "Error startService() .. test startForegroundService()")
                ContextCompat.startForegroundService(context, intent.setClass(context, WidgetService::class.java))
            }
            catch (ignored: Exception)
            {
                LogExt().e(javaClass.simpleName, "Error startForegroundService() .. goodbye!")
            }
        }

//        try
//        {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                context.startForegroundService(intent.setClass(context, AccaService::class.java))
//            else context.startService(intent.setClass(context, AccaService::class.java))
//        }
//        catch (ignored: Exception)
//        {
//            slog("AccaService", "Error runSelfIntent(): "+intent.action)
//        }
    }

    //------------------------------------------------------------------------------------
}