package com.uploadity.database.postaccount

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PostAccountDao {
    @Query("SELECT * FROM post_accounts")
    fun getAllPostAccounts(): List<PostAccount>

    @Query("SELECT * FROM post_accounts WHERE postId = :postId")
    fun getPostAccountByPostId(postId: Int): List<PostAccount>

    @Query("DELETE FROM post_accounts WHERE postId = :postId")
    fun deletePostAccountsByPostId(postId: Int)

    @Insert
    fun insert(postAccount: PostAccount): Long

    @Delete
    fun delete(postAccount: PostAccount)
}