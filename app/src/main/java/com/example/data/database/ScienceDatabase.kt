package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChatMessage

@Database(entities = [ChatMessage::class], version = 1, exportSchema = false)
abstract class ScienceDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: ScienceDatabase? = null

        fun getDatabase(context: Context): ScienceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScienceDatabase::class.java,
                    "science_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
