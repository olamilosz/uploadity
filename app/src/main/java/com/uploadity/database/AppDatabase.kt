package com.uploadity.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uploadity.database.accounts.Account
import com.uploadity.database.accounts.AccountDao
import com.uploadity.database.blogs.Blog
import com.uploadity.database.blogs.BlogDao
import com.uploadity.database.postaccount.PostAccount
import com.uploadity.database.postaccount.PostAccountDao
import com.uploadity.database.posts.Post
import com.uploadity.database.posts.PostDao

@Database(entities = [Post::class, Account::class, Blog::class, PostAccount::class], version = 15, exportSchema = true)
abstract class AppDatabase : RoomDatabase()  {
    private lateinit var databaseInstance: AppDatabase

    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, AppDatabase::class.java,"the_database.db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance as AppDatabase
        }
    }

    abstract fun postDao(): PostDao

    abstract fun accountDao(): AccountDao

    abstract fun blogDao(): BlogDao

    abstract fun postAccountDao(): PostAccountDao
}