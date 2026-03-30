package com.example.myapplication.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.presentation.viewmodel.DashboardViewModel
import com.example.myapplication.presentation.viewmodel.TaskViewModel
import com.example.myapplication.ui.screens.addedittask.AddEditTaskScreen
import com.example.myapplication.ui.screens.dashboard.DashboardScreen
import com.example.myapplication.ui.screens.tasklist.TaskListScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val TASK_LIST = "taskList"
    const val ADD_EDIT_TASK = "addEditTask/{taskId}"

    fun addEditTask(taskId: Long = -1L) = "addEditTask/$taskId"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        // ── Dashboard ──────────────────────────────────────────────────
        composable(Routes.DASHBOARD) {
            val viewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToTaskList = { navController.navigate(Routes.TASK_LIST) },
                onNavigateToAddTask = { navController.navigate(Routes.addEditTask()) },
                onNavigateToEditTask = { id -> navController.navigate(Routes.addEditTask(id)) }
            )
        }

        // ── Task List ──────────────────────────────────────────────────
        composable(Routes.TASK_LIST) {
            val viewModel: TaskViewModel = hiltViewModel()
            TaskListScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddTask = { navController.navigate(Routes.addEditTask()) },
                onNavigateToEditTask = { id -> navController.navigate(Routes.addEditTask(id)) }
            )
        }

        // ── Add / Edit Task ────────────────────────────────────────────
        composable(
            route = Routes.ADD_EDIT_TASK,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val viewModel: TaskViewModel = hiltViewModel()
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
            AddEditTaskScreen(
                viewModel = viewModel,
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
