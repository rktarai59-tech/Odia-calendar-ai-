package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalendarViewModel

@Composable
fun NotificationCenterScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val userPref by viewModel.userPreferences.collectAsState()
    val notifications by viewModel.simulatedNotifications.collectAsState()

    val isOdia = userPref.language == "Odia"

    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error

    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // --- Header Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isOdia) "ନୋଟିଫିକେସନ୍ ଓ ସେଟିଂସ" else "Alerts & Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor
                    )
                )
                Text(
                    text = if (isOdia) "ଆପଣଙ୍କ ଦୈନିକ ପାଞ୍ଜି ପରାମର୍ଶ" else "Manage your spiritual alerts",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            if (notifications.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearAllNotifications() },
                    modifier = Modifier.testTag("clear_all_notifications")
                ) {
                    Text(
                        text = if (isOdia) "ସବୁ ସଫା କରନ୍ତୁ" else "Clear All",
                        color = secondaryColor
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Configuration Card ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, outlineColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (isOdia) "ଦୈନିକ ସୂଚନା ସେଟିଂସ (Alert Config)" else "Notification Preferences",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = secondaryColor),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 1. Enable Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOdia) "ଦୈନିକ ରାଶିଫଳ ସୂଚନା" else "Daily Rashi Phala Alerts",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isOdia) "ପ୍ରତିଦିନ ସକାଳେ ଶୁଭ ବେଳାର ନୋଟିଫିକେସନ୍" else "Receive horoscope notification every morning",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Switch(
                                checked = userPref.dailyNotificationsEnabled,
                                onCheckedChange = { viewModel.updateNotificationsConfig(it, userPref.notificationTime) },
                                modifier = Modifier.testTag("enable_notifications_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 16.dp))

                        // 2. Notification Preferred Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isOdia) "ନୋଟିଫିକେସନ୍ ସମୟ" else "Alert Timings",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isOdia) "ନୋଟିଫିକେସନ୍ ମିଳିବାର ସମୟ" else "Preferred daily delivery time",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .border(1.dp, outlineColor, RoundedCornerShape(12.dp))
                                    .clickable { showTimePicker = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = userPref.notificationTime,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = secondaryColor
                                    )
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 16.dp))

                        // 3. Language preference
                        Column {
                            Text(
                                text = if (isOdia) "ଭାଷା ମନୋନୟନ (Language)" else "App & Alert Language",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Odia" to "ଓଡ଼ିଆ (Odia)", "English" to "English (English)").forEach { (langCode, label) ->
                                    val isSelected = userPref.language == langCode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer)
                                            .border(1.dp, if (isSelected) secondaryColor else outlineColor, RoundedCornerShape(12.dp))
                                            .clickable { viewModel.setLanguage(langCode) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) secondaryColor else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Historical/Simulated Notifications List ---
            item {
                Text(
                    text = if (isOdia) "ସୂଚନା ଇତିହାସ (Alert Feed)" else "Spiritual Alert Feed",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = secondaryColor),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (notifications.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isOdia) "କୌଣସି ସୂଚନା ମିଳିନାହିଁ" else "Feed is Empty",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isOdia) "ଆପଣଙ୍କ ପାଞ୍ଜି ଏବଂ ରାଶିଫଳ ଅପଡେଟ୍ ଏଠାରେ ଦେଖାଯିବ।" else "Historic daily alerts and panjika notifications will be displayed here.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.markNotificationAsRead(notif.id) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (!notif.isRead) BorderStroke(1.dp, secondaryColor.copy(alpha = 0.5f)) else BorderStroke(1.dp, outlineColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (notif.isRead) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (notif.isRead) Icons.Default.Notifications else Icons.Default.NotificationImportant,
                                    contentDescription = null,
                                    tint = if (notif.isRead) MaterialTheme.colorScheme.onSurfaceVariant else secondaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (notif.isRead) FontWeight.Normal else FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = notif.timeStr,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.text,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Time Picker Dialog Mock Dialog ---
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = if (isOdia) "ନୋଟିଫିକେସନ୍ ସମୟ ବାଛନ୍ତୁ" else "Select Alert Time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val timings = listOf("05:00 AM", "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM")
                    timings.forEach { time ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateNotificationsConfig(userPref.dailyNotificationsEnabled, time)
                                    showTimePicker = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (userPref.notificationTime == time) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            border = BorderStroke(1.dp, outlineColor)
                        ) {
                            Text(
                                text = time,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (userPref.notificationTime == time) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(text = if (isOdia) "ରଦ୍ଦ କରନ୍ତୁ" else "Cancel", color = errorColor)
                }
            }
        )
    }
}
