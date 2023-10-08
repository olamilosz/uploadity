package com.uploadity.database.blogs

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blogs")
data class Blog(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val accountId: Int,
    val name: String,
    val title: String,
    val url: String,
    val uuid: String
)