package com.example.taskmanager

import android.app.Application
import com.example.taskmanager.data.AppDatabase
import com.example.taskmanager.data.TaskRepository

class TaskApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TaskRepository(database.taskDao()) }
}