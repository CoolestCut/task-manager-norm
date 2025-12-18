package com.example.taskmanager

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.jakewharton.threetenabp.AndroidThreeTen
import java.io.PrintWriter
import java.io.StringWriter

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Устанавливаем глобальный обработчик ошибок
        Thread.setDefaultUncaughtExceptionHandler {
            thread, e -> handleUncaughtException(e)
        }

        AndroidThreeTen.init(this)

        // Применяем тему при запуске
        updateTheme()
    }

    private fun handleUncaughtException(e: Throwable) {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        val errorText = sw.toString()

        val intent = Intent(this, CrashDisplayActivity::class.java).apply {
            putExtra("error", errorText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)

        // Завершаем процесс
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(10)
    }

    private fun updateTheme() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = preferences.getString("theme_preference", "system")

        when (themePreference) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}