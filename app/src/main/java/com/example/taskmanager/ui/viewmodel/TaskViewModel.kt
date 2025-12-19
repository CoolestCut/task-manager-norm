package com.example.taskmanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.data.AppDatabase
import com.example.taskmanager.data.TaskRepository
import com.example.taskmanager.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val _currentFilter = MutableStateFlow("all")
    
    val filteredTasks: StateFlow<List<Task>> = _currentFilter
        .flatMapLatest { filter ->
            if (filter == "all") {
                repository.allTasks
            } else {
                repository.getTasksByStatus(filter)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        val taskDao = AppDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)

        viewModelScope.launch {
            // Если база данных пуста, создаем тестовые задачи
            val allTasks = repository.allTasks.first()
            if (allTasks.isEmpty()) {
                createTestTasks()
            }
        }
    }

    fun filterTasks(status: String) {
        _currentFilter.value = status
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(task)
        }
    }

    private suspend fun createTestTasks() {
        val calendar = Calendar.getInstance()
        val tasks = listOf(
            Task(
                strTitle = "Купить продукты",
                strDescription = "Молоко, хлеб, яйца, фрукты",
                iPriority = 2,
                dtDueDate = getDate(calendar, 2, 15, 0),
                strStatus = "todo"
            ),
            Task(
                strTitle = "Сделать презентацию",
                strDescription = "Подготовить слайды для совещания",
                iPriority = 3,
                dtDueDate = getDate(calendar, 0, 18, 30),
                strStatus = "in_progress"
            ),
            Task(
                strTitle = "Отправить отчет",
                strDescription = "Ежемесячный отчет по проекту",
                iPriority = 3,
                dtDueDate = getDate(calendar, -1, 10, 0),
                strStatus = "todo"
            ),
            Task(
                strTitle = "Позвонить маме",
                strDescription = "Обсудить планы на выходные",
                iPriority = 1,
                dtDueDate = getDate(calendar, 7, 20, 0),
                strStatus = "done"
            ),
            Task(
                strTitle = "Записаться к врачу",
                strDescription = "Плановый осмотр",
                iPriority = 2,
                dtDueDate = null,
                strStatus = "todo"
            )
        )
        tasks.forEach { repository.insertTask(it) }
    }

    private fun getDate(calendar: Calendar, daysOffset: Int, hour: Int, minute: Int): Date {
        val newCalendar = calendar.clone() as Calendar
        newCalendar.add(Calendar.DAY_OF_MONTH, daysOffset)
        newCalendar.set(Calendar.HOUR_OF_DAY, hour)
        newCalendar.set(Calendar.MINUTE, minute)
        newCalendar.set(Calendar.SECOND, 0)
        return newCalendar.time
    }
}