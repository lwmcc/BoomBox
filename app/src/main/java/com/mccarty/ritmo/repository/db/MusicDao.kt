package com.mccarty.ritmo.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mccarty.ritmo.domain.model.RecentlyPlayedItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentlyPlayedItems(item: List<RecentlyPlayedItem>)

    @Query("SELECT * FROM recently_played_item ORDER BY track ASC")
    fun getRecentlyPlayed(): Flow<Array<RecentlyPlayedItem>>
}