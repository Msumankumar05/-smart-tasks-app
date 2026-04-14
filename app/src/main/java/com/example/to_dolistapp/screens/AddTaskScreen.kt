package com.smarttasks.official.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.smarttasks.official.components.TimePickerDialog
import com.smarttasks.official.ui.theme.OrangePrimary
import com.smarttasks.official.utils.DateFormatUtils
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onBack: () -> Unit,
    onSave: (name: String, time: String, notes: String, attachment: String?, date: Date) -> Unit
) {
    var taskName     by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var notes        by remember { mutableStateOf("") }
    var attachment   by remember { mutableStateOf<String?>(null) }

    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = remember(selectedDate) { selectedDate.time }
    )

    val canSave = taskName.isNotBlank() && selectedTime.isNotBlank()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add New Task",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { if (canSave) onSave(taskName, selectedTime, notes, attachment, selectedDate) },
                containerColor = if (canSave) OrangePrimary
                                 else MaterialTheme.colorScheme.surfaceVariant,
                shape          = RoundedCornerShape(16.dp),
                modifier       = Modifier
                    .size(56.dp)
                    .shadow(if (canSave) 8.dp else 0.dp, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = "Save Task",
                    tint               = if (canSave) MaterialTheme.colorScheme.onPrimary
                                         else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Task Name
            OutlinedTextField(
                value         = taskName,
                onValueChange = { taskName = it },
                label         = { Text("Task Name *") },
                placeholder   = { Text("What do you need to do?") },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(16.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = OrangePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedLabelColor    = OrangePrimary
                ),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Column
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true }
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Date", tint = OrangePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Date",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        val formattedDate = remember(selectedDate) {
                            DateFormatUtils.DATE_FORMATTER.format(selectedDate)
                        }
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Time Column
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showTimePicker = true }
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = "Time", tint = OrangePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Time *",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Text(
                            text  = if (selectedTime.isNotEmpty()) selectedTime else "Select time",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedTime.isNotEmpty())
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }
            }


            // Notes
            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it },
                label         = { Text("Notes (optional)") },
                placeholder   = { Text("Add details…") },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(16.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    focusedLabelColor  = OrangePrimary
                ),
                minLines = 3,
                maxLines = 5
            )

            // Attachment
            OutlinedTextField(
                value         = attachment ?: "",
                onValueChange = { attachment = it.ifBlank { null } },
                label         = { Text("Attachment link (optional)") },
                leadingIcon   = {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Attach",
                        tint = OrangePrimary
                    )
                },
                modifier   = Modifier.fillMaxWidth(),
                shape      = RoundedCornerShape(16.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    focusedLabelColor  = OrangePrimary
                ),
                singleLine = true
            )

            // Hint
            if (!canSave) {
                Text(
                    text  = "* Task name and time are required",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = {
                selectedTime  = it
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        selectedDate = Date(millis)
                    }
                    showDatePicker = false
                }) {
                    Text("Save", color = OrangePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(
                state = dateState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = OrangePrimary,
                    todayDateBorderColor = OrangePrimary,
                    todayContentColor = OrangePrimary
                )
            )
        }
    }
}


