package com.smarttasks.official.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smarttasks.official.components.BottomNavItem
import com.smarttasks.official.components.PremiumBottomNavigation
import com.smarttasks.official.components.TaskItem
import com.smarttasks.official.model.Task
import com.smarttasks.official.model.getCalendarDays
import com.smarttasks.official.model.isSameDay
import com.smarttasks.official.ui.theme.OrangePrimary
import com.smarttasks.official.utils.DateFormatUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    tasks: List<Task>,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onEditTask: (Task) -> Unit = {}
) {
    var displayMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate  by remember { mutableStateOf(Calendar.getInstance()) }
    var goingForward  by remember { mutableStateOf(true) }

    val year  = displayMonth.get(Calendar.YEAR)
    val month = displayMonth.get(Calendar.MONTH)

    val calendarDays = remember(year, month) { getCalendarDays(year, month) }

    val monthFormatter = DateFormatUtils.MONTH_YEAR_FORMATTER
    val dayFormatter   = DateFormatUtils.DAY_MONTH_FORMATTER

    val selectedDayTasks = remember(tasks, selectedDate.timeInMillis) {
        tasks.filter { isSameDay(it.date, selectedDate.time) }
    }

    // day -> task count for current displayed month
    val taskDaySet = remember(tasks, year, month) {
        val set = mutableSetOf<Int>()
        tasks.forEach { task ->
            val cal = Calendar.getInstance().apply { time = task.date }
            if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                set.add(cal.get(Calendar.DAY_OF_MONTH))
            }
        }
        set
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Calendar",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            PremiumBottomNavigation(
                currentRoute = "calendar",
                onItemClick  = { item ->
                    when (item.route) {
                        "tasks"    -> navController.navigate("task_list") {
                            popUpTo("task_list") { inclusive = true }
                        }
                        "settings" -> navController.navigate("settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Calendar card ──────────────────────────────────────────────
            item {
                Card(
                    shape    = RoundedCornerShape(24.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        // Month navigation
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                goingForward = false
                                displayMonth = (displayMonth.clone() as Calendar)
                                    .apply { add(Calendar.MONTH, -1) }
                            }) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "Previous month",
                                    tint   = OrangePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            AnimatedContent(
                                targetState = monthFormatter.format(displayMonth.time),
                                transitionSpec = {
                                    if (goingForward) {
                                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                        (slideOutHorizontally { -it } + fadeOut())
                                    } else {
                                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                        (slideOutHorizontally { it } + fadeOut())
                                    }
                                },
                                label = "month_anim"
                            ) { label ->
                                Text(
                                    text     = label,
                                    style    = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color    = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(onClick = {
                                goingForward = true
                                displayMonth = (displayMonth.clone() as Calendar)
                                    .apply { add(Calendar.MONTH, 1) }
                            }) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Next month",
                                    tint   = OrangePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Day-of-week headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Su","Mo","Tu","We","Th","Fr","Sa").forEach { d ->
                                Text(
                                    text      = d,
                                    modifier  = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style     = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar grid
                        val weeks = calendarDays.chunked(7)
                        weeks.forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                val cells = week + List(7 - week.size) { null } // pad last row
                                cells.forEach { day ->
                                    Box(
                                        modifier          = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (day != null) {
                                            val dayCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, year)
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, day)
                                            }
                                            val isToday    = isSameDay(dayCal.time, Calendar.getInstance().time)
                                            val isSelected = isSameDay(dayCal.time, selectedDate.time)
                                            val hasTask    = taskDaySet.contains(day)

                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isSelected -> OrangePrimary
                                                            isToday    -> OrangePrimary.copy(alpha = 0.18f)
                                                            else       -> Color.Transparent
                                                        }
                                                    )
                                                    .clickable { selectedDate = dayCal },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text       = day.toString(),
                                                        style      = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color      = when {
                                                            isSelected -> Color.White
                                                            isToday    -> OrangePrimary
                                                            else       -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )
                                                    if (hasTask) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    if (isSelected) Color.White
                                                                    else OrangePrimary
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Selected-day header ────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = dayFormatter.format(selectedDate.time),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    if (selectedDayTasks.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = OrangePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text     = "${selectedDayTasks.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style    = MaterialTheme.typography.labelMedium,
                                color    = OrangePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ── Task list for selected day ─────────────────────────────────
            if (selectedDayTasks.isEmpty()) {
                item {
                    Column(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment   = Alignment.CenterHorizontally,
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🗓️", fontSize = 40.sp)
                        Text(
                            "No tasks for this day",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }
            } else {
                items(selectedDayTasks, key = { it.id }) { task ->
                    TaskItem(
                        task             = task,
                        onToggleComplete = { onToggleTask(task.id) },
                        onEdit           = { onEditTask(task) }
                    )
                }
            }
        }
    }
}



