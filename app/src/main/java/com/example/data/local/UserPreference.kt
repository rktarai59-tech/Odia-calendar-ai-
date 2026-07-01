package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey val id: Int = 1,
    val zodiac: String = "", // e.g., "Mesha", "Vrisha", etc. Empty means not set.
    val language: String = "Odia", // "Odia" or "English"
    val dailyNotificationsEnabled: Boolean = true,
    val notificationTime: String = "07:00 AM"
)
