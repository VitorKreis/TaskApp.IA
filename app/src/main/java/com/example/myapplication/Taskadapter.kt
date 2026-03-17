package com.example.myapplication

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val tasks: List<Task>,
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit,
    private val onToggle: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dot        : View        = view.findViewById(R.id.view_priority_bar)
        val tvTitle    : TextView    = view.findViewById(R.id.tv_task_title)
        val tvDate     : TextView    = view.findViewById(R.id.tv_due_date)
        val btnComplete: ImageButton = view.findViewById(R.id.btn_complete)
        val btnDelete  : ImageButton = view.findViewById(R.id.btn_delete)

        fun bind(task: Task) {
            // Título
            tvTitle.text = task.title
            tvTitle.alpha = if (task.isDone) 0.45f else 1f
            tvTitle.paintFlags = if (task.isDone)
                tvTitle.paintFlags or  Paint.STRIKE_THRU_TEXT_FLAG
            else
                tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            // Data
            task.formattedDueDate()?.let {
                tvDate.visibility = View.VISIBLE
                tvDate.text = it
                tvDate.setTextColor(if (task.isOverdue) 0xFFE53935.toInt() else 0xFF9E9E9E.toInt())
            } ?: run { tvDate.visibility = View.GONE }

            // Dot de prioridade
            dot.backgroundTintList = ColorStateList.valueOf(task.priorityColor())

            // Ícone do checkbox
            btnComplete.setImageResource(
                if (task.isDone) android.R.drawable.checkbox_on_background
                else             android.R.drawable.checkbox_off_background
            )

            // Cliques
            itemView.setOnClickListener { onEdit(task) }
            btnComplete.setOnClickListener { onToggle(task) }
            btnDelete.setOnClickListener  { onDelete(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(tasks[position])

    override fun getItemCount() = tasks.size
}