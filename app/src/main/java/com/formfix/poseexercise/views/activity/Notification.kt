package com.formfix.poseexercise

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import java.util.concurrent.TimeUnit

class Notification(private val context: Context) {
    private val PREFS_NAME = "UserActivityPrefs"
    private val LAST_ACTIVE_KEY = "last_active_timestamp"
    private val CHANNEL_ID = "inactivity_channel"
    private val NOTIFICATION_ID = 1001

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
        scheduleInactivityCheck()
    }

    fun updateLastActiveTimestamp() {
        prefs.edit().putLong(LAST_ACTIVE_KEY, System.currentTimeMillis()).apply()
    }

    fun checkInactivity() {
        val lastActiveTime = prefs.getLong(LAST_ACTIVE_KEY, 0)
        val currentTime = System.currentTimeMillis()
        val diffInMillis = currentTime - lastActiveTime


        if (diffInMillis >= TimeUnit.DAYS.toMillis(3)) {
            showInactivityNotification()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Inactivity Notifications"
            val descriptionText = "Notifications for user inactivity"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showInactivityNotification() {
        val intent = Intent(context, Home::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Don’t let your progress slip")
            .setContentText("Every step counts. Start small today and keep moving forward!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // Set as high priority for Doze mode
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun scheduleInactivityCheck() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, InactiveReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1), // Check every day
                    pendingIntent
                )
            } else {

                val permissionIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(permissionIntent)
            }
        } else {

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
                pendingIntent
            )
        }
    }

}