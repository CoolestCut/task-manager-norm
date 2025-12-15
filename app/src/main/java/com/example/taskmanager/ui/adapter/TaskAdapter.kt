package com.example.taskmanager.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.R
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private var arrTasks: List<Task> = emptyList(),
    private val onTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder - хранит ссылки на элементы макета
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        // Создаем новый ViewHolder, используя макет item_task.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = arrTasks[position]

        // Заполняем элементы данными задачи
        holder.tvTitle.text = task.strTitle
        holder.tvDescription.text = task.strDescription

        // Форматируем дату
        val dateFormat = SimpleDateFormat("dd MMMM - HH:mm", Locale.getDefault())
        val dateText = task.dtDueDate?.let { dateFormat.format(it) } ?: "Без срока"
        holder.tvDateTime.text = dateText

        // Настраиваем чип статуса
        holder.chipStatus.text = task.strGetStatusText()
        holder.chipStatus.setChipBackgroundColorResource(android.R.color.transparent)
        holder.chipStatus.chipStrokeColor = android.content.res.ColorStateList.valueOf(task.iGetStatusColor())
        holder.chipStatus.setTextColor(task.iGetStatusColor())

        // Обработчик клика по карточке
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }

    override fun getItemCount(): Int = arrTasks.size

    // Функция для обновления списка задач
    fun updateTasks(newTasks: List<Task>) {
        arrTasks = newTasks
        notifyDataSetChanged() // Сообщаем RecyclerView, что данные изменились
    }
}