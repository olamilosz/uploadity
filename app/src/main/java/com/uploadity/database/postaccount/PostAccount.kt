package com.uploadity.database.postaccount

import androidx.room.Entity
import com.uploadity.tools.SocialMediaPlatforms

@Entity(tableName = "post_accounts", primaryKeys = ["postId", "accountId", "blogId"])
data class PostAccount(
    val postId: Int,
    val accountId: Int,
    val blogId: Int,
    val isPublished: Boolean,
    val isDeleted: Boolean?,
    val socialMediaPlatformName: String,
    val name: String
)