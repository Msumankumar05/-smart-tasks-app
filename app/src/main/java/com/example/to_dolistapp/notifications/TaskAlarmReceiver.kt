package com.smarttasks.official.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.smarttasks.official.MainActivity
import com.smarttasks.official.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("EXTRA_TASK_ID") ?: return
        val taskName = intent.getStringExtra("EXTRA_TASK_NAME") ?: "Task"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Time for your task!"
        val notificationId = intent.getIntExtra("EXTRA_NOTIFICATION_ID", taskId.hashCode())

        Log.d("TaskAlarmReceiver", "Notification fired - Task: $taskName, Message: $message, Time: ${Calendar.getInstance().time}")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the NotificationChannel for Android 8.0 and above
        val channelId = "task_reminders_channel"
        val channelName = "Task Reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Task Alarms"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Use a generic Android alarm icon, safe across all OS versions. 
        val icon = android.R.drawable.ic_popup_reminder

        // Create intent to open app when notification is tapped
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("task_id", taskId)
        }
        
        val contentIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get current time for accurate notification display
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Calendar.getInstance().time)
        val subtitleText = "$message | Triggered: $currentTime"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(taskName)
            .setContentText(subtitleText)
            .setSubText("Tap to open task")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        notificationManager.notify(notificationId, builder.build())
    }
}



