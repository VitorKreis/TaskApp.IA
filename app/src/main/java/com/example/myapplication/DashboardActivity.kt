package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.presentation.viewmodel.DashboardViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.button.MaterialButton

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var overdueAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setupPieChart()
        setupOverdueRecyclerView()
        observeViewModel()

        findViewById<MaterialButton>(R.id.btn_view_all_tasks).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun setupPieChart() {
        val pieChart = findViewById<PieChart>(R.id.pie_chart)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 42f
        pieChart.transparentCircleRadius = 47f
        pieChart.setUsePercentValues(false)
        pieChart.setNoDataText("Nenhuma tarefa ainda")
        pieChart.setNoDataTextColor(Color.GRAY)
        pieChart.legend.isEnabled = true
        pieChart.legend.textSize = 12f
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(11f)
    }

    private fun setupOverdueRecyclerView() {
        overdueAdapter = TaskAdapter(
            tasks = emptyList(),
            onEdit = {},
            onDelete = {},
            onToggle = {}
        )
        val recycler = findViewById<RecyclerView>(R.id.recycler_overdue)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = overdueAdapter
    }

    private fun observeViewModel() {
        viewModel.totalCount.observe(this) { count ->
            findViewById<TextView>(R.id.tv_stat_total).text = count.toString()
        }
        viewModel.doneCount.observe(this) { count ->
            findViewById<TextView>(R.id.tv_stat_done).text = count.toString()
        }
        viewModel.pendingCount.observe(this) { count ->
            findViewById<TextView>(R.id.tv_stat_pending).text = count.toString()
        }
        viewModel.overdueCount.observe(this) { count ->
            findViewById<TextView>(R.id.tv_stat_overdue).text = count.toString()
        }
        viewModel.overdueTasks.observe(this) { tasks ->
            overdueAdapter.updateTasks(tasks)
            val tvNoOverdue = findViewById<TextView>(R.id.tv_no_overdue)
            val recyclerOverdue = findViewById<RecyclerView>(R.id.recycler_overdue)
            if (tasks.isEmpty()) {
                tvNoOverdue.visibility = View.VISIBLE
                recyclerOverdue.visibility = View.GONE
            } else {
                tvNoOverdue.visibility = View.GONE
                recyclerOverdue.visibility = View.VISIBLE
            }
        }
        viewModel.priorityCounts.observe(this) { counts ->
            updatePieChart(counts)
        }
    }

    private fun updatePieChart(counts: Map<Int, Int>) {
        val pieChart = findViewById<PieChart>(R.id.pie_chart)

        val priorityLabels = mapOf(0 to "Baixa", 1 to "Média", 2 to "Alta", 3 to "Urgente")
        val priorityColors = mapOf(
            0 to 0xFF4CAF50.toInt(),
            1 to 0xFFFFC107.toInt(),
            2 to 0xFFFF5722.toInt(),
            3 to 0xFFE53935.toInt()
        )

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        for (priority in 0..3) {
            val count = counts.getOrDefault(priority, 0)
            if (count > 0) {
                entries.add(PieEntry(count.toFloat(), priorityLabels[priority]))
                colors.add(priorityColors.getValue(priority))
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 13f
            valueTextColor = Color.WHITE
            sliceSpace = 2f
        }

        pieChart.data = PieData(dataSet)
        pieChart.animateY(600)
        pieChart.invalidate()
    }
}
