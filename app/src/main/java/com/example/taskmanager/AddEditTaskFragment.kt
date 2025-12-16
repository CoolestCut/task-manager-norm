package com.example.taskmanager

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.databinding.FragmentAddEditTaskBinding
import com.example.taskmanager.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()
    private var taskToEdit: Task? = null
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskToEdit = it.getParcelable("task")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskToEdit?.let { task ->
            binding.etTitle.setText(task.strTitle)
            binding.etDescription.setText(task.strDescription)
            task.dtDueDate?.let { calendar.time = it }
            updateDateTimeLabel()
        }

        binding.tvDueDate.setOnClickListener {
            showDateTimePicker()
        }

        binding.btnSave.setOnClickListener {
            saveTask()
        }
    }

    private fun showDateTimePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateDateTimeLabel()
            }

            TimePickerDialog(
                requireContext(),
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateTimeLabel() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale.getDefault())
        binding.tvDueDate.text = dateFormat.format(calendar.time)
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()

        if (title.isBlank()) {
            binding.tilTitle.error = "Название не может быть пустым"
            return
        }

        val task = taskToEdit?.copy(
            strTitle = title,
            strDescription = description,
            dtDueDate = calendar.time
        ) ?: Task(
            strTitle = title,
            strDescription = description,
            dtDueDate = calendar.time
        )

        if (taskToEdit == null) {
            viewModel.addTask(task)
        } else {
            viewModel.updateTask(task)
        }

        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: Task? = null): AddEditTaskFragment {
            val fragment = AddEditTaskFragment()
            task?.let {
                val args = Bundle()
                args.putParcelable("task", it)
                fragment.arguments = args
            }
            return fragment
        }
    }
}