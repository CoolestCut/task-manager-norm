package com.example.taskmanager.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taskmanager.data.model.Task
import java.util.Calendar
import java.util.Date

class TaskViewModel : ViewModel() {

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val allTasks = mutableListOf<Task>()

    init {
        createTestTasks()
        _tasks.value = allTasks
    }

    private fun createTestTasks() {
        val calendar = Calendar.getInstance()
        allTasks.addAll(listOf(
            Task(iId = 1, strTitle = "Купить продукты", strDescription = "Молоко, хлеб, яйца, фрукты", iPriority = 2, dtDueDate = getDate(calendar, 2, 15, 0), strStatus = "todo"),
            Task(iId = 2, strTitle = "Сделать презентацию", strDescription = "Подготовить слайды для совещания", iPriority = 3, dtDueDate = getDate(calendar, 0, 18, 30), strStatus = "in_progress"),
            Task(iId = 3, strTitle = "Отправить отчет", strDescription = "Ежемесячный отчет по проекту", iPriority = 3, dtDueDate = getDate(calendar, -1, 10, 0), strStatus = "todo"),
            Task(iId = 4, strTitle = "Позвонить маме", strDescription = "Обсудить планы на выходные", iPriority = 1, dtDueDate = getDate(calendar, 7, 20, 0), strStatus = "done"),
            Task(iId = 5, strTitle = "Записаться к врачу", strDescription = "Плановый осмотр", iPriority = 2, dtDueDate = null, strStatus = "todo")
        ))
    }

    fun filterTasks(status: String) {
        if (status == "all") {
            _tasks.value = allTasks.toList()
        } else {
            _tasks.value = allTasks.filter { it.strStatus == status }
        }
    }

    // Для добавления из CreateTaskActivity
    fun addNewTask(title: String, description: String, priority: Int, dueDate: Date?, status: String) {
        val newTask = Task(
            iId = allTasks.size + 1,
            strTitle = title,
            strDescription = description,
            iPriority = priority,
            dtDueDate = dueDate,
            strStatus = status
        )
        allTasks.add(0, newTask)
        _tasks.value = allTasks.toList()
    }

    // Для добавления из FAB
    fun addNewTask() {
        val newTask = Task(
            iId = allTasks.size + 1,
            strTitle = "Новая задача ${allTasks.size + 1}",
            strDescription = "Описание новой задачи",
            iPriority = 2,
            strStatus = "todo"
        )
        allTasks.add(0, newTask)
        _tasks.value = allTasks.toList() // Обновляем LiveData
    }

    fun deleteTask(task: Task) {
        allTasks.remove(task)
        _tasks.value = allTasks.toList()
    }

    fun addTask(task: Task) {
        allTasks.add(task)
        _tasks.value = allTasks.toList()
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