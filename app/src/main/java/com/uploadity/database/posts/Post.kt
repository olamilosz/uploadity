package com.uploadity.database.posts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var title: String?,
    var description: String?,
    var mediaUri: String?,
    var isPicture: Boolean,
    var isPublished: Boolean,
    val postLink: String?
)