package com.example.taskmanager

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.databinding.ActivityMainBinding
import com.example.taskmanager.ui.adapter.TaskAdapter
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private val allTasks = mutableListOf<Task>() // Все задачи
    private var filteredTasks = mutableListOf<Task>() // Отфильтрованные задачи

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Используем View Binding для доступа к элементам интерфейса
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настраиваем Toolbar
        setSupportActionBar(binding.toolbar)

        // Инициализируем адаптер
        taskAdapter = TaskAdapter(emptyList()) { task ->
            // Обработчик клика по задаче
            Toast.makeText(this, "Клик по задаче: ${task.strTitle}", Toast.LENGTH_SHORT).show()
        }

        // Настраиваем RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }

        // Создаем тестовые задачи
        createTestTasks()

        // Настраиваем фильтры
        setupFilters()

        // Настраиваем FAB (кнопку добавления)
        binding.fabAddTask.setOnClickListener {
            // Простое добавление новой задачи
            addNewTask()
        }

        // Настраиваем клик по иконке меню
        binding.toolbar.setNavigationOnClickListener {
            Toast.makeText(this, "Открыть боковое меню", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTestTasks() {
        val calendar = Calendar.getInstance()

        // Добавляем несколько тестовых задач
        allTasks.addAll(listOf(
            Task(
                iId = 1,
                strTitle = "Купить продукты",
                strDescription = "Молоко, хлеб, яйца, фрукты",
                iPriority = 2,
                dtDueDate = getDate(calendar, 2, 15, 0), // Завтра 15:00
                strStatus = "todo"
            ),
            Task(
                iId = 2,
                strTitle = "Сделать презентацию",
                strDescription = "Подготовить слайды для совещания",
                iPriority = 3,
                dtDueDate = getDate(calendar, 0, 18, 30), // Сегодня 18:30
                strStatus = "in_progress"
            ),
            Task(
                iId = 3,
                strTitle = "Отправить отчет",
                strDescription = "Ежемесячный отчет по проекту",
                iPriority = 3,
                dtDueDate = getDate(calendar, -1, 10, 0), // Вчера 10:00 (просрочено)
                strStatus = "todo"
            ),
            Task(
                iId = 4,
                strTitle = "Позвонить маме",
                strDescription = "Обсудить планы на выходные",
                iPriority = 1,
                dtDueDate = getDate(calendar, 7, 20, 0), // Через неделю 20:00
                strStatus = "done"
            ),
            Task(
                iId = 5,
                strTitle = "Записаться к врачу",
                strDescription = "Плановый осмотр",
                iPriority = 2,
                dtDueDate = null, // Без срока
                strStatus = "todo"
            )
        ))

        // Показываем все задачи по умолчанию
        filteredTasks.addAll(allTasks)
        taskAdapter.updateTasks(filteredTasks)
    }

    private fun setupFilters() {
        // По умолчанию выбрана кнопка "Все"
        binding.toggleGroup.check(R.id.btnAll)

        // Обработчик изменения выбранного фильтра
        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnAll -> {
                        // Показать все задачи
                        filteredTasks.clear()
                        filteredTasks.addAll(allTasks)
                    }
                    R.id.btnTodo -> {
                        filteredTasks.clear()
                        filteredTasks.addAll(allTasks.filter { it.strStatus == "todo" })
                    }
                    R.id.btnInProgress -> {
                        filteredTasks.clear()
                        filteredTasks.addAll(allTasks.filter { it.strStatus == "in_progress" })
                    }
                    R.id.btnDone -> {
                        filteredTasks.clear()
                        filteredTasks.addAll(allTasks.filter { it.strStatus == "done" })
                    }
                }
                taskAdapter.updateTasks(filteredTasks)
            }
        }
    }

    // Вспомогательная функция для создания даты
    private fun getDate(calendar: Calendar, daysOffset: Int, hour: Int, minute: Int): Date {
        val newCalendar = calendar.clone() as Calendar
        newCalendar.add(Calendar.DAY_OF_MONTH, daysOffset)
        newCalendar.set(Calendar.HOUR_OF_DAY, hour)
        newCalendar.set(Calendar.MINUTE, minute)
        newCalendar.set(Calendar.SECOND, 0)
        return newCalendar.time
    }

    // Функция для добавления новой задачи (вместо showSimpleTaskDialog)
    private fun addNewTask() {
        val task = Task(
            strTitle = "Новая задача ${allTasks.size + 1}",
            strDescription = "Описание новой задачи",
            iPriority = 2,
            strStatus = "todo"
        )

        allTasks.add(0, task) // Добавляем в начало списка
        filteredTasks.clear()
        filteredTasks.addAll(allTasks)
        taskAdapter.updateTasks(filteredTasks)

        Toast.makeText(this, "Задача добавлена!", Toast.LENGTH_SHORT).show()
    }
}