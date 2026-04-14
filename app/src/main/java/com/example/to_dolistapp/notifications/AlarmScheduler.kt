package com.smarttasks.official.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smarttasks.official.model.Task
import com.smarttasks.official.utils.DateFormatUtils
import java.util.Calendar
import java.util.TimeZone

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        // Grace period for past alarms (1 minute buffer)
        private const val GRACE_PERIOD_MS = 1 * 60 * 1000L
    }

    fun scheduleAlarms(task: Task) {
        // Only schedule if the task has a valid time and isn't completed
        if (task.isCompleted || task.time.isBlank()) {
            cancelAlarms(task)
            return
        }

        try {
            val parsedTime = DateFormatUtils.TIME_FORMAT.parse(task.time) ?: return
            val timeCal = Calendar.getInstance().apply { time = parsedTime }

            // Get device timezone for accurate calculations
            val deviceTz = TimeZone.getDefault()
            
            val taskCalendar = Calendar.getInstance(deviceTz).apply {
                time = task.date
                set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val triggerTimeMs = taskCalendar.timeInMillis
            val currentTimeMs = System.currentTimeMillis()

            Log.d("AlarmScheduler", """
                Task: ${task.name}
                Scheduled time: ${taskCalendar.time}
                Current time: ${Calendar.getInstance().time}
                Timezone: ${deviceTz.id}
                IsDST: ${deviceTz.inDaylightTime(taskCalendar.time)}
            """.trimIndent())

            // Schedule exactly at time
            scheduleIfFuture(task, triggerTimeMs, 0, "Time for your task!", currentTimeMs)
            
            // Schedule at +5 mins
            val cal5 = taskCalendar.clone() as Calendar
            cal5.add(Calendar.MINUTE, 5)
            scheduleIfFuture(task, cal5.timeInMillis, 5, "5 minutes passed! Don't forget your task.", currentTimeMs)

            // Schedule at +10 mins
            val cal10 = taskCalendar.clone() as Calendar
            cal10.add(Calendar.MINUTE, 10)
            scheduleIfFuture(task, cal10.timeInMillis, 10, "10 minutes passed! Last reminder for your task.", currentTimeMs)

        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Failed to schedule alarms", e)
        }
    }

    private fun scheduleIfFuture(task: Task, triggerAtMillis: Long, offsetMins: Int, message: String, currentTimeMs: Long) {
        // Only schedule alarms that are in the future (even if just a few seconds away)
        // Removed grace period for more accurate scheduling
        if (currentTimeMs >= triggerAtMillis) {
            Log.w("AlarmScheduler", "Skipping past alarm: offset=$offsetMins, scheduled for ${triggerAtMillis - currentTimeMs}ms ago")
            return
        }

        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("EXTRA_TASK_ID", task.id)
            putExtra("EXTRA_TASK_NAME", task.name)
            putExtra("EXTRA_MESSAGE", message)
            val notificationId = task.id.hashCode() + offsetMins
            putExtra("EXTRA_NOTIFICATION_ID", notificationId)
        }

        val requestCode = task.id.hashCode() + offsetMins
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Scheduled alarm for task ${task.name} with offset $offsetMins at exact time $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Exact alarm permission missing, falling back to inexact", e)
            try {
                // Fallback for missing SCHEDULE_EXACT_ALARM permission
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (fallbackError: Exception) {
                Log.e("AlarmScheduler", "Failed to schedule alarm with fallback", fallbackError)
            }
        }
    }

    fun cancelAlarms(task: Task) {
        val offsets = listOf(0, 5, 10)
        for (offsetMins in offsets) {
            val intent = Intent(context, TaskAlarmReceiver::class.java)
            val requestCode = task.id.hashCode() + offsetMins
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
            
            // Also clear existing notification if any
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(requestCode)
        }
        Log.d("AlarmScheduler", "Canceled alarms for task ${task.name}")
    }
}



