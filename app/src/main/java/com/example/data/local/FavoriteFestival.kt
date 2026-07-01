package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_festivals")
data class FavoriteFestival(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val festivalName: String,
    val dateStr: String, // format "YYYY-MM-DD"
    val customNote: String? = null
)
