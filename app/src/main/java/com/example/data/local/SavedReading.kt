package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_readings")
data class SavedReading(
    @PrimaryKey val dateStr: String, // format "YYYY-MM-DD"
    val rashi: String,               // e.g. "Mesha"
    val readingOdia: String,
    val readingEnglish: String,
    val timestamp: Long = System.currentTimeMillis()
)
