package com.smarttasks.official

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.smarttasks.official.model.AppDatabase
import com.smarttasks.official.viewmodel.TaskViewModel
import com.smarttasks.official.viewmodel.TaskViewModelFactory
import com.smarttasks.official.model.Task
import com.smarttasks.official.screens.AddTaskScreen
import com.smarttasks.official.screens.CalendarScreen
import com.smarttasks.official.screens.EditTaskScreen
import com.smarttasks.official.screens.SettingsScreen
import com.smarttasks.official.screens.TaskListScreen
import com.smarttasks.official.ui.theme.ToDoListTheme
import com.smarttasks.official.notifications.AlarmScheduler
import com.smarttasks.official.utils.PreferencesManager
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDoListApp()
        }
    }
}

@Composable
fun ToDoListApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val factory = remember { TaskViewModelFactory(database.taskDao()) }
    val viewModel: TaskViewModel = viewModel(factory = factory)
    
    val alarmScheduler = remember { AlarmScheduler(context) }
    val preferencesManager = remember { PreferencesManager(context) }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { }
        )
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // ── App-level state ──────────────────────────────────────────────────
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    var isDarkTheme   by remember { mutableStateOf(preferencesManager.getDarkMode()) }
    var showCompleted by remember { mutableStateOf(preferencesManager.getShowCompleted()) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    val navController = rememberNavController()

    // ── Callbacks ────────────────────────────────────────────────────────
    val onToggleTask: (String) -> Unit = { taskId ->
        val task = tasks.find { it.id == taskId }
        if (task != null) {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            viewModel.updateTask(updatedTask)
            alarmScheduler.scheduleAlarms(updatedTask)
        }
    }

    val onDeleteTask: (String) -> Unit = { taskId ->
        val task = tasks.find { it.id == taskId }
        if (task != null) {
            alarmScheduler.cancelAlarms(task)
        }
        viewModel.deleteTask(taskId)
    }

    val onClearCompleted: () -> Unit = {
        tasks.filter { it.isCompleted }.forEach { 
            alarmScheduler.cancelAlarms(it) 
        }
        viewModel.clearCompleted()
    }

    // ── Theme wrapper ────────────────────────────────────────────────────
    ToDoListTheme(darkTheme = isDarkTheme) {
        NavHost(
            navController  = navController,
            startDestination = "task_list"
        ) {

            // ── Task List ────────────────────────────────────────────────
            composable("task_list") {
                TaskListScreen(
                    navController = navController,
                    viewModel     = viewModel,
                    onToggleTask  = onToggleTask,
                    onDeleteTask  = onDeleteTask,
                    onAddTask     = { navController.navigate("add_task") },
                    onEditTask    = { task -> 
                        taskToEdit = task
                        navController.navigate("edit_task")
                    },
                    showCompleted = showCompleted
                )
            }

            // ── Add Task ─────────────────────────────────────────────────
            composable("add_task") {
                AddTaskScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { name, time, notes, attachment, date ->
                        val newTask = Task(
                            id         = UUID.randomUUID().toString(),
                            name       = name,
                            time       = time,
                            notes      = notes,
                            attachment = attachment,
                            date       = date
                        )
                        viewModel.addTask(newTask)
                        alarmScheduler.scheduleAlarms(newTask)
                        navController.popBackStack()
                    }
                )
            }

            // ── Edit Task ────────────────────────────────────────────────
            composable("edit_task") {
                if (taskToEdit != null) {
                    EditTaskScreen(
                        task = taskToEdit!!,
                        onBack = { navController.popBackStack() },
                        onSave = { name, time, notes, attachment, date ->
                            val updatedTask = taskToEdit!!.copy(
                                name       = name,
                                time       = time,
                                notes      = notes,
                                attachment = attachment,
                                date       = date
                            )
                            viewModel.updateTask(updatedTask)
                            alarmScheduler.scheduleAlarms(updatedTask)
                            taskToEdit = null
                            navController.popBackStack()
                        }
                    )
                }
            }

            // ── Calendar ─────────────────────────────────────────────────
            composable("calendar") {
                CalendarScreen(
                    navController = navController,
                    tasks         = tasks,
                    onToggleTask  = onToggleTask,
                    onDeleteTask  = onDeleteTask,
                    onEditTask    = { task ->
                        taskToEdit = task
                        navController.navigate("edit_task")
                    }
                )
            }

            // ── Settings ─────────────────────────────────────────────────
            composable("settings") {
                SettingsScreen(
                    navController         = navController,
                    tasks                 = tasks,
                    isDarkTheme           = isDarkTheme,
                    onToggleDarkTheme     = { 
                        isDarkTheme = !isDarkTheme
                        preferencesManager.setDarkMode(isDarkTheme)
                    },
                    showCompleted         = showCompleted,
                    onToggleShowCompleted = { 
                        showCompleted = !showCompleted
                        preferencesManager.setShowCompleted(showCompleted)
                    },
                    onClearCompleted      = onClearCompleted
                )
            }
        }
    }
}


