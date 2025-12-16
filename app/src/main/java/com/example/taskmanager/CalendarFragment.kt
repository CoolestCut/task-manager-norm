package com.example.taskmanager

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.databinding.FragmentCalendarBinding
import com.example.taskmanager.ui.adapter.TaskAdapter
import com.example.taskmanager.ui.viewmodel.TaskViewModel
import com.google.android.material.snackbar.Snackbar
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var calendarTaskAdapter: TaskAdapter
    private var selectedDate: CalendarDay = CalendarDay.today()

    private lateinit var outOfMonthDecorator: OutOfMonthDayDecorator
    private lateinit var todoDecorator: EventDecorator
    private lateinit var inProgressDecorator: EventDecorator
    private lateinit var doneDecorator: EventDecorator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupCalendarView()
        observeViewModel()
        binding.calendarView.selectedDate = CalendarDay.today()
        filterTasksForSelectedDate(CalendarDay.today())
    }

    private fun setupRecyclerView() {
        calendarTaskAdapter = TaskAdapter(
            emptyList(),
            onTaskClick = { task ->
                showTaskActionsDialog(task)
            },
            onStatusClick = { task ->
                showStatusSelectionDialog(task)
            }
        )

        binding.rvCalendarTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = calendarTaskAdapter
        }

        setupSwipeForTasks()
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

    private fun setupSwipeForTasks() {
        val swipeCallback = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = calendarTaskAdapter.getTaskAt(position)

                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            showDeleteConfirmationDialog(task)
                        }
                        ItemTouchHelper.RIGHT -> {
                            changeTaskStatusToNext(task)
                        }
                    }

                    calendarTaskAdapter.notifyItemChanged(position)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvCalendarTasks)
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

    private fun setupCalendarView() {
        val emptyDates = HashSet<CalendarDay>()
        todoDecorator = EventDecorator(0xFFFF9800.toInt(), emptyDates)
        inProgressDecorator = EventDecorator(0xFF2196F3.toInt(), emptyDates)
        doneDecorator = EventDecorator(0xFF4CAF50.toInt(), emptyDates)

        val initialDate = binding.calendarView.currentDate
        outOfMonthDecorator = OutOfMonthDayDecorator(initialDate.month)

        binding.calendarView.addDecorators(
            CurrentDayDecorator(requireActivity()),
            outOfMonthDecorator,
            todoDecorator,
            inProgressDecorator,
            doneDecorator
        )

        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            selectedDate = date
            filterTasksForSelectedDate(date)
            (activity as? MainActivity)?.showBottomAppBarAndFab()
        }

        binding.calendarView.setOnMonthChangedListener { widget, date ->
            outOfMonthDecorator.setMonth(date.month)
            widget.invalidateDecorators()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredTasks.collect { tasks ->
                    updateCalendarDecorators(tasks)
                    filterTasksForSelectedDate(selectedDate)
                }
            }
        }
    }

    private fun filterTasksForSelectedDate(date: CalendarDay) {
        val tasks = viewModel.filteredTasks.value
        val tasksForDay = tasks.filter { task ->
            task.dtDueDate?.let { dueDate ->
                val taskCalendar = Calendar.getInstance().apply { time = dueDate }
                val taskDay = CalendarDay.from(
                    taskCalendar.get(Calendar.YEAR),
                    taskCalendar.get(Calendar.MONTH) + 1,
                    taskCalendar.get(Calendar.DAY_OF_MONTH)
                )
                taskDay == date
            } ?: false
        }

        calendarTaskAdapter.updateTasks(tasksForDay)
        binding.tvNoTasks.visibility = if (tasksForDay.isEmpty()) View.VISIBLE else View.GONE
        binding.rvCalendarTasks.visibility = if (tasksForDay.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateCalendarDecorators(tasks: List<Task>) {
        val todoDates = mutableSetOf<CalendarDay>()
        val inProgressDates = mutableSetOf<CalendarDay>()
        val doneDates = mutableSetOf<CalendarDay>()
        val calendar = Calendar.getInstance()

        tasks.forEach { task ->
            task.dtDueDate?.let { dueDate ->
                calendar.time = dueDate
                val day = CalendarDay.from(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                when (task.strStatus) {
                    Task.STATUS_TODO -> todoDates.add(day)
                    Task.STATUS_IN_PROGRESS -> inProgressDates.add(day)
                    Task.STATUS_DONE -> doneDates.add(day)
                }
            }
        }

        todoDecorator.setDates(todoDates)
        inProgressDecorator.setDates(inProgressDates)
        doneDecorator.setDates(doneDates)

        binding.calendarView.invalidateDecorators()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class EventDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {
    private var dates: HashSet<CalendarDay> = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(10f, color))
    }

    fun setDates(dates: Collection<CalendarDay>) {
        this.dates = HashSet(dates)
    }
}

class CurrentDayDecorator(context: Activity) : DayViewDecorator {
    private val today = CalendarDay.today()
    private val drawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.current_day_circle)

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day == today
    }

    override fun decorate(view: DayViewFacade) {
        drawable?.let { view.setBackgroundDrawable(it) }
    }
}

class OutOfMonthDayDecorator(private var month: Int) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.month != month
    }

    override fun decorate(view: DayViewFacade) {
        view.setDaysDisabled(true)
    }

    fun setMonth(month: Int) {
        this.month = month
    }
}