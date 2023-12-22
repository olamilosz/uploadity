package com.uploadity.database.postaccount

import androidx.room.Entity
import com.uploadity.tools.SocialMediaPlatforms
import java.io.StringBufferInputStream

@Entity(tableName = "post_accounts", primaryKeys = ["postId", "accountId", "blogId"])
data class PostAccount(
    val postId: Int,
    val accountId: Int,
    val blogId: Int,
    val publishedPostId: String,
    val isPublished: Boolean,
    var isDeleted: Boolean?,
    val socialMediaPlatformName: String,
    val name: String,
    val description: String
)