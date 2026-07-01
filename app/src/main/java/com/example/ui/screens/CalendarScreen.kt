package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.OdiaDayInfo
import com.example.ui.CalendarViewModel
import com.example.utils.OdiaCalendarCalculator
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dayInfo by viewModel.selectedDayInfo.collectAsState()
    val userPref by viewModel.userPreferences.collectAsState()
    val favoriteFestivals by viewModel.favoriteFestivals.collectAsState()

    var currentYearMonth by remember { mutableStateOf(LocalDate.of(2026, 7, 1)) } // Default starts in July 2026

    val isOdia = userPref.language == "Odia"

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
    ) {
        // --- Premium Header ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, outlineColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, 
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = getHeaderMonthYearText(currentYearMonth, isOdia),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = secondaryColor
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = getHeaderOdiaEraText(currentYearMonth, isOdia),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    IconButton(
                        onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward, 
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- Calendar Grid Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Day of Week Header Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    val daysOfWeek = if (isOdia) {
                        listOf("ରବି", "ସୋମ", "ମଙ୍ଗଳ", "ବୁଧ", "ଗୁରୁ", "ଶୁକ୍ର", "ଶନି")
                    } else {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    }
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (day == "ରବି" || day == "Sun") secondaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Monthly Calendar Days Grid
                val firstDayOfMonth = currentYearMonth.withDayOfMonth(1)
                val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7 // offset Sunday (Sunday is 7 in java time, so 7 % 7 = 0)
                val daysInMonth = currentYearMonth.lengthOfMonth()

                val totalCells = dayOfWeekOffset + daysInMonth
                val totalRows = (totalCells + 6) / 7

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(totalRows) { rowIndex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (colIndex in 0..6) {
                                val cellIndex = rowIndex * 7 + colIndex
                                val dayNumber = cellIndex - dayOfWeekOffset + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val cellDate = currentYearMonth.withDayOfMonth(dayNumber)
                                    val cellInfo = OdiaCalendarCalculator.getOdiaDayInfo(cellDate)
                                    val isSelected = cellDate == selectedDate
                                    val isToday = cellDate == LocalDate.now()

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(0.85f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when {
                                                    isSelected -> primaryColor.copy(alpha = 0.3f)
                                                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.dp,
                                                color = if (isSelected) secondaryColor else if (isToday) outlineColor else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { viewModel.changeSelectedDate(cellDate) }
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            // English Day Text
                                            Text(
                                                text = dayNumber.toString(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (colIndex == 0) secondaryColor else MaterialTheme.colorScheme.onSurface
                                                )
                                            )

                                            // Odia Solar day or Tithi abbreviation
                                            Text(
                                                text = if (isOdia) {
                                                    // Show Odia day of month
                                                    "${cellInfo.odiaDay} ଦିନ"
                                                } else {
                                                    "${cellInfo.odiaMonthEng} ${cellInfo.odiaDay}"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 8.sp,
                                                    color = if (isSelected) secondaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                ),
                                                textAlign = TextAlign.Center
                                            )

                                            // Small indicator for festivals
                                            if (cellInfo.festivalEng != null) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(secondaryColor)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Empty box for padding days
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // --- Bottom Selected Day Details Sheet inside the column ---
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SelectedDayPanjikaDetails(
                            dayInfo = dayInfo,
                            isOdia = isOdia,
                            isFavorited = favoriteFestivals.any { it.dateStr == selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE) && it.festivalName == (dayInfo.festivalEng ?: "") },
                            onFavoriteClick = {
                                if (dayInfo.festivalEng != null) {
                                    viewModel.toggleFavoriteFestival(selectedDate, dayInfo.festivalEng!!)
                                }
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SelectedDayPanjikaDetails(
    dayInfo: OdiaDayInfo,
    isOdia: Boolean,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val errorColor = MaterialTheme.colorScheme.error

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

        // Selected Date Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isOdia) {
                        "ଆଜିର ପାଞ୍ଜି ପ୍ରାସଙ୍ଗିକତା"
                    } else {
                        "Daily Panjika Context"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = secondaryColor)
                )
                Text(
                    text = dayInfo.englishDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            // Favorite/Reminder Toggle for Festival
            if (dayInfo.festivalEng != null) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite Festival",
                        tint = secondaryColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Festival Banner Card
        if (dayInfo.festivalEng != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Festival,
                        contentDescription = "Festival Day",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ପର୍ବପର୍ବାଣୀ / Festival" else "Today's Festival",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isOdia) dayInfo.festivalOdia ?: "" else dayInfo.festivalEng ?: "",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }
            }
        }

        // Panjika Parameters Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PanjikaParamCard(
                label = if (isOdia) "ରାଶି ମାସ (Solar Month)" else "Solar Month",
                value = if (isOdia) "${dayInfo.odiaMonthOdia} ${dayInfo.odiaDay} ଦିନ" else "${dayInfo.odiaMonthEng} ${dayInfo.odiaDay}",
                icon = Icons.Default.WbSunny,
                modifier = Modifier.weight(1f)
            )
            PanjikaParamCard(
                label = if (isOdia) "ଚାନ୍ଦ୍ର ମାସ (Lunar Month)" else "Lunar Month",
                value = if (isOdia) dayInfo.lunarMonthOdia else dayInfo.lunarMonthEng,
                icon = Icons.Default.Brightness3,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PanjikaParamCard(
                label = if (isOdia) "ପକ୍ଷ / Paksha" else "Paksha",
                value = if (isOdia) dayInfo.pakshaOdia else dayInfo.pakshaEng,
                icon = Icons.Default.FilterVintage,
                modifier = Modifier.weight(1f)
            )
            PanjikaParamCard(
                label = if (isOdia) "ତିଥି / Tithi" else "Tithi",
                value = if (isOdia) dayInfo.tithiOdia else dayInfo.tithiEng,
                icon = Icons.Default.CalendarMonth,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PanjikaParamCard(
                label = if (isOdia) "ନକ୍ଷତ୍ର / Nakshatra" else "Nakshatra",
                value = if (isOdia) dayInfo.nakshatraOdia else dayInfo.nakshatraEng,
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )
            PanjikaParamCard(
                label = if (isOdia) "ଯୋଗ / Status" else "Auspiciousness",
                value = if (dayInfo.isAuspicious) {
                    if (isOdia) "ଶୁଭ ଯୋଗ" else "Auspicious"
                } else {
                    if (isOdia) "ସାଧାରଣ ଦିନ" else "Standard Day"
                },
                icon = if (dayInfo.isAuspicious) Icons.Default.CheckCircle else Icons.Default.Info,
                iconColor = if (dayInfo.isAuspicious) Color(0xFF81C784) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Auspicious/Inauspicious timings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, outlineColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isOdia) "ଦୈନିକ ବେଳା ନିର୍ଣ୍ଣୟ (Time Periods)" else "Auspicious Timings",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = secondaryColor)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF81C784)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOdia) "ଶୁଭ ଅମୃତ ବେଳା: ${dayInfo.auspiciousTimings}" else "Amruta Bela: ${dayInfo.auspiciousTimings}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(errorColor))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOdia) "ରାହୁ କାଳ / ବାରବେଳା: ${dayInfo.inauspiciousTimings}" else "Rahu Kala / Inauspicious: ${dayInfo.inauspiciousTimings}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PanjikaParamCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

private fun getHeaderMonthYearText(date: LocalDate, isOdia: Boolean): String {
    return if (isOdia) {
        val monthOdiaList = listOf(
            "ଜାନୁଆରୀ", "ଫେବୃଆରୀ", "ମାର୍ଚ୍ଚ", "ଏପ୍ରିଲ", "ମେ", "ଜୁନ୍",
            "ଜୁଲାଇ", "ଅଗଷ୍ଟ", "ସେପ୍ଟେମ୍ବର", "ଅକ୍ଟୋବର", "ନଭେମ୍ବର", "ଡିସେମ୍ବର"
        )
        val monthStr = monthOdiaList[date.monthValue - 1]
        // Convert year number to Odia text/numerals
        val yearOdia = date.year.toString()
            .replace('0', '୦')
            .replace('1', '୧')
            .replace('2', '୨')
            .replace('3', '୩')
            .replace('4', '୪')
            .replace('5', '୫')
            .replace('6', '୬')
            .replace('7', '୭')
            .replace('8', '୮')
            .replace('9', '୯')
        "$monthStr - $yearOdia"
    } else {
        date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
    }
}

private fun getHeaderOdiaEraText(date: LocalDate, isOdia: Boolean): String {
    // Odia Calendar Year / Saka Era is approximately English Year - 78
    val sakaYear = date.year - 78
    val odiaYear = date.year - 593 // approximate Odia San calendar era
    return if (isOdia) {
        "ଶକାବ୍ଦ: ${sakaYear.toString().replaceCharToOdia()} | ସଂବତ: ${odiaYear.toString().replaceCharToOdia()}"
    } else {
        "Saka Samvat: $sakaYear | San Era: $odiaYear"
    }
}

private fun String.replaceCharToOdia(): String {
    return this.replace('0', '୦')
        .replace('1', '୧')
        .replace('2', '୨')
        .replace('3', '୩')
        .replace('4', '୪')
        .replace('5', '୫')
        .replace('6', '୬')
        .replace('7', '୭')
        .replace('8', '୮')
        .replace('9', '୯')
}

private fun BoxBorder(color: Color) = BorderStroke(1.dp, color)
