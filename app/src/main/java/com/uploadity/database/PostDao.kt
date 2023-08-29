package com.uploadity.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PostDao {
    @Query("SELECT * FROM posts")
    fun getAll(): List<Post>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPost(id: Int): Post?

    @Insert
    fun insert(post: Post)

    @Update
    fun update(post: Post)

    @Delete
    fun delete(post: Post)
}