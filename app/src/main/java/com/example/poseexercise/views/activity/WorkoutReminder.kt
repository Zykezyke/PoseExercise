package com.example.poseexercise

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.Locale
import android.provider.Settings

class WorkoutReminder(private val context: Context) {
    private val CHANNEL_ID = "workout_reminder_channel"
    private val MORNING_NOTIFICATION_ID = 2001
    private val AFTERNOON_NOTIFICATION_ID = 2002
    private val PREFS_NAME = "WorkoutReminderPrefs"
    private val ALARM_SET_KEY = "alarm_set"

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Workout Reminders"
            val descriptionText = "Daily workout reminder notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleWorkoutCheck() {
        scheduleDailyAlarm(8, 0, "MORNING_CHECK")  // 8:00 AM
        scheduleDailyAlarm(15, 0, "AFTERNOON_CHECK")  // 3:00 PM
    }

    private fun scheduleDailyAlarm(hour: Int, minute: Int, action: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WorkoutReminderReceiver::class.java).apply {
            this.action = action
        }

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Check if the app can schedule exact alarms (for Android 12 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
            if (canScheduleExactAlarms) {
                // Schedule exact alarm if permission is granted
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                // Request permission from the user to allow exact alarms
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK  // Add this line
                }
                context.startActivity(intent)
            }
        } else {
            // For older versions, directly schedule the alarm
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        // Save that we've set the alarm
        prefs.edit().putBoolean(ALARM_SET_KEY, true).apply()
    }

    fun checkTodaysWorkout(isAfternoon: Boolean = false) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val dayOfWeek = Calendar.getInstance().getDisplayName(
            Calendar.DAY_OF_WEEK,
            Calendar.LONG,
            Locale.getDefault()
        ) ?: return

        val databaseReference = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("UserAccounts")
            .child(currentUser.uid)
            .child("${dayOfWeek}Planner")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val exerciseCount = if (snapshot.exists()) snapshot.childrenCount.toInt() else 0
                showWorkoutReminderNotification(exerciseCount, isAfternoon)
            }

            override fun onCancelled(error: DatabaseError) {
                showWorkoutReminderNotification(0, isAfternoon)
            }
        })
    }

    private fun showWorkoutReminderNotification(exerciseCount: Int, isAfternoon: Boolean) {
        val intent = Intent(context, WeeklyPlannerDisplay::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val title = if (isAfternoon) {
            "Afternoon Workout Check-in 💪"
        } else {
            "Morning Workout Reminder 💪"
        }

        val message = when {
            exerciseCount == 0 -> "No exercises planned for today. Want to add some?"
            exerciseCount == 1 -> "You have 1 exercise planned for today. Let's crush it!"
            else -> "You have $exerciseCount exercises planned for today. Time to get moving!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationId = if (isAfternoon) AFTERNOON_NOTIFICATION_ID else MORNING_NOTIFICATION_ID
        notificationManager.notify(notificationId, builder.build())
    }
}
