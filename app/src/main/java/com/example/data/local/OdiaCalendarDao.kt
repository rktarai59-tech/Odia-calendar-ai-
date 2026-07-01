package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OdiaCalendarDao {

    // --- User Preferences ---
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferencesFlow(): Flow<UserPreference?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreferences(): UserPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(userPreference: UserPreference)

    // --- Cached Readings ---
    @Query("SELECT * FROM saved_readings WHERE dateStr = :dateStr AND rashi = :rashi LIMIT 1")
    suspend fun getSavedReading(dateStr: String, rashi: String): SavedReading?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedReading(savedReading: SavedReading)

    // --- Favorite Festivals ---
    @Query("SELECT * FROM favorite_festivals ORDER BY dateStr ASC")
    fun getAllFavoriteFestivalsFlow(): Flow<List<FavoriteFestival>>

    @Query("SELECT * FROM favorite_festivals WHERE dateStr = :dateStr AND festivalName = :festivalName LIMIT 1")
    suspend fun getFavoriteFestival(dateStr: String, festivalName: String): FavoriteFestival?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteFestival(favoriteFestival: FavoriteFestival)

    @Delete
    suspend fun deleteFavoriteFestival(favoriteFestival: FavoriteFestival)

    @Query("DELETE FROM favorite_festivals WHERE dateStr = :dateStr AND festivalName = :festivalName")
    suspend fun deleteFavoriteFestivalByNameAndDate(dateStr: String, festivalName: String)
}
