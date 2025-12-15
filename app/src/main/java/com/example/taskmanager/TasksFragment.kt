package com.example.taskmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        taskAdapter = TaskAdapter(emptyList()) { task ->
            Toast.makeText(requireContext(), "Клик по задаче: ${task.strTitle}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        val swipeToDeleteCallback = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.getTaskAt(position)
                viewModel.deleteTask(task)

                Snackbar.make(binding.root, "Задача удалена", Snackbar.LENGTH_LONG)
                    .setAction("Отмена") { viewModel.addTask(task) }
                    .show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun observeViewModel() {
        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.updateTasks(it)
        }
    }

    private fun setupFilters() {
        binding.toggleGroup.check(R.id.btnAll)
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnAll -> viewModel.filterTasks("all")
                    R.id.btnTodo -> viewModel.filterTasks("todo")
                    R.id.btnInProgress -> viewModel.filterTasks("in_progress")
                    R.id.btnDone -> viewModel.filterTasks("done")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}