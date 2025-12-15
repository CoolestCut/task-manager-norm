package com.example.taskmanager.data.dao

import androidx.room.*
import com.example.taskmanager.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tbl_tasks ORDER BY dtCreatedAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tbl_tasks WHERE strStatus = :status ORDER BY dtCreatedAt DESC")
    fun getTasksByStatus(status: String): Flow<List<Task>>

    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tbl_tasks")
    suspend fun deleteAllTasks()
}