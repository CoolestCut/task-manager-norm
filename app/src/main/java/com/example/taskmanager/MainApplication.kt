package com.example.taskmanager

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class MainApplication : Application() {
    // БД инициализируется лениво, поэтому здесь ничего не нужно
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}