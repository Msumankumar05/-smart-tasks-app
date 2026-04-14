package com.smarttasks.official.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.smarttasks.official.ui.theme.OrangePrimary

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Tasks    : BottomNavItem("tasks",    Icons.Filled.List,          "Tasks")
    object Calendar : BottomNavItem("calendar", Icons.Filled.CalendarMonth, "Calendar")
    object Settings : BottomNavItem("settings", Icons.Filled.Settings,      "Settings")
}

@Composable
fun PremiumBottomNavigation(
    currentRoute: String,
    onItemClick: (BottomNavItem) -> Unit
) {
    val items = listOf(BottomNavItem.Tasks, BottomNavItem.Calendar, BottomNavItem.Settings)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = androidx.compose.ui.unit.Dp(8f)
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = OrangePrimary,
                    selectedTextColor   = OrangePrimary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor      = OrangePrimary.copy(alpha = 0.1f)
                )
            )
        }
    }
}


