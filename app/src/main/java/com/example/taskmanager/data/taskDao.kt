package com.example.taskmanager.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.taskmanager.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dtCreatedAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE strStatus = :status ORDER BY dtCreatedAt DESC")
    fun getTasksByStatus(status: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE strStatus IN ('todo', 'in_progress') AND dtDueDate IS NOT NULL ORDER BY dtDueDate ASC")
    fun getUpcomingTasks(): Flow<List<Task>>

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}