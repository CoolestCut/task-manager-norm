package com.example.taskmanager

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.databinding.FragmentCalendarBinding
import com.example.taskmanager.ui.adapter.TaskAdapter
import com.example.taskmanager.ui.viewmodel.TaskViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.Calendar
import java.util.Date

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var calendarTaskAdapter: TaskAdapter
    private var selectedDate: CalendarDay = CalendarDay.today()

    // --- DECORATOR REFS ---
    private lateinit var outOfMonthDecorator: OutOfMonthDayDecorator
    private lateinit var todoDecorator: EventDecorator
    private lateinit var inProgressDecorator: EventDecorator
    private lateinit var doneDecorator: EventDecorator
    // ---

    private val createTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                val title = it.getStringExtra(CreateTaskActivity.EXTRA_TITLE)!!
                val description = it.getStringExtra(CreateTaskActivity.EXTRA_DESCRIPTION)!!
                val priority = it.getIntExtra(CreateTaskActivity.EXTRA_PRIORITY, 2)
                val dueDateMillis = it.getLongExtra(CreateTaskActivity.EXTRA_DUE_DATE, -1L)
                val status = it.getStringExtra(CreateTaskActivity.EXTRA_STATUS)!!
                val dueDate = if (dueDateMillis != -1L) Date(dueDateMillis) else null
                viewModel.addNewTask(title, description, priority, dueDate, status)
            }
        }
    }

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
        calendarTaskAdapter = TaskAdapter(emptyList()) { task ->
            Toast.makeText(requireContext(), "Клик по задаче: ${task.strTitle}", Toast.LENGTH_SHORT).show()
        }
        binding.rvCalendarTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = calendarTaskAdapter
        }
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
        }

        binding.calendarView.setOnMonthChangedListener { widget, date ->
            outOfMonthDecorator.setMonth(date.month)
            widget.invalidateDecorators()
        }
    }

    private fun observeViewModel() {
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            updateCalendarDecorators(tasks)
            filterTasksForSelectedDate(selectedDate)
        }
    }

    private fun filterTasksForSelectedDate(date: CalendarDay) {
        val tasksForDay = viewModel.tasks.value?.filter { task ->
            task.dtDueDate?.let { dueDate ->
                val taskCalendar = Calendar.getInstance().apply { time = dueDate }
                val taskDay = CalendarDay.from(
                    taskCalendar.get(Calendar.YEAR),
                    taskCalendar.get(Calendar.MONTH) + 1,
                    taskCalendar.get(Calendar.DAY_OF_MONTH)
                )
                taskDay == date
            } ?: false
        } ?: emptyList()
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
                    "todo" -> todoDates.add(day)
                    "in_progress" -> inProgressDates.add(day)
                    "done" -> doneDates.add(day)
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
        view.addSpan(ForegroundColorSpan(Color.GRAY))
    }

    fun setMonth(month: Int) {
        this.month = month
    }
}
