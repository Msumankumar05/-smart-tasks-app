package com.smarttasks.official.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val name: String,
    val time: String,
    val notes: String = "",
    val attachment: String? = null,
    val isCompleted: Boolean = false,
    val date: Date
)

enum class TaskSection {
    TODAY, TOMORROW
}


