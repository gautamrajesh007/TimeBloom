package com.timebloom.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarHeatmap(
    modifier: Modifier = Modifier,
    checkInDates: List<Long>, // List of timestamps
    daysToShow: Int = 90
) {
    val calendar = Calendar.getInstance()
    val today = calendar.timeInMillis

    // Create a map of dates to check-in counts
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val checkInMap = checkInDates
        .groupBy { dateFormatter.format(Date(it)) }
        .mapValues { it.value.size }

    Column(modifier = modifier) {
        Text(
            text = "Activity Heatmap",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Week day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Spacer(modifier = Modifier.width(30.dp)) // Space for month labels
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day.take(1),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .width(20.dp)
                        .padding(horizontal = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow {
            item {
                // Calculate weeks to display
                val weeks = mutableListOf<List<CalendarDay>>()
                calendar.add(Calendar.DAY_OF_YEAR, -daysToShow)

                val currentWeek = mutableListOf<CalendarDay>()
                for (i in 0 until daysToShow) {
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-6
                    val dateString = dateFormatter.format(calendar.time)
                    val checkInCount = checkInMap[dateString] ?: 0

                    // Fill empty days at start of first week
                    if (i == 0) {
                        repeat(dayOfWeek) {
                            currentWeek.add(CalendarDay(0, 0, true))
                        }
                    }

                    currentWeek.add(
                        CalendarDay(
                            date = calendar.timeInMillis,
                            checkInCount = checkInCount,
                            isEmpty = false
                        )
                    )

                    // Complete week (Sunday)
                    if (dayOfWeek == 6 || i == daysToShow - 1) {
                        // Fill remaining days if last week
                        if (i == daysToShow - 1) {
                            repeat(6 - dayOfWeek) {
                                currentWeek.add(CalendarDay(0, 0, true))
                            }
                        }
                        weeks.add(currentWeek.toList())
                        currentWeek.clear()
                    }

                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                // Display weeks
                Row {
                    weeks.forEachIndexed { weekIndex, week ->
                        Column(
                            modifier = Modifier.padding(horizontal = 1.dp)
                        ) {
                            week.forEach { day ->
                                HeatmapCell(
                                    checkInCount = day.checkInCount,
                                    isEmpty = day.isEmpty,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }
            }
        }

        // Legend
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            listOf(0, 1, 2, 3, 4).forEach { level ->
                HeatmapCell(
                    checkInCount = level,
                    isEmpty = false,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = "More",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeatmapCell(
    checkInCount: Int,
    isEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    val baseColor = MaterialTheme.colorScheme.primary
    val backgroundColor = when {
        isEmpty -> Color.Transparent
        checkInCount == 0 -> Color.LightGray.copy(alpha = 0.2f)
        checkInCount == 1 -> baseColor.copy(alpha = 0.3f)
        checkInCount == 2 -> baseColor.copy(alpha = 0.5f)
        checkInCount == 3 -> baseColor.copy(alpha = 0.7f)
        else -> baseColor
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .then(
                if (!isEmpty && checkInCount == 0) {
                    Modifier.border(
                        width = 0.5.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(2.dp)
                    )
                } else Modifier
            )
    )
}

private data class CalendarDay(
    val date: Long,
    val checkInCount: Int,
    val isEmpty: Boolean
)