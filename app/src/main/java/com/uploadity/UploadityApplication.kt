package com.uploadity

import android.app.Application
import com.uploadity.database.AppDatabase
import com.uploadity.database.AppDatabaseRepository

class UploadityApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: AppDatabaseRepository by lazy { AppDatabaseRepository(database.accountDao(), database.blogDao(), database.postDao(), database.postAccountDao()) }
}