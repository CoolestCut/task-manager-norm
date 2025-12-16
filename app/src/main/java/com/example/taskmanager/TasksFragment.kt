package com.example.taskmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.FragmentTasksBinding
import com.example.taskmanager.ui.adapter.TaskAdapter
import com.example.taskmanager.ui.viewmodel.TaskViewModel
import com.google.android.material.snackbar.Snackbar

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter
    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilters()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            emptyList(),
            onTaskClick = { task ->
                showTaskActionsDialog(task)
            },
            onStatusClick = { task ->
                showStatusSelectionDialog(task)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        val swipeToDeleteCallback = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.getTaskAt(position)

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        showDeleteConfirmationDialog(task)
                    }
                    ItemTouchHelper.RIGHT -> {
                        changeTaskStatusToNext(task)
                    }
                }

                taskAdapter.notifyItemChanged(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun showTaskActionsDialog(task: com.example.taskmanager.data.model.Task) {
        val nextStatusText = com.example.taskmanager.data.model.Task.getStatusText(task.getNextStatus())
        val actions = arrayOf(
            "Изменить статус на: $nextStatusText",
            "Редактировать задачу",
            "Удалить задачу",
            "Отмена"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(task.strTitle)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> changeTaskStatusToNext(task)
                    1 -> editTask(task)
                    2 -> showDeleteConfirmationDialog(task)
                    // 3 - Отмена
                }
            }
            .show()
    }

    private fun changeTaskStatusToNext(task: com.example.taskmanager.data.model.Task) {
        val newStatus = task.getNextStatus()
        changeTaskStatus(task, newStatus)
    }

    private fun changeTaskStatus(task: com.example.taskmanager.data.model.Task, newStatus: String) {
        if (task.strStatus != newStatus) {
            val updatedTask = task.copy(strStatus = newStatus)
            viewModel.updateTask(updatedTask)

            val statusText = com.example.taskmanager.data.model.Task.getStatusText(newStatus)
            Snackbar.make(binding.root, "Статус изменен на: $statusText", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showStatusSelectionDialog(task: com.example.taskmanager.data.model.Task) {
        val statuses = com.example.taskmanager.data.model.Task.STATUS_LIST
        val statusNames = statuses.map { com.example.taskmanager.data.model.Task.getStatusText(it) }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Выберите статус")
            .setItems(statusNames.toTypedArray()) { _, which ->
                val newStatus = statuses[which]
                changeTaskStatus(task, newStatus)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun editTask(task: com.example.taskmanager.data.model.Task) {
        // TODO: Реализовать редактирование
        Snackbar.make(binding.root, "Редактирование: ${task.strTitle}", Snackbar.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog(task: com.example.taskmanager.data.model.Task) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удаление задачи")
            .setMessage("Удалить задачу \"${task.strTitle}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteTask(task)
                showUndoSnackbar(task)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showUndoSnackbar(deletedTask: com.example.taskmanager.data.model.Task) {
        Snackbar.make(binding.root, "Задача удалена", Snackbar.LENGTH_LONG)
            .setAction("Отмена") { viewModel.addTask(deletedTask) }
            .show()
    }

    private fun observeViewModel() {
        viewModel.filteredTasks.observe(viewLifecycleOwner) {
            taskAdapter.updateTasks(it)
        }
    }

    private fun setupFilters() {
        binding.toggleGroup.check(R.id.btnAll)
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnAll -> viewModel.filterTasks("all")
                    R.id.btnTodo -> viewModel.filterTasks(com.example.taskmanager.data.model.Task.STATUS_TODO)
                    R.id.btnInProgress -> viewModel.filterTasks(com.example.taskmanager.data.model.Task.STATUS_IN_PROGRESS)
                    R.id.btnDone -> viewModel.filterTasks(com.example.taskmanager.data.model.Task.STATUS_DONE)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}