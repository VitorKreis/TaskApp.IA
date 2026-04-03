package com.example.myapplication.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
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
    // O filtro é passado na rota para abrir TaskList já segmentada.
    const val TASK_LIST = "taskList/{filter}"
    const val CALENDAR = "calendar"
    const val ADD_EDIT_TASK = "addEditTask/{taskId}"

    fun taskList(filter: Int = 0) = "taskList/$filter"
    fun addEditTask(taskId: Long = -1L) = "addEditTask/$taskId"
}

private val bottomNavItems = listOf(
    BottomNavItem(route = Routes.DASHBOARD, label = "Dashboard", icon = Icons.Default.Dashboard),
    BottomNavItem(route = Routes.taskList(0), label = "Tarefas", icon = Icons.AutoMirrored.Filled.Assignment),
    BottomNavItem(route = Routes.CALENDAR, label = "Calendario", icon = Icons.Default.CalendarMonth),
)

// Rotas base que mostram a bottom nav (sem argumentos resolvidos).
private val bottomNavBaseRoutes = setOf(Routes.DASHBOARD, Routes.TASK_LIST, Routes.CALENDAR)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavBaseRoutes

    // A rota real pode vir como taskList/0, taskList/1...; normalizamos para manter highlight correto.
    val selectedNavRoute = when {
        currentRoute == Routes.TASK_LIST -> Routes.taskList(0)
        else -> currentRoute
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    items = bottomNavItems,
                    currentRoute = selectedNavRoute,
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
                // Dashboard
                composable(Routes.DASHBOARD) {
                    val viewModel: DashboardViewModel = hiltViewModel()
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTaskList = { filter ->
                            navController.navigate(Routes.taskList(filter)) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = false
                            }
                        },
                        onNavigateToAddTask = { navController.navigate(Routes.addEditTask()) },
                        onNavigateToEditTask = { id -> navController.navigate(Routes.addEditTask(id)) }
                    )
                }

                // Task List (com filtro como argumento)
                composable(
                    route = Routes.TASK_LIST,
                    arguments = listOf(navArgument("filter") { type = NavType.IntType; defaultValue = 0 })
                ) { backStackEntry ->
                    val viewModel: TaskViewModel = hiltViewModel()
                    val filter = backStackEntry.arguments?.getInt("filter") ?: 0
                    TaskListScreen(
                        viewModel = viewModel,
                        initialFilter = filter,
                        onNavigateToAddTask = { navController.navigate(Routes.addEditTask()) },
                        onNavigateToEditTask = { id -> navController.navigate(Routes.addEditTask(id)) }
                    )
                }

                // Calendar
                composable(Routes.CALENDAR) {
                    val viewModel: CalendarViewModel = hiltViewModel()
                    CalendarScreen(
                        viewModel = viewModel,
                        onNavigateToAddTask = { navController.navigate(Routes.addEditTask()) },
                        onNavigateToEditTask = { id -> navController.navigate(Routes.addEditTask(id)) }
                    )
                }

                // Add / Edit Task
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
