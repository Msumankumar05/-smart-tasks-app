package com.smarttasks.official.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val DARK_MODE_KEY = "dark_mode"
        private const val SHOW_COMPLETED_KEY = "show_completed"
    }

    // ── Dark Mode ────────────────────────────────────────────────────────
    fun getDarkMode(): Boolean = sharedPreferences.getBoolean(DARK_MODE_KEY, false)

    fun setDarkMode(isDark: Boolean) {
        sharedPreferences.edit().putBoolean(DARK_MODE_KEY, isDark).apply()
    }

    // ── Show Completed Tasks ─────────────────────────────────────────────
    fun getShowCompleted(): Boolean = sharedPreferences.getBoolean(SHOW_COMPLETED_KEY, true)

    fun setShowCompleted(show: Boolean) {
        sharedPreferences.edit().putBoolean(SHOW_COMPLETED_KEY, show).apply()
    }
}
