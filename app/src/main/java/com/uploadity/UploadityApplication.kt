package com.uploadity

import android.app.Application
import com.uploadity.database.AppDatabase

class UploadityApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}