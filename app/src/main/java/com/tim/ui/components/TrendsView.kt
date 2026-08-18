package com.tim.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tim.data.model.CatDailyLogEntity
import com.tim.data.model.CatEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TrendsView(
    cats: List<CatEntity>,
    selectedCatId: Int?,
    logsForCat: List<CatDailyLogEntity>,
    onSelectCat: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCat = cats.firstOrNull { it.id == selectedCatId } ?: cats.firstOrNull()

    if (cats.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No cats added yet. Tap 'Add Cat' above to start tracking!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Cat Selector Horizontal Scroll
        Text(
            text = "Select Cat for Historical Trends:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cats) { cat ->
                val isSelected = cat.id == selectedCat?.id
                val catColor = try {
                    Color(android.graphics.Color.parseColor(cat.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCat(cat.id) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = cat.avatarEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = cat.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = catColor.copy(alpha = 0.2f),
                        selectedLabelColor = catColor,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = catColor,
                        borderWidth = 1.5.dp
                    ),
                    modifier = Modifier.testTag("cat_trend_chip_${cat.id}")
                )
            }
        }

        if (selectedCat != null) {
            val totalLogsCount = logsForCat.size
            val last7Logs = logsForCat.take(7)
            val last30Logs = logsForCat.take(30)
            val catReqMed = selectedCat.requiresMedication

            val completed7Count = last7Logs.count { it.isFullyCompleted(catReqMed) }
            val rate7Days = if (last7Logs.isNotEmpty()) (completed7Count * 100) / last7Logs.size else 0

            val completed30Count = last30Logs.count { it.isFullyCompleted(catReqMed) }
            val rate30Days = if (last30Logs.isNotEmpty()) (completed30Count * 100) / last30Logs.size else 0

            // Streak calculation
            var streak = 0
            for (log in logsForCat) {
                if (log.isFullyCompleted(catReqMed)) {
                    streak++
                } else {
                    break
                }
            }

            // Task breakdown
            val totalTasksLogged = logsForCat.size.coerceAtLeast(1)
            val morningMealRate = (logsForCat.count { it.morningMeal } * 100) / totalTasksLogged
            val eveningMealRate = (logsForCat.count { it.eveningMeal } * 100) / totalTasksLogged
            val poopRate = (logsForCat.count { it.poop } * 100) / totalTasksLogged
            val medRate = if (catReqMed) (logsForCat.count { it.medication } * 100) / totalTasksLogged else 0

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "7-Day Rate",
                    value = "$rate7Days%",
                    subtitle = "$completed7Count/7 Days Done",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF2A9D8F),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Current Streak",
                    value = "$streak Days",
                    subtitle = if (catReqMed) "All 4 tasks met" else "All 3 tasks met",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Color(0xFFFF8C38),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "30-Day Rate",
                    value = "$rate30Days%",
                    subtitle = "$completed30Count/30 Days Done",
                    icon = Icons.Default.BarChart,
                    color = Color(0xFF4EA8DE),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Canvas Completion Trend Chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Daily Completion Score (0 - ${if (catReqMed) 4 else 3})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Last 10 Logs",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Canvas Bar Chart
                    val displayLogs = logsForCat.take(10).reversed()
                    DailyCompletionChart(logs = displayLogs, requiresMedication = catReqMed)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task Breakdown Breakdown
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Task Compliance Breakdown for ${selectedCat.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    TaskProgressRow(title = "🥣 Morning Meal", percentage = morningMealRate, color = Color(0xFFFF8C38))
                    Spacer(modifier = Modifier.height(8.dp))
                    TaskProgressRow(title = "🍲 Evening Meal", percentage = eveningMealRate, color = Color(0xFF4EA8DE))
                    Spacer(modifier = Modifier.height(8.dp))
                    TaskProgressRow(title = "💩 Litter Box Poop", percentage = poopRate, color = Color(0xFFA0522D))
                    if (catReqMed) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TaskProgressRow(title = "💊 Daily Medication", percentage = medRate, color = Color(0xFF8E44AD))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Historical Logs List Header
            Text(
                text = "Historical Care Entries (${logsForCat.size}):",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Individual Log Items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (logsForCat.isEmpty()) {
                    Text(
                        text = "No history recorded yet for ${selectedCat.name}.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    logsForCat.forEach { log ->
                        HistoricalLogItem(log = log, requiresMedication = catReqMed)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DailyCompletionChart(logs: List<CatDailyLogEntity>, requiresMedication: Boolean = true) {
    if (logs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No log data available to graph yet.", fontSize = 12.sp, color = Color.Gray)
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = (width / (logs.size * 1.6f)).coerceIn(16f, 40f)
        val spacing = (width - (barWidth * logs.size)) / (logs.size + 1)

        val maxScore = if (requiresMedication) 4f else 3f

        logs.forEachIndexed { index, log ->
            val score = log.getCompletedCount(requiresMedication).toFloat()
            val barHeight = (score / maxScore) * (height - 30f)
            val x = spacing + index * (barWidth + spacing)
            val y = height - 20f - barHeight

            val completed = log.getCompletedCount(requiresMedication)
            val barColor = if (requiresMedication) {
                when (completed) {
                    4 -> Color(0xFF38B000)
                    3 -> Color(0xFF2A9D8F)
                    2 -> Color(0xFFFF8C38)
                    1 -> Color(0xFFF4A261)
                    else -> Color.LightGray
                }
            } else {
                when (completed) {
                    3 -> Color(0xFF38B000)
                    2 -> Color(0xFF2A9D8F)
                    1 -> Color(0xFFFF8C38)
                    else -> Color.LightGray
                }
            }

            // Draw bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }

        // Base line
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, height - 20f),
            end = Offset(width, height - 20f),
            strokeWidth = 2f
        )
    }

    // Date labels row under canvas
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        logs.forEach { log ->
            val shortDate = if (log.dateString.length >= 10) log.dateString.substring(5) else log.dateString
            Text(
                text = shortDate,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TaskProgressRow(title: String, percentage: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "$percentage%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun HistoricalLogItem(log: CatDailyLogEntity, requiresMedication: Boolean = true) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = log.dateString,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (log.notes.isNotBlank()) {
                    Text(
                        text = "\"${log.notes}\"",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LogStatusBadge("Morning", log.morningMeal, Color(0xFFFF8C38))
                LogStatusBadge("Evening", log.eveningMeal, Color(0xFF4EA8DE))
                LogStatusBadge("Poop", log.poop, Color(0xFFA0522D))
                if (requiresMedication) {
                    LogStatusBadge("Med", log.medication, Color(0xFF8E44AD))
                }
            }
        }
    }
}

@Composable
fun LogStatusBadge(label: String, isDone: Boolean, activeColor: Color) {
    Surface(
        color = if (isDone) activeColor.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDone) activeColor else Color.LightGray)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
            color = if (isDone) activeColor else Color.Gray,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
