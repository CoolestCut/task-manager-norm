package com.example.taskmanager

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanager.databinding.ActivityCreateTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private var selectedDate: Calendar = Calendar.getInstance()

    companion object {
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION"
        const val EXTRA_PRIORITY = "EXTRA_PRIORITY"
        const val EXTRA_DUE_DATE = "EXTRA_DUE_DATE"
        const val EXTRA_STATUS = "EXTRA_STATUS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedTime = intent.getLongExtra("SELECTED_DATE", -1)
        if (selectedTime != -1L) {
            selectedDate.timeInMillis = selectedTime
        }
        updateDateButtonText()

        setupStatusToggle()

        binding.btnPickDate.setOnClickListener {
            showDateTimePicker()
        }

        binding.sliderPriority.addOnChangeListener { _, value, _ ->
            binding.sliderPriority.value = value
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveTask()
        }
    }

    private fun setupStatusToggle() {
        binding.toggleStatus.check(R.id.btnStatusTodo)
    }

    private fun showDateTimePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        selectedDate.set(year, month, day, hour, minute)
                        updateDateButtonText()
                    },
                    selectedDate.get(Calendar.HOUR_OF_DAY),
                    selectedDate.get(Calendar.MINUTE),
                    true
                ).show()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateButtonText() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        binding.btnPickDate.text = dateFormat.format(selectedDate.time)
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val priority = binding.sliderPriority.value.toInt()
        val status = when (binding.toggleStatus.checkedButtonId) {
            R.id.btnStatusInProgress -> "in_progress"
            R.id.btnStatusDone -> "done"
            else -> "todo"
        }

        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent().apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESCRIPTION, description)
            putExtra(EXTRA_PRIORITY, priority)
            putExtra(EXTRA_DUE_DATE, selectedDate.timeInMillis)
            putExtra(EXTRA_STATUS, status)
        }

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}