package mattecarra.accapp.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED
import android.content.Context
import android.content.Intent
import xml.BatteryInfoWidget
import xml.WIDGET_ALL_UPDATE
import xml.WIDGET_ID_NAME
import xml.WIDGET_ONE_UPDATE
import java.util.*

open class AppWidgetAlarm(context: Context)
{
    private val ALARM_ID = 7
    private val LONG_INTERVAL_MILLIS: Long = 3600000L  // 1house (60*60*1000)
    private val FAST_INTERVAL_MILLIS: Long = 2000      // 2sec
    private val mContext: Context = context

    fun startLongUpdateAlarm()
    {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MILLISECOND, LONG_INTERVAL_MILLIS.toInt())
        //
        val updateAllIntent = Intent(mContext, BatteryInfoWidget::class.java).setAction(WIDGET_ALL_UPDATE)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, updateAllIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        // RTC does not wake the device up
        val alarmManager: AlarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC, calendar.timeInMillis, LONG_INTERVAL_MILLIS, pendingIntent)
    }

    fun startFastUpdateAlarm(widgetId: Int)
    {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MILLISECOND, FAST_INTERVAL_MILLIS.toInt())
        //
        val updateOneIntent = Intent(mContext, BatteryInfoWidget::class.java).setAction(WIDGET_ONE_UPDATE).putExtra(WIDGET_ID_NAME, widgetId)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(mContext, widgetId, updateOneIntent, PendingIntent.FLAG_ONE_SHOT)
        //
        val alarmManager: AlarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC, calendar.timeInMillis, FAST_INTERVAL_MILLIS, pendingIntent)
    }

    fun stopLongUpdateAlarm()
    {
        val alarmIntent = Intent(WIDGET_ALL_UPDATE)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        (mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent)
    }

    fun stopFastUpdateAlarm(widgetId: Int)
    {
        val updateOneIntent = Intent(WIDGET_ONE_UPDATE).putExtra(WIDGET_ID_NAME, widgetId)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(mContext, widgetId, updateOneIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        (mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent)
    }

}