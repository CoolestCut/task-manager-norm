package com.example.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.taskmanager.data.converter.DateConverter
import java.util.Date

@Entity(tableName = "tbl_tasks")
@TypeConverters(DateConverter::class)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val iId: Int = 0,
    val strTitle: String,
    val strDescription: String,
    val iPriority: Int = 2, // 1-низкий, 2-средний, 3-высокий
    val dtDueDate: Date? = null,
    val bIsCompleted: Boolean = false,
    val dtCreatedAt: Date = Date(),
    val strStatus: String = "todo" // "todo", "in_progress", "done"
) {
    // Функция для получения цвета статуса
    fun iGetStatusColor(): Int {
        return when (strStatus) {
            "todo" -> 0xFFFF9800.toInt()
            "in_progress" -> 0xFF2196F3.toInt()
            "done" -> 0xFF4CAF50.toInt()
            else -> 0xFF9E9E9E.toInt()
        }
    }

    // Функция для получения текста статуса на русском
    fun strGetStatusText(): String {
        return when (strStatus) {
            "todo" -> "Сделать"
            "in_progress" -> "В процессе"
            "done" -> "Завершено"
            else -> "Неизвестно"
        }
    }
}