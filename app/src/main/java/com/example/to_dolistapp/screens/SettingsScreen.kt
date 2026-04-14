package com.smarttasks.official.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smarttasks.official.components.BottomNavItem
import com.smarttasks.official.components.PremiumBottomNavigation
import com.smarttasks.official.model.Task
import com.smarttasks.official.ui.theme.DeleteRed
import com.smarttasks.official.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController  : NavController,
    tasks          : List<Task>,
    isDarkTheme    : Boolean,
    onToggleDarkTheme     : () -> Unit,
    showCompleted  : Boolean,
    onToggleShowCompleted : () -> Unit,
    onClearCompleted      : () -> Unit
) {
    val totalCount     = tasks.size
    val completedCount = tasks.count { it.isCompleted }
    val pendingCount   = totalCount - completedCount
    val completionPct  = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style      = MaterialTheme.typography.headlineLarge,
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
                currentRoute = "settings",
                onItemClick  = { item ->
                    when (item.route) {
                        "tasks"    -> navController.navigate("task_list") {
                            popUpTo("task_list") { inclusive = true }
                        }
                        "calendar" -> navController.navigate("calendar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(bottom = 32.dp)
        ) {
            // ── Stats hero card ──────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(OrangePrimary, Color(0xFFFF6B35), Color(0xFFFF4500))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            "Your Progress",
                            style      = MaterialTheme.typography.titleMedium,
                            color      = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBlock("Total",   totalCount.toString())
                            StatDivider()
                            StatBlock("Pending", pendingCount.toString())
                            StatDivider()
                            StatBlock("Done",    completedCount.toString())
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Progress bar
                        Text(
                            "$completionPct% completed",
                            style  = MaterialTheme.typography.labelMedium,
                            color  = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress          = { completionPct / 100f },
                            modifier          = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color             = Color.White,
                            trackColor        = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ── Appearance ───────────────────────────────────────────────
            item { SectionLabel("APPEARANCE") }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        icon     = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        title    = "Dark Mode",
                        subtitle = if (isDarkTheme) "Dark theme enabled" else "Light theme enabled",
                        checked  = isDarkTheme,
                        onToggle = onToggleDarkTheme
                    )
                }
            }

            // ── Display ──────────────────────────────────────────────────
            item { SectionLabel("DISPLAY") }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        icon     = Icons.Default.CheckCircle,
                        title    = "Show Completed Tasks",
                        subtitle = if (showCompleted) "Completed tasks are visible" else "Completed tasks are hidden",
                        checked  = showCompleted,
                        onToggle = onToggleShowCompleted
                    )
                }
            }

            // ── Data ─────────────────────────────────────────────────────
            item { SectionLabel("DATA") }
            item {
                SettingsCard {
                    SettingsActionRow(
                        icon     = Icons.Default.DeleteSweep,
                        title    = "Clear Completed Tasks",
                        subtitle = if (completedCount > 0)
                                       "$completedCount task(s) will be removed"
                                   else
                                       "No completed tasks",
                        iconBg   = DeleteRed.copy(alpha = 0.12f),
                        iconTint = DeleteRed,
                        onClick  = { if (completedCount > 0) showClearDialog = true }
                    )
                }
            }

            // ── About ────────────────────────────────────────────────────
            item { SectionLabel("ABOUT") }
            item {
                SettingsCard {
                    SettingsInfoRow(
                        icon     = Icons.Default.TaskAlt,
                        title    = "ToDo List App",
                        subtitle = "Version 1.0.0"
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(start = 72.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    SettingsInfoRow(
                        icon     = Icons.Default.Code,
                        title    = "Built with",
                        subtitle = "Jetpack Compose · Material 3 · Kotlin"
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(start = 72.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    SettingsInfoRow(
                        icon     = Icons.Default.Favorite,
                        title    = "Made with love",
                        subtitle = "Minimalist · Productive · Beautiful"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }

    // Clear completed confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DeleteRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint     = DeleteRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    "Clear Completed Tasks",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Remove all $completedCount completed task(s)? This cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearCompleted()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeleteRed),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ── Reusable sub-composables ───────────────────────────────────────────────

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f))
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.3f))
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsToggleRow(
    icon    : ImageVector,
    title   : String,
    subtitle: String,
    checked : Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(icon = icon, tint = OrangePrimary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    style = MaterialTheme.typography.bodyLarge,  fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
        Switch(
            checked         = checked,
            onCheckedChange = { onToggle() },
            colors          = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OrangePrimary
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon    : ImageVector,
    title   : String,
    subtitle: String,
    iconBg  : Color = OrangePrimary.copy(alpha = 0.12f),
    iconTint: Color = OrangePrimary,
    onClick : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(icon = icon, bg = iconBg, tint = iconTint)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint   = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon    : ImageVector,
    title   : String,
    subtitle: String
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(icon = icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title,    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun IconBox(
    icon: ImageVector,
    bg  : Color = OrangePrimary.copy(alpha = 0.12f),
    tint: Color = OrangePrimary
) {
    Box(
        modifier         = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}



