package com.tim.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tim.data.model.CatDailyLogEntity
import com.tim.data.model.CatEntity
import com.tim.data.repository.CatRepository

@Composable
fun CatDailyChecklistCard(
    cat: CatEntity,
    log: CatDailyLogEntity?,
    onToggleTask: (CatRepository.TaskType) -> Unit,
    onNotesChanged: (String) -> Unit,
    onDeleteCatClick: () -> Unit,
    onEditCatClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val morningMealDone = log?.morningMeal == true
    val eveningMealDone = log?.eveningMeal == true
    val poopDone = log?.poop == true
    val medicationDone = log?.medication == true

    val totalTasks = if (cat.requiresMedication) 4 else 3
    val completedCount = (if (morningMealDone) 1 else 0) +
            (if (eveningMealDone) 1 else 0) +
            (if (poopDone) 1 else 0) +
            (if (cat.requiresMedication && medicationDone) 1 else 0)

    val isAllDone = completedCount == totalTasks
    val progress = if (totalTasks > 0) completedCount / totalTasks.toFloat() else 0f

    var isNotesExpanded by remember { mutableStateOf(log?.notes?.isNotBlank() == true) }
    var notesText by remember(log?.notes) { mutableStateOf(log?.notes ?: "") }

    val catThemeColor = try {
        Color(android.graphics.Color.parseColor(cat.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cat Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(catThemeColor.copy(alpha = 0.2f))
                            .border(2.dp, catThemeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = cat.avatarEmoji, fontSize = 26.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = cat.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (cat.breed.isNotBlank()) {
                            Text(
                                text = cat.breed,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (cat.requiresMedication && cat.medicationNotes.isNotBlank()) {
                            Text(
                                text = "💊 ${cat.medicationNotes}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge progress
                    Surface(
                        color = if (isAllDone) Color(0xFF38B000) else catThemeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isAllDone) "All Done! 🎉" else "$completedCount/$totalTasks Tasks",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllDone) Color.White else catThemeColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (onEditCatClick != null) {
                        IconButton(
                            onClick = onEditCatClick,
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .testTag("edit_cat_${cat.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Cat Details",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDeleteCatClick,
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .testTag("delete_cat_${cat.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Progress bar
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isAllDone) Color(0xFF38B000) else catThemeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Tasks Grid/Column
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CareTaskChip(
                        title = "Morning Meal",
                        subtitle = "Breakfast 🥣",
                        isCompleted = morningMealDone,
                        activeColor = Color(0xFFFF8C38),
                        modifier = Modifier.weight(1f),
                        testTag = "task_morning_${cat.id}",
                        onClick = { onToggleTask(CatRepository.TaskType.MORNING_MEAL) }
                    )

                    CareTaskChip(
                        title = "Evening Meal",
                        subtitle = "Dinner 🍲",
                        isCompleted = eveningMealDone,
                        activeColor = Color(0xFF4EA8DE),
                        modifier = Modifier.weight(1f),
                        testTag = "task_evening_${cat.id}",
                        onClick = { onToggleTask(CatRepository.TaskType.EVENING_MEAL) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CareTaskChip(
                        title = "Poop",
                        subtitle = "Litter box 💩",
                        isCompleted = poopDone,
                        activeColor = Color(0xFFA0522D),
                        modifier = Modifier.weight(1f),
                        testTag = "task_poop_${cat.id}",
                        onClick = { onToggleTask(CatRepository.TaskType.POOP) }
                    )

                    if (cat.requiresMedication) {
                        CareTaskChip(
                            title = "Medication",
                            subtitle = "Daily dose 💊",
                            isCompleted = medicationDone,
                            activeColor = Color(0xFF8E44AD),
                            modifier = Modifier.weight(1f),
                            testTag = "task_medication_${cat.id}",
                            onClick = { onToggleTask(CatRepository.TaskType.MEDICATION) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Expandable Notes section
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isNotesExpanded = !isNotesExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (notesText.isNotBlank()) "Daily Note: \"$notesText\"" else "+ Add daily note / observations",
                    fontSize = 12.sp,
                    color = if (notesText.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                    fontWeight = if (notesText.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                )
            }

            AnimatedVisibility(visible = isNotesExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = {
                            notesText = it
                            onNotesChanged(it)
                        },
                        placeholder = { Text("e.g., Ate full breakfast, high energy, drank water", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_input_${cat.id}"),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CareTaskChip(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isCompleted) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) activeColor else Color.Transparent,
        label = "borderColor"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .testTag(testTag),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCompleted) activeColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) activeColor else MaterialTheme.colorScheme.surface)
                    .border(1.dp, if (isCompleted) activeColor else MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
