package com.uploadity.database.posts

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.uploadity.database.posts.Post

@Dao
interface PostDao {
    @Query("SELECT * FROM posts")
    fun getAll(): List<Post>

    @Query("SELECT * FROM posts WHERE isPublished = 0")
    fun getAllUnpublishedPosts(): List<Post>

    @Query("SELECT * FROM posts WHERE isPublished = 1")
    fun getAllPublishedPosts(): List<Post>

    @Query("SELECT * FROM posts")
    fun getAllLive(): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPost(id: Int): Post?

    @Insert
    fun insert(post: Post)

    @Insert
    fun insertAndGetId(post: Post): Long

    @Update
    fun update(post: Post)

    @Delete
    fun delete(post: Post)
}