package com.smarttasks.official.model

import java.util.Calendar
import java.util.Date

// Cache today's tart of day to avoid repeated Calendar.getInstance() calls
private var cachedToday: Long = 0L
private var cachedTodayStart: Long = 0L
private var cachedTodayEnd: Long = 0L

private fun getTodayBounds(): Triple<Long, Long, Long> {
    val now = System.currentTimeMillis()
    // Refresh cache only if we've moved to a new day (60 second tolerance)
    if (Math.abs(now - cachedToday) > 60000L) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cachedTodayStart = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        cachedTodayEnd = calendar.timeInMillis
        cachedToday = now
    }
    return Triple(cachedToday, cachedTodayStart, cachedTodayEnd)
}

// Optimized version using timestamp comparison instead of Calendar objects
fun isToday(date: Date): Boolean {
    val (_, todayStart, todayEnd) = getTodayBounds()
    val dateTime = date.time
    return dateTime >= todayStart && dateTime < todayEnd
}

fun isTomorrow(date: Date): Boolean {
    val (_, todayStart, todayEnd) = getTodayBounds()
    val tomorrowStart = todayEnd
    val tomorrowEnd = tomorrowStart + (24 * 60 * 60 * 1000L)
    val dateTime = date.time
    return dateTime >= tomorrowStart && dateTime < tomorrowEnd
}

// Keep for backward compatibility but optimized
fun isSameDay(date1: Date, date2: Date): Boolean {
    val c1 = Calendar.getInstance().apply { time = date1 }
    val c2 = Calendar.getInstance().apply { time = date2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

fun getCalendarDays(year: Int, month: Int): List<Int?> {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    return buildList {
        repeat(firstDayOfWeek) { add(null) }
        for (day in 1..daysInMonth) add(day)
    }
}



