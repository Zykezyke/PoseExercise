package com.formfix.poseexercise

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class InactiveReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val notification = Notification(context)
        notification.checkInactivity()
    }
}