package com.example.taskmanager.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.R
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    var arrTasks: List<Task> = emptyList(),
    private val onTaskClick: (Task) -> Unit,
    private val onStatusClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = arrTasks[position]

        holder.tvTitle.text = task.strTitle
        holder.tvDescription.text = task.strDescription

        val dateFormat = SimpleDateFormat("dd MMMM - HH:mm", Locale.getDefault())
        val dateText = task.dtDueDate?.let { dateFormat.format(it) } ?: "Без срока"
        holder.tvDateTime.text = dateText

        updateChipStatus(holder.chipStatus, task)

        holder.chipStatus.setOnClickListener {
            showStatusSelectionDialog(holder.itemView.context, task)
        }

        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }

    private fun updateChipStatus(chip: Chip, task: Task) {
        chip.text = task.strGetStatusText()
        chip.setChipBackgroundColorResource(android.R.color.transparent)

        val statusColor = task.iGetStatusColor()
        chip.chipStrokeColor = android.content.res.ColorStateList.valueOf(statusColor)
        chip.setTextColor(statusColor)
        chip.isClickable = true
        chip.isFocusable = true
    }

    private fun showStatusSelectionDialog(context: android.content.Context, task: Task) {
        val statusOptions = arrayOf(
            Task.STATUS_TODO,
            Task.STATUS_IN_PROGRESS,
            Task.STATUS_DONE
        )

        val statusNames = statusOptions.map { Task.getStatusText(it) }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Выберите статус")
            .setItems(statusNames) { _, which ->
                val selectedStatus = statusOptions[which]
                onStatusClick(task)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun getItemCount(): Int = arrTasks.size

    fun updateTasks(newTasks: List<Task>) {
        arrTasks = newTasks
        notifyDataSetChanged()
    }

    fun getTaskAt(position: Int): Task {
        return arrTasks[position]
    }
}