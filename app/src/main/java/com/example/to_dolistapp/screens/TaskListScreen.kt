package com.smarttasks.official.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smarttasks.official.components.PremiumBottomNavigation
import com.smarttasks.official.components.TaskItem
import com.smarttasks.official.model.Task
import com.smarttasks.official.ui.theme.DeleteRed
import com.smarttasks.official.ui.theme.OrangePrimary
import com.smarttasks.official.utils.DateFormatUtils
import com.smarttasks.official.viewmodel.TaskViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController : NavController,
    viewModel     : TaskViewModel,
    onToggleTask  : (String) -> Unit,
    onDeleteTask  : (String) -> Unit,
    onAddTask     : () -> Unit,
    onEditTask    : (Task) -> Unit = {},
    showCompleted : Boolean
) {
    var selectedFilter by remember { mutableStateOf("All") }

    // Collect pre-filtered StateFlows from ViewModel (done once, not recomputed)
    val todayPending    by viewModel.todayPending.collectAsState()
    val tomorrowPending by viewModel.tomorrowPending.collectAsState()
    val otherPending    by viewModel.otherPending.collectAsState()
    val allPending      by viewModel.allPending.collectAsState()
    val completedTasks  by viewModel.completedTasks.collectAsState()
    val allTasks        by viewModel.tasks.collectAsState()

    // ── Which list to show for the selected filter ────────────────────────
    data class FilterDef(val key: String, val label: String, val count: Int)
    val filters = listOf(
        FilterDef("All",      "All",      allPending.size),
        FilterDef("Today",    "Today",    todayPending.size),
        FilterDef("Tomorrow", "Tomorrow", tomorrowPending.size),
        FilterDef("Done",     "Done",     completedTasks.size)
    )

    // ── Greeting ──────────────────────────────────────────────────────────
    // Cache greeting hour to avoid repeated Calendar.getInstance() calls
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val (greetingText, greetingEmoji) = when {
        hour in 5..11 -> "Good morning"  to "☀️"
        hour in 12..16 -> "Good afternoon" to "🌤️"
        hour in 17..20 -> "Good evening"  to "🌆"
        else           -> "Good night"    to "🌙"
    }
    val dateLabel = remember {
        DateFormatUtils.DAY_MONTH_FORMATTER.format(Date())
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Tasks",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onAddTask,
                containerColor = OrangePrimary,
                shape          = RoundedCornerShape(20.dp),
                modifier       = Modifier
                    .size(60.dp)
                    .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = OrangePrimary)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint     = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        bottomBar = {
            PremiumBottomNavigation(
                currentRoute = "tasks",
                onItemClick  = { item ->
                    when (item.route) {
                        "calendar" -> navController.navigate("calendar")
                        "settings" -> navController.navigate("settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // ── Greeting hero card ────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFFF8C00),
                                    Color(0xFFFF5722)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(greetingEmoji, fontSize = 30.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    greetingText,
                                    style      = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = Color.White
                                )
                                Text(
                                    dateLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.82f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HeroStat(
                                value    = "${allPending.size}",
                                label    = "Pending",
                                modifier = Modifier.weight(1f)
                            )
                            HeroStat(
                                value    = "${todayPending.size}",
                                label    = "Due today",
                                modifier = Modifier.weight(1f)
                            )
                            HeroStat(
                                value    = "${allTasks.count { it.isCompleted }}",
                                label    = "Completed",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── Filter chips ──────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { f ->
                        PremiumFilterChip(
                            label    = f.label,
                            count    = f.count,
                            selected = selectedFilter == f.key,
                            onClick  = { selectedFilter = f.key }
                        )
                    }
                }
            }

            // ── Task list (animated transition) ───────────────────────────
            when (selectedFilter) {

                "All" -> {
                    if (allPending.isEmpty() && completedTasks.isEmpty()) {
                        item { EmptyState("No tasks yet", "Tap + to add your first task 🚀") }
                    }
                    if (todayPending.isNotEmpty()) {
                        item { InlineHeader("Today", todayPending.size) }
                        items(todayPending, key = { it.id }) { task ->
                            SwipeToDelete(onDelete = { onDeleteTask(task.id) }) {
                                TaskItem(task = task, onToggleComplete = { onToggleTask(task.id) }, onEdit = { onEditTask(task) })
                            }
                        }
                    }
                    if (tomorrowPending.isNotEmpty()) {
                        item { InlineHeader("Tomorrow", tomorrowPending.size) }
                        items(tomorrowPending, key = { it.id }) { task ->
                            SwipeToDelete(onDelete = { onDeleteTask(task.id) }) {
                                TaskItem(task = task, onToggleComplete = { onToggleTask(task.id) }, onEdit = { onEditTask(task) })
                            }
                        }
                    }
                    if (otherPending.isNotEmpty()) {
                        item { InlineHeader("Upcoming", otherPending.size) }
                        items(otherPending, key = { it.id }) { task ->
                            SwipeToDelete(onDelete = { onDeleteTask(task.id) }) {
                                TaskItem(task = task, onToggleComplete = { onToggleTask(task.id) }, onEdit = { onEditTask(task) })
                            }
                        }
                    }
                    if (completedTasks.isNotEmpty() && showCompleted) {
                        item { InlineHeader("Completed", completedTasks.size) }
                        items(completedTasks, key = { it.id }) { task ->
                            SwipeToDelete(onDelete = { onDeleteTask(task.id) }) {
                                TaskItem(task = task, onToggleComplete = { onToggleTask(task.id) }, onEdit = { onEditTask(task) })
                            }
                        }
                    }
                }

                "Today" -> {
                    if (todayPending.isEmpty()) {
                        item { EmptyState("You're free today! 🎉", "No tasks due for today.") }
                    } else {
                        items(todayPending, key = { it.id }) { task ->
                            SwipeToDelete(onDelete = { onDeleteTask(task.id) }) {
                                TaskItem(task = task, onToggleComplete = { onToggleTask(task.id) }, onEdit = { onEditTask(task) })
                            }
                        }
                    }
                }

                "Tomorrow" -> {
                    if (tomorrowPending.isEmpty()) {
                        item { EmptyState("Nothing planned 🌅", "No tasks for tomorrow yet.") }
                    } else {
                        items(tomorrowPending, key = { it.id }) { task ->
                            SwipeToDelete(onDelete = { onDeleteTask(task.id) }) {
                                TaskItem(task = task, onToggleComplete = { onToggleTask(task.id) }, onEdit = { onEditTask(task) })
                            }
                        }
                    }
                }

                "Done" -> {
                    if (completedTasks.isEmpty()) {
                        item { EmptyState("No completed tasks yet", "Keep going! ✨") }
                    } else {
                        items(completedTasks, key = { it.id }) { task ->
                            SwipeToDelete(onDelete = { onDeleteTask(task.id) }) {
                                TaskItem(task = task, onToggleComplete = { onToggleTask(task.id) }, onEdit = { onEditTask(task) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Premium filter chip ────────────────────────────────────────────────────

@Composable
private fun PremiumFilterChip(
    label   : String,
    count   : Int,
    selected: Boolean,
    onClick : () -> Unit
) {
    val bgColor   by animateColorAsState(
        targetValue = if (selected) OrangePrimary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        animationSpec = tween(200),
        label = "chip_text"
    )

    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(50),
        color   = bgColor,
        shadowElevation = if (selected) 6.dp else 2.dp,
        modifier = Modifier.height(38.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                label,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color      = textColor
            )
            if (count > 0) {
                Box(
                    modifier         = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) Color.White.copy(alpha = 0.25f)
                            else OrangePrimary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$count",
                        style  = MaterialTheme.typography.labelSmall,
                        color  = if (selected) Color.White else OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 10.sp
                    )
                }
            }
        }
    }
}

// ── Inline section header ──────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, count: Int) { // kept public for CalendarScreen compat
    InlineHeader(title, count)
}

@Composable
private fun InlineHeader(title: String, count: Int) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title.uppercase(),
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            letterSpacing = 1.2.sp
        )
        Surface(
            shape = CircleShape,
            color = OrangePrimary.copy(alpha = 0.12f)
        ) {
            Text(
                "$count",
                modifier   = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = OrangePrimary
            )
        }
    }
}

// ── Hero stat block ────────────────────────────────────────────────────────

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        color    = Color.White.copy(alpha = 0.18f)
    ) {
        Column(
            modifier              = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White,
                fontSize   = 22.sp
            )
            Text(
                label,
                style  = MaterialTheme.typography.labelSmall,
                color  = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Empty state ────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp, horizontal = 32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            if (title.contains("free") || title.contains("Nothing")) "🎉" else "📋",
            fontSize = 48.sp
        )
        Text(
            title,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            textAlign  = TextAlign.Center
        )
        Text(
            subtitle,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            textAlign = TextAlign.Center
        )
    }
}

// ── Swipe-to-delete wrapper ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDelete(
    onDelete: () -> Unit,
    content : @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(); true
            } else false
        }
    )

    SwipeToDismissBox(
        state                    = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val bg by animateColorAsState(
                targetValue   = if (state.targetValue == SwipeToDismissBoxValue.EndToStart)
                    DeleteRed else Color.Transparent,
                animationSpec = tween(220),
                label         = "delete_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint     = Color.White,
                    modifier = Modifier.padding(end = 20.dp).size(22.dp)
                )
            }
        }
    ) {
        content()
    }
}


