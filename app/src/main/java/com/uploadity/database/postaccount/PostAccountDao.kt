package com.uploadity.database.postaccount

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PostAccountDao {
    @Query("SELECT * FROM post_accounts")
    fun getAllPostAccounts(): List<PostAccount>

    @Query("SELECT * FROM post_accounts")
    fun getAllPostAccountsFlow(): Flow<List<PostAccount>>

    @Query("SELECT * FROM post_accounts WHERE postId = :postId")
    fun getPostAccountByPostId(postId: Int): List<PostAccount>

    @Query("SELECT * FROM post_accounts WHERE postId = :postId AND accountId = :accountId AND blogId = :blogId LIMIT 1")
    fun getPostAccount(postId: Int, accountId: Int, blogId: Int): PostAccount?

    @Query("SELECT * FROM post_accounts WHERE postId = :postId")
    fun getPostAccountsByPostIdFlow(postId: Int): Flow<List<PostAccount>>

    @Query("DELETE FROM post_accounts WHERE postId = :postId")
    fun deletePostAccountsByPostId(postId: Int)

    @Query("DELETE FROM post_accounts WHERE accountId = :accountId")
    fun deletePostAccountsByAccountId(accountId: Int)

    @Update
    fun updatePostAccount(postAccount: PostAccount)

    @Insert
    fun insert(postAccount: PostAccount): Long

    @Delete
    fun delete(postAccount: PostAccount)
}