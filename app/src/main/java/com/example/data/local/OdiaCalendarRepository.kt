package com.example.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OdiaCalendarRepository(private val dao: OdiaCalendarDao) {

    // Get preferences, and emit a default one if DB is currently empty
    val userPreferences: Flow<UserPreference> = dao.getUserPreferencesFlow().map { pref ->
        pref ?: UserPreference()
    }

    suspend fun getDirectUserPreferences(): UserPreference {
        return dao.getUserPreferences() ?: UserPreference()
    }

    suspend fun saveUserPreferences(userPreference: UserPreference) {
        dao.insertUserPreferences(userPreference)
    }

    suspend fun updateZodiac(zodiac: String) {
        val current = getDirectUserPreferences()
        saveUserPreferences(current.copy(zodiac = zodiac))
    }

    suspend fun updateLanguage(language: String) {
        val current = getDirectUserPreferences()
        saveUserPreferences(current.copy(language = language))
    }

    suspend fun updateNotificationSettings(enabled: Boolean, time: String) {
        val current = getDirectUserPreferences()
        saveUserPreferences(current.copy(dailyNotificationsEnabled = enabled, notificationTime = time))
    }

    // Cached Readings
    suspend fun getSavedReading(dateStr: String, rashi: String): SavedReading? {
        return dao.getSavedReading(dateStr, rashi)
    }

    suspend fun saveReading(savedReading: SavedReading) {
        dao.insertSavedReading(savedReading)
    }

    // Favorite Festivals
    val favoriteFestivals: Flow<List<FavoriteFestival>> = dao.getAllFavoriteFestivalsFlow()

    suspend fun isFavorite(dateStr: String, name: String): Boolean {
        return dao.getFavoriteFestival(dateStr, name) != null
    }

    suspend fun toggleFavorite(dateStr: String, name: String) {
        val existing = dao.getFavoriteFestival(dateStr, name)
        if (existing != null) {
            dao.deleteFavoriteFestival(existing)
        } else {
            dao.insertFavoriteFestival(FavoriteFestival(festivalName = name, dateStr = dateStr))
        }
    }
}
