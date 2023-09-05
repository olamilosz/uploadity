package com.uploadity.database.accounts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String?,
    val pictureUrl: String?,
    val email: String?,
    val socialMediaServiceName: String?
)
