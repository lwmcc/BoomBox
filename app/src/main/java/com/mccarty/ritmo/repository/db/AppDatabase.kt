package com.mccarty.ritmo.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mccarty.ritmo.domain.model.RecentlyPlayedItem
import com.mccarty.ritmo.repository.Constants.BOOM_BOX_DB

@Database(entities = [RecentlyPlayedItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    BOOM_BOX_DB)
                    .build()
            }
            return INSTANCE as AppDatabase
        }
    }
}