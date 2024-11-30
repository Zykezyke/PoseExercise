package com.formfix.poseexercise.util

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

class MyApplication : Application() {
    // Using the companion object to create a singleton instance of MyApplication
    companion object {
        private lateinit var instance: MyApplication

        fun getInstance(): MyApplication {
            return instance
        }
    }

    private fun initializeTheme() {
        // Get saved theme preference or default to system theme
        val sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("isNightMode", isSystemInNightMode())

        // Apply the theme
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun isSystemInNightMode(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    // Override the onCreate method to initialize the instance variable
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}