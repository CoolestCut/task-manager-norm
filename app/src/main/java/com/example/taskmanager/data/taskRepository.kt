package com.example.taskmanager.data

import com.example.taskmanager.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksByStatus(status: String): Flow<List<Task>> = taskDao.getTasksByStatus(status)

    fun getUpcomingTasks(): Flow<List<Task>> = taskDao.getUpcomingTasks()

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteAllTasks() = taskDao.deleteAllTasks()
}