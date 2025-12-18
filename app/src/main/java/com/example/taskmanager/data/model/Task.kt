package com.example.taskmanager.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.taskmanager.data.converter.DateConverter
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "tasks")
@TypeConverters(DateConverter::class)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val iId: Int = 0,
    val strTitle: String,
    val strDescription: String,
    val iPriority: Int = 2,
    val dtDueDate: Date? = null,
    val bIsCompleted: Boolean = false,
    val dtCreatedAt: Date = Date(),
    val strStatus: String = STATUS_TODO,
    val imageUrl: String? = null
) : Parcelable {
    companion object {
        const val STATUS_TODO = "todo"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_DONE = "done"

        val STATUS_LIST = listOf(STATUS_TODO, STATUS_IN_PROGRESS, STATUS_DONE)

        fun getStatusText(status: String): String {
            return when (status) {
                STATUS_TODO -> "Сделать"
                STATUS_IN_PROGRESS -> "В процессе"
                STATUS_DONE -> "Завершено"
                else -> "Неизвестно"
            }
        }
    }

    fun strGetStatusText(): String {
        return getStatusText(strStatus)
    }

    fun iGetStatusColor(): Int {
        return when (strStatus) {
            STATUS_TODO -> 0xFFFF9800.toInt()
            STATUS_IN_PROGRESS -> 0xFF2196F3.toInt()
            STATUS_DONE -> 0xFF4CAF50.toInt()
            else -> 0xFF9E9E9E.toInt()
        }
    }

    fun getNextStatus(): String {
        return when (strStatus) {
            STATUS_TODO -> STATUS_IN_PROGRESS
            STATUS_IN_PROGRESS -> STATUS_DONE
            STATUS_DONE -> STATUS_TODO
            else -> STATUS_TODO
        }
    }
}