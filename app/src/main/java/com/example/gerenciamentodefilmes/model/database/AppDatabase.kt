package com.example.gerenciamentodefilmes.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gerenciamentodefilmes.model.dao.FilmeDao
import com.example.gerenciamentodefilmes.model.entity.Filme

@Database(entities = [Filme::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun filmeDao(): FilmeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}