package com.uploadity.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uploadity.NewPostActivity

@Database(entities = [Post::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase()  {
    private lateinit var databaseInstance: AppDatabase

    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context,AppDatabase::class.java,"the_database.db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance as AppDatabase
        }
    }

    abstract fun postDao(): PostDao
}