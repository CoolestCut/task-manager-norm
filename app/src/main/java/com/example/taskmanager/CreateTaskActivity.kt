package com.example.taskmanager

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.databinding.ActivityCreateTaskBinding
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.Calendar
import java.util.Date

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private var selectedDate: Calendar? = null
    private var selectedStatus: String = "todo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка статуса
        setupStatusToggle()

        // Выбор даты и времени
        binding.btnPickDate.setOnClickListener {
            showDateTimePicker()
        }

        // Настройка слайдера приоритета
        binding.sliderPriority.addOnChangeListener { _, value, _ ->
            val priorityText = when (value) {
                1.0f -> "Низкий"
                2.0f -> "Средний"
                3.0f -> "Высокий"
                else -> "Средний"
            }
            binding.sliderPriority.value = value
        }

        // Кнопка отмены
        binding.btnCancel.setOnClickListener {
            finish()
        }

        // Кнопка сохранения
        binding.btnSave.setOnClickListener {
            saveTask()
        }
    }

    private fun setupStatusToggle() {
        binding.toggleStatus.check(R.id.btnStatusTodo)

        binding.toggleStatus.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedStatus = when (checkedId) {
                    R.id.btnStatusTodo -> "todo"
                    R.id.btnStatusInProgress -> "in_progress"
                    R.id.btnStatusDone -> "done"
                    else -> "todo"
                }
            }
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        // Сначала выбираем дату
        DatePickerDialog(
            this,
            { _, year, month, day ->
                // После выбора даты выбираем время
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        selectedDate = Calendar.getInstance().apply {
                            set(year, month, day, hour, minute)
                        }

                        // Форматируем дату для отображения
                        val dateText = "${day}.${month + 1}.$year $hour:$minute"
                        binding.btnPickDate.text = dateText
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val priority = binding.sliderPriority.value.toInt()

        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(
            strTitle = title,
            strDescription = description,
            iPriority = priority,
            dtDueDate = selectedDate?.time,
            strStatus = selectedStatus
        )

        // Здесь будет сохранение в базу данных
        // Пока просто возвращаемся в MainActivity
        Toast.makeText(this, "Задача сохранена", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}