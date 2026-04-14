package com.smarttasks.official.utils

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Thread-safe centralized date formatters to avoid repeated allocation.
 * SimpleDateFormat is NOT thread-safe, so these are used carefully within Compose's single-threaded context.
 */
object DateFormatUtils {
    // Cached formatters - recreated only once
    val TIME_FORMAT by lazy { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val DATE_FORMATTER by lazy { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val MONTH_YEAR_FORMATTER by lazy { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val DAY_MONTH_FORMATTER by lazy { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
    val SHORT_DATE_FORMATTER by lazy { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) }

    fun formatTime(text: String): String = try {
        TIME_FORMAT.parse(text)?.let { TIME_FORMAT.format(it) } ?: text
    } catch (e: Exception) {
        text
    }
}



