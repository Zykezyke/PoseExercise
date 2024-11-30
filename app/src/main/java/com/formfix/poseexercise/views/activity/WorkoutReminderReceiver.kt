package com.formfix.poseexercise

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WorkoutReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val workoutReminder = WorkoutReminder(context)

        when (intent?.action) {
            "MORNING_CHECK" -> workoutReminder.checkTodaysWorkout(false)
            "AFTERNOON_CHECK" -> workoutReminder.checkTodaysWorkout(true)
            Intent.ACTION_BOOT_COMPLETED -> workoutReminder.scheduleWorkoutCheck()
        }
    }
}