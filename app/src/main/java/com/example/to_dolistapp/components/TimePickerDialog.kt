package com.smarttasks.official.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarttasks.official.ui.theme.OrangePrimary
import java.util.Calendar

@Composable
fun TimePickerDialog(
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cal = Calendar.getInstance()
    var isAm by remember { mutableStateOf(cal.get(Calendar.AM_PM) == Calendar.AM) }
    var selectedHour by remember { 
        val h = cal.get(Calendar.HOUR)
        mutableStateOf(if (h == 0) 12 else h) 
    }
    var selectedMinute by remember { mutableStateOf(cal.get(Calendar.MINUTE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours picker (1 to 12)
                    NumberPicker(
                        value = selectedHour,
                        range = 1..12,
                        loop = true,
                        onValueChange = { selectedHour = it }
                    )

                    Text(
                        ":",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Minutes picker (0 to 59)
                    NumberPicker(
                        value = selectedMinute,
                        range = 0..59,
                        loop = true,
                        onValueChange = { selectedMinute = it }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // AM/PM Toggle Column
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AmPmButton(text = "AM", selected = isAm) { isAm = true }
                        AmPmButton(text = "PM", selected = !isAm) { isAm = false }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val formattedTime = String.format("%02d:%02d %s", selectedHour, selectedMinute, if (isAm) "AM" else "PM")
                    onTimeSelected(formattedTime)
                    onDismiss()
                }
            ) {
                Text("Save", color = OrangePrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun AmPmButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(50.dp, 36.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    loop: Boolean = false,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up arrow
        Icon(
            imageVector = Icons.Default.ArrowDropUp,
            contentDescription = "Increase",
            modifier = Modifier
                .size(40.dp)
                .clickable {
                    if (value < range.last) {
                        onValueChange(value + 1)
                    } else if (loop) {
                        onValueChange(range.first)
                    }
                },
            tint = OrangePrimary
        )

        // Current value
        Surface(
            modifier = Modifier
                .size(65.dp, 60.dp)
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = OrangePrimary.copy(alpha = 0.1f)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", value),
                    style = MaterialTheme.typography.headlineMedium,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Down arrow
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Decrease",
            modifier = Modifier
                .size(40.dp)
                .clickable {
                    if (value > range.first) {
                        onValueChange(value - 1)
                    } else if (loop) {
                        onValueChange(range.last)
                    }
                },
            tint = OrangePrimary
        )
    }
}


