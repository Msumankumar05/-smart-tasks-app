package com.smarttasks.official.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smarttasks.official.model.Task
import com.smarttasks.official.model.TaskDao
import com.smarttasks.official.model.isToday
import com.smarttasks.official.model.isTomorrow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    // Main tasks list
    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Pre-filtered StateFlows (do filtering once, emit separately)
    // This prevents UI from re-filtering on every task change
    val todayPending: StateFlow<List<Task>> = tasks
        .map { allTasks -> allTasks.filter { isToday(it.date) && !it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tomorrowPending: StateFlow<List<Task>> = tasks
        .map { allTasks -> allTasks.filter { isTomorrow(it.date) && !it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val otherPending: StateFlow<List<Task>> = tasks
        .map { allTasks ->
            allTasks.filter { !isToday(it.date) && !isTomorrow(it.date) && !it.isCompleted }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPending: StateFlow<List<Task>> = tasks
        .map { allTasks -> allTasks.filter { !it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<Task>> = tasks
        .map { allTasks -> allTasks.filter { it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(task: Task) {
        viewModelScope.launch {
            taskDao.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskDao.deleteTaskById(taskId)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            taskDao.clearCompleted()
        }
    }
}

class TaskViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



