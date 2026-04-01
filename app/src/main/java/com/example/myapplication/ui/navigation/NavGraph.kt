package com.example.myapplication.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.presentation.viewmodel.CalendarViewModel
import com.example.myapplication.presentation.viewmodel.DashboardViewModel
import com.example.myapplication.presentation.viewmodel.TaskViewModel
import com.example.myapplication.ui.components.BottomNavBar
import com.example.myapplication.ui.components.BottomNavItem
import com.example.myapplication.ui.screens.addedittask.AddEditTaskScreen
import com.example.myapplication.ui.screens.calendar.CalendarScreen
import com.example.myapplication.ui.screens.dashboard.DashboardScreen
import com.example.myapplication.ui.screens.tasklist.TaskListScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val TASK_LIST = "taskList"
    const val CALENDAR = "calendar"
    const val ADD_EDIT_TASK = "addEditTask/{taskId}"

    fun addEditTask(taskId: Long = -1L) = "addEditTask/$taskId"
}

private val bottomNavItems = listOf(
    BottomNavItem(route = Routes.DASHBOARD, label = "Dashboard", icon = Icons.Default.Dashboard),
    BottomNavItem(route = Routes.TASK_LIST, label = "Tarefas", icon = Icons.AutoMirrored.Filled.Assignment),
    BottomNavItem(route = Routes.CALENDAR, label = "Calendário", icon = Icons.Default.CalendarMonth),
)

// Rotas que mostram a bottom nav
private val bottomNavRoutes = setOf(Routes.DASHBOARD, Routes.TASK_LIST, Routes.CALENDAR)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                        onNavigateToTaskList = {
                            navController.navigate(Routes.TASK_LIST) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToAddTask = { navController.navigate(Routes.addEditTask()) },
                        onNavigateToEditTask = { id -> navController.navigate(Routes.addEditTask(id)) }
                    )
                }

                // ── Task List ──────────────────────────────────────────────────
                composable(Routes.TASK_LIST) {
                    val viewModel: TaskViewModel = hiltViewModel()
                    TaskListScreen(
                        viewModel = viewModel,
                        onNavigateToAddTask = { navController.navigate(Routes.addEditTask()) },
                        onNavigateToEditTask = { id -> navController.navigate(Routes.addEditTask(id)) }
                    )
                }

                // ── Calendar ───────────────────────────────────────────────────
                composable(Routes.CALENDAR) {
                    val viewModel: CalendarViewModel = hiltViewModel()
                    CalendarScreen(
                        viewModel = viewModel,
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
    }
}
