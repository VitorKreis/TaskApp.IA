package com.example.myapplication

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.local.entity.TaskEntity
import com.google.android.material.card.MaterialCardView

class TaskAdapter(
    private var tasks: List<TaskEntity>,
    private val onEdit: (TaskEntity) -> Unit,
    private val onDelete: (TaskEntity) -> Unit,
    private val onToggle: (TaskEntity) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val indicator    : View             = view.findViewById(R.id.view_priority_indicator)
        val tvTitle      : TextView         = view.findViewById(R.id.tv_title)
        val tvDescription: TextView         = view.findViewById(R.id.tv_description)
        val tvDate       : TextView         = view.findViewById(R.id.tv_due_date)
        val tvPriority   : TextView         = view.findViewById(R.id.tv_priority_label)
        val cbDone       : CheckBox         = view.findViewById(R.id.cb_done)
        val card         : MaterialCardView = view.findViewById(R.id.card_task)

        fun bind(task: TaskEntity) {
            // Título e Descrição
            tvTitle.text = task.title
            tvDescription.text = task.description
            tvDescription.visibility = if (task.description.isEmpty()) View.GONE else View.VISIBLE

            // Estilo de tarefa concluída (Strikethrough e opacidade)
            if (task.isDone) {
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                card.alpha = 0.6f
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                card.alpha = 1.0f
            }

            // Data
            tvDate.text = task.dueDate?.let {
                java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date(it))
            } ?: "Sem prazo"
            val isOverdue = task.dueDate != null && !task.isDone && task.dueDate < System.currentTimeMillis()
            if (isOverdue) {
                tvDate.setTextColor(0xFFE53935.toInt()) // Vermelho para atrasado
            } else {
                tvDate.setTextColor(0xFF9E9E9E.toInt()) // Cinza padrão
            }

            // Prioridade (Indicador lateral e Label)
            val color = when (task.priority) {
                0 -> 0xFF4CAF50.toInt()
                1 -> 0xFFFFC107.toInt()
                2 -> 0xFFFF5722.toInt()
                else -> 0xFFE53935.toInt()
            }
            indicator.setBackgroundColor(color)
            tvPriority.text = when (task.priority) {
                0 -> "BAIXA"
                1 -> "MÉDIA"
                2 -> "ALTA"
                else -> "URGENTE"
            }
            tvPriority.backgroundTintList = ColorStateList.valueOf(color)

            // Checkbox
            cbDone.setOnCheckedChangeListener(null) // Remove listener para evitar triggers durante o bind
            cbDone.isChecked = task.isDone
            cbDone.setOnClickListener { onToggle(task) }

            // Cliques no card
            itemView.setOnClickListener { onEdit(task) }
            itemView.setOnLongClickListener {
                onDelete(task)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(tasks[position])

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<TaskEntity>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
