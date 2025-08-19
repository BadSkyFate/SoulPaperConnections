package com.soulpaper.connections.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGrid(
    month: YearMonth,
    appointmentsByDate: Map<LocalDate, Int>,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate?) -> Unit,
    onChangeMonth: (YearMonth) -> Unit,
    firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    onLongPressDate: (LocalDate) -> Unit = {}
) {
    val today = LocalDate.now()
    Card(Modifier.fillMaxWidth()) {

        // Header: Month title + nav + Today
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onChangeMonth(month.minusMonths(1)) }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Previous month")
            }
            Text(
                text = month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { onChangeMonth(month.plusMonths(1)) }) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Next month")
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { onChangeMonth(YearMonth.now()); onSelectDate(today) }) {
                Text("Today")
            }
        }

        // Weekday headers
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = (0 until 7).map { firstDayOfWeek.plus(it.toLong()) }
            days.forEach { dow ->
                Text(
                    text = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 6 rows × 7 columns
        val grid = monthGrid(month, firstDayOfWeek)
        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            grid.forEach { week ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    week.forEach { date ->
                        val inMonth = date.month == month.month
                        val isPast = date.isBefore(today)
                        val isToday = date == today
                        val isSelected = date == selectedDate

                        val count = appointmentsByDate[date] ?: 0
                        // Shade intensity grows with count (log-scale)
                        val intensity = if (count > 0) {
                            val v = 0.10f + (0.12f * ln((count + 1).toFloat()))
                            min(0.38f, max(0.10f, v))
                        } else 0f

                        val cellBg =
                            if (intensity > 0f) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = intensity)
                            else MaterialTheme.colorScheme.surface

                        val contentAlpha =
                            when {
                                !inMonth -> 0.45f
                                isPast && !isToday -> 0.75f
                                else -> 1f
                            }

                        val borderMod =
                            when {
                                isSelected -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                                isToday -> Modifier.border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                                else -> Modifier
                            }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(cellBg, shape = MaterialTheme.shapes.small)
                                .then(borderMod)
                                .clip(MaterialTheme.shapes.small)
                                .combinedClickable(
                                    onClick = {
                                        if (selectedDate == date) onSelectDate(null) else onSelectDate(date)
                                    },
                                    onLongClick = { onLongPressDate(date) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.alpha(contentAlpha)
                            )
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (count <= 9) count.toString() else "9+",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun monthGrid(month: YearMonth, firstDayOfWeek: DayOfWeek): List<List<LocalDate>> {
    val firstOfMonth = month.atDay(1)
    val start = firstOfMonth.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    return (0 until 6).map { week ->
        (0 until 7).map { day -> start.plusDays((week * 7 + day).toLong()) }
    }
}
