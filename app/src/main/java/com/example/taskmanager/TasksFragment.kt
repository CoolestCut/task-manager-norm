package com.example.taskmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.databinding.FragmentTasksBinding
import com.example.taskmanager.ui.adapter.TaskAdapter
import com.example.taskmanager.ui.viewmodel.TaskViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

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
                        editTask(task)
                    }
                }

                taskAdapter.notifyItemChanged(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun showTaskActionsDialog(task: Task) {
        val nextStatusText = Task.getStatusText(task.getNextStatus())
        val actions = arrayOf(
            "Изменить статус на: $nextStatusText",
            "Редактировать задачу",
            "Удалить задачу",
            "Отмена"
        )

        AlertDialog.Builder(requireContext())
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

    private fun changeTaskStatusToNext(task: Task) {
        val newStatus = task.getNextStatus()
        changeTaskStatus(task, newStatus)
    }

    private fun changeTaskStatus(task: Task, newStatus: String) {
        if (task.strStatus != newStatus) {
            val updatedTask = task.copy(strStatus = newStatus)
            viewModel.updateTask(updatedTask)

            val statusText = Task.getStatusText(newStatus)
            Snackbar.make(binding.root, "Статус изменен на: $statusText", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showStatusSelectionDialog(task: Task) {
        val statuses = Task.STATUS_LIST
        val statusNames = statuses.map { Task.getStatusText(it) }

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите статус")
            .setItems(statusNames.toTypedArray()) { _, which ->
                val newStatus = statuses[which]
                changeTaskStatus(task, newStatus)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun editTask(task: Task) {
        (activity as? MainActivity)?.loadFragment(AddEditTaskFragment.newInstance(task), true)
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление задачи")
            .setMessage("Удалить задачу \"${task.strTitle}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteTask(task)
                showUndoSnackbar(task)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showUndoSnackbar(deletedTask: Task) {
        Snackbar.make(binding.root, "Задача удалена", Snackbar.LENGTH_LONG)
            .setAction("Отмена") { viewModel.addTask(deletedTask) }
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredTasks.collect { tasks ->
                    taskAdapter.updateTasks(tasks)
                }
            }
        }
    }

    private fun setupFilters() {
        binding.toggleGroup.check(R.id.btnAll)
        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnAll -> viewModel.filterTasks("all")
                    R.id.btnTodo -> viewModel.filterTasks(Task.STATUS_TODO)
                    R.id.btnInProgress -> viewModel.filterTasks(Task.STATUS_IN_PROGRESS)
                    R.id.btnDone -> viewModel.filterTasks(Task.STATUS_DONE)
                }
            } else {
                if (group.checkedButtonId == View.NO_ID) {
                    group.check(R.id.btnAll)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}