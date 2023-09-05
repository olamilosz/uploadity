package com.uploadity.database.posts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val title: String?,
    val description: String?,
    val mediaUri: String?,
    val isPicture: Boolean
)