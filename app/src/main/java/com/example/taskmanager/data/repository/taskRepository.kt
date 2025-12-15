package com.example.taskmanager.data.repository

import com.example.taskmanager.data.dao.TaskDao
import com.example.taskmanager.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksByStatus(status: String): Flow<List<Task>> =
        taskDao.getTasksByStatus(status)

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
}