package mattecarra.accapp.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import xml.BatteryInfoWidget
import xml.WIDGET_AUTO_UPDATE
import java.util.*

open class AppWidgetAlarm(context: Context)
{
    private val ALARM_ID = 7
    private val INTERVAL_MILLIS: Long = 60*1000
    private val mContext: Context = context

    fun startAlarm()
    {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MILLISECOND, INTERVAL_MILLIS.toInt())

        val alarmIntent = Intent(mContext, BatteryInfoWidget::class.java).setAction(WIDGET_AUTO_UPDATE)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        // RTC does not wake the device up
        val alarmManager: AlarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), INTERVAL_MILLIS, pendingIntent)

    }

    fun stopAlarm()
    {
        val alarmIntent = Intent(WIDGET_AUTO_UPDATE)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        (mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pendingIntent)
    }

}