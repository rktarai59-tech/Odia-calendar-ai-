package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.ui.CalendarViewModel
import java.time.format.DateTimeFormatter
import java.time.LocalDate

data class RashiItem(
    val id: String,
    val nameEng: String,
    val nameOdia: String,
    val element: String,
    val planet: String,
    val symbol: String
)

@Composable
fun RashiPhalaScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val userPref by viewModel.userPreferences.collectAsState()
    val dailyHoroscope by viewModel.dailyHoroscope.collectAsState()
    val isLoading by viewModel.rashiPhalaLoading.collectAsState()
    val errorText by viewModel.rashiPhalaError.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showZodiacSelector by remember { mutableStateOf(userPref.zodiac.isEmpty()) }
    var selectedLang by remember { mutableStateOf(userPref.language) }

    val isOdia = selectedLang == "Odia"

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error

    val rashis = listOf(
        RashiItem("Mesha", "Mesha", "ମେଷ", "Fire", "Mars", "🐏"),
        RashiItem("Vrisha", "Vrisha", "ବୃଷ", "Earth", "Venus", "🐂"),
        RashiItem("Mithuna", "Mithuna", "ମିଥୁନ", "Air", "Mercury", "♊"),
        RashiItem("Karka", "Karka", "କର୍କଟ", "Water", "Moon", "🦀"),
        RashiItem("Simha", "Simha", "ସିଂହ", "Fire", "Sun", "🦁"),
        RashiItem("Kanya", "Kanya", "କନ୍ୟା", "Earth", "Mercury", "♍"),
        RashiItem("Tula", "Tula", "ତୁଳା", "Air", "Venus", "scales"),
        RashiItem("Vrischika", "Vrischika", "ବିଛା", "Water", "Mars", "🦂"),
        RashiItem("Dhanu", "Dhanu", "ଧନୁ", "Fire", "Jupiter", "🏹"),
        RashiItem("Makara", "Makara", "ମକର", "Earth", "Saturn", "🐐"),
        RashiItem("Kumbha", "Kumbha", "କୁମ୍ଭ", "Air", "Saturn", "🏺"),
        RashiItem("Meena", "Meena", "ମୀନ", "Water", "Jupiter", "🐟")
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    // Trigger loading of horoscope whenever userPref zodiac or selectedDate changes
    LaunchedEffect(userPref.zodiac, selectedDate) {
        if (userPref.zodiac.isNotEmpty()) {
            viewModel.loadSelectedZodiacHoroscope()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
    ) {
        // --- Custom App Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isOdia) "ଆଜିର ରାଶିଫଳ" else "Daily Rashi Phala",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor
                    )
                )
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            // Quick Toggle Zodiac Selection Button
            if (userPref.zodiac.isNotEmpty()) {
                IconButton(
                    onClick = { showZodiacSelector = !showZodiacSelector },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(primaryColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Grid4x4,
                        contentDescription = "Select Zodiac",
                        tint = secondaryColor
                    )
                }
            }
        }

        if (showZodiacSelector) {
            // --- Zodiac Selection Grid ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, outlineColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isOdia) "ଆପଣଙ୍କ ଜନ୍ମ ରାଶି ବାଛନ୍ତୁ" else "Select Your Birth Rashi (Zodiac)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = secondaryColor),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(280.dp)
                        ) {
                            items(rashis) { rashi ->
                                val isSelected = userPref.zodiac == rashi.id
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1.1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) primaryColor.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) secondaryColor else outlineColor,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            viewModel.selectZodiac(rashi.id)
                                            showZodiacSelector = false
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = getZodiacSymbol(rashi.id),
                                            fontSize = 24.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = rashi.nameOdia,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = rashi.nameEng,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (userPref.zodiac.isEmpty()) {
            // --- Empty State ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = secondaryColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "କୌଣସି ରାଶି ଚୟନ କରାଯାଇ ନାହିଁ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "No Zodiac Sign Selected",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please choose your birth rashi in the panel above to generate today's predictions.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            // --- Selected Zodiac Horoscope Context ---
            val activeRashi = rashis.find { it.id == userPref.zodiac } ?: rashis[0]

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Zodiac Metadata Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, outlineColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getZodiacSymbol(activeRashi.id),
                                    fontSize = 28.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeRashi.nameOdia,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = secondaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${activeRashi.nameEng})",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Text(
                                    text = "ସ୍ୱାମୀ ଗ୍ରହ (Planet): ${if (isOdia) getOdiaPlanet(activeRashi.planet) else activeRashi.planet} | ତତ୍ତ୍ୱ (Element): ${if (isOdia) getOdiaElement(activeRashi.element) else activeRashi.element}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }

                // Language selection & Action Tabs Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language Selector Tabs
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, outlineColor, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            listOf("Odia" to "ଓଡ଼ିଆ", "English" to "English").forEach { (langCode, label) ->
                                val isSelected = selectedLang == langCode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) primaryColor else Color.Transparent)
                                        .clickable { selectedLang = langCode }
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        // Refresh / Trigger Button
                        IconButton(
                            onClick = { viewModel.fetchHoroscopeForZodiac(activeRashi.id, selectedDate) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, outlineColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Forecast",
                                tint = secondaryColor
                            )
                        }
                    }
                }

                // Horoscope Text Display Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("horoscope_text_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, outlineColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            if (isLoading) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = secondaryColor)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (isOdia) "ମହାପ୍ରଭୁ ଶ୍ରୀ ଜଗନ୍ନାଥଙ୍କ ଆରତି ସହିତ ଆପଣଙ୍କ ଭବିଷ୍ୟତ ବିଶ୍ଳେଷଣ ଚାଲିଛି..." else "Analyzing your stars under Lord Jagannatha's guidance...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            } else {
                                val textToShow = if (isOdia) dailyHoroscope?.first else dailyHoroscope?.second
                                if (textToShow != null) {
                                    Text(
                                        text = textToShow,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            lineHeight = 24.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                } else {
                                    Text(
                                        text = if (isOdia) "ରାଶିଫଳ ବିଶ୍ଳେଷଣ ପ୍ରଦାନ କରାଯାଇ ପାରିଲା ନାହିଁ। ଦୟାକରି ରିଫ୍ରେଶ କରନ୍ତୁ।" else "Horoscope reading not found. Please refresh to load.",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Daily Alarm/Push Trigger Panel (Craftsmanship)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = primaryColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Notification Configuration",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (isOdia) "ରାଶିଫଳ ନୋଟିଫିକେସନ୍ ପରୀକ୍ଷଣ" else "Instant Push Notification",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isOdia) {
                                    "ଏହି ବଟନ ଟିପିବା ଦ୍ୱାରା ଆଜିର ରାଶିଫଳକୁ ଆପଣଙ୍କ ଫୋନ୍ ଷ୍ଟାଟସ୍ ବାର୍ ରେ ତୁରନ୍ତ ଦେଖିପାରିବେ।"
                                } else {
                                    "Trigger an instant native Android system notification to see how daily horoscope alerts arrive on your phone."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.triggerInstantSystemNotification() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("trigger_notification_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = secondaryColor,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                Icon(Icons.Default.RingVolume, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = if (isOdia) "ତୁରନ୍ତ ନୋଟିଫିକେସନ୍ ପଠାନ୍ତୁ" else "Trigger Notification Now")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Emoji symbols representing signs
private fun getZodiacSymbol(id: String): String {
    return when (id) {
        "Mesha" -> "🐏"
        "Vrisha" -> "🐂"
        "Mithuna" -> "♊"
        "Karka" -> "🦀"
        "Simha" -> "🦁"
        "Kanya" -> "♍"
        "Tula" -> "⚖️"
        "Vrischika" -> "🦂"
        "Dhanu" -> "🏹"
        "Makara" -> "🐐"
        "Kumbha" -> "🏺"
        "Meena" -> "🐟"
        else -> "🌟"
    }
}

private fun getOdiaPlanet(planet: String): String {
    return when (planet) {
        "Mars" -> "ମଙ୍ଗଳ"
        "Venus" -> "ଶୁକ୍ର"
        "Mercury" -> "ବୁଧ"
        "Moon" -> "ଚନ୍ଦ୍ର"
        "Sun" -> "ସୂର୍ଯ୍ୟ"
        "Jupiter" -> "ବୃହସ୍ପତି"
        "Saturn" -> "ଶନି"
        else -> planet
    }
}

private fun getOdiaElement(element: String): String {
    return when (element) {
        "Fire" -> "ଅଗ୍ନି"
        "Earth" -> "ପୃଥିବୀ"
        "Air" -> "ବାୟୁ"
        "Water" -> "ଜଳ"
        else -> element
    }
}

private fun BoxBorder(color: Color) = BorderStroke(1.dp, color)
