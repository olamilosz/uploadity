package com.uploadity.database.postaccount

import androidx.room.Entity

@Entity(tableName = "post_accounts", primaryKeys = ["postId", "accountId"])
data class PostAccount(
    val postId: Long,
    val accountId: Long
)
