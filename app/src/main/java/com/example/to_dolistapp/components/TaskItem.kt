package com.smarttasks.official.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttasks.official.model.Task
import com.smarttasks.official.ui.theme.DeleteRed
import com.smarttasks.official.ui.theme.GrayTextLight
import com.smarttasks.official.ui.theme.OrangePrimary
import com.smarttasks.official.utils.DateFormatUtils
import java.util.Calendar
import java.util.Locale

fun isTaskOverdue(task: Task): Boolean {
    if (task.isCompleted || task.time.isBlank()) return false
    return try {
        val parsedTime = DateFormatUtils.TIME_FORMAT.parse(task.time) ?: return false
        val timeCal = Calendar.getInstance().apply { time = parsedTime }
        
        val taskCalendar = Calendar.getInstance().apply {
            time = task.date
            set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        taskCalendar.add(Calendar.MINUTE, 15)
        Calendar.getInstance().after(taskCalendar)
    } catch (e: Exception) {
        false
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Memoize expensive overdue check
    val isOverdue = remember(task.id, task.isCompleted, task.time, task.date) {
        isTaskOverdue(task)
    }

    // Animated colors for smooth completion feel
    val accentColor by animateColorAsState(
        targetValue = if (task.isCompleted) GrayTextLight.copy(alpha = 0.5f) else OrangePrimary,
        label = "accent_color"
    )
    val cardBgColor by animateColorAsState(
        targetValue = if (task.isCompleted)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surface,
        label = "card_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (task.isCompleted)
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        else
            MaterialTheme.colorScheme.onSurface,
        label = "text_color"
    )

    // Bounce scale on toggle
    var bouncing by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue  = if (bouncing) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        finishedListener = { bouncing = false },
        label = "scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .scale(scale)
            .shadow(
                elevation  = if (task.isCompleted) 0.dp else 4.dp,
                shape      = RoundedCornerShape(20.dp),
                spotColor  = OrangePrimary.copy(alpha = 0.08f)
            )
            .clickable {
                bouncing = true
                onToggleComplete()
            },
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.heightIn(min = 60.dp)) {

            // ── Left accent bar ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )

            // ── Content ──────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Toggle button
                IconButton(
                    onClick   = {
                        bouncing = true
                        onToggleComplete()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector        = if (task.isCompleted)
                            Icons.Filled.CheckCircle
                        else
                            Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Toggle complete",
                        tint               = accentColor,
                        modifier           = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Text column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text            = task.name,
                        style           = MaterialTheme.typography.bodyLarge,
                        fontWeight      = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                        color           = textColor,
                        textDecoration  = if (task.isCompleted) TextDecoration.LineThrough
                                          else TextDecoration.None,
                        maxLines        = 2,
                        overflow        = TextOverflow.Ellipsis
                    )
                    if (task.notes.isNotEmpty() && !task.isCompleted) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text     = task.notes,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isOverdue) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text     = "Task not completed",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = DeleteRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Time badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        OrangePrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text     = task.time,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color    = if (task.isCompleted) GrayTextLight else OrangePrimary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Edit button (only show when not completed)
                if (!task.isCompleted) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit task",
                            tint = OrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


