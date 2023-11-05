package com.uploadity.database.blogs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BlogDao {
    @Query("SELECT * FROM blogs")
    fun getAllBlogs(): List<Blog>

    @Query("SELECT * FROM blogs WHERE id = :id")
    fun getBlogById(id: Int): Blog?

    @Query("SELECT * FROM blogs WHERE accountId = :accountId")
    fun getBlogsByAccountId(accountId: Int): List<Blog>

    @Query("DELETE FROM blogs WHERE accountId = :accountId")
    fun deleteBlogsByAccountId(accountId: Int)

    @Query("SELECT * FROM blogs LIMIT 1")
    fun getFirstBlog(): Blog?

    @Insert
    fun insert(blog: Blog): Long

    @Update
    fun update(blog: Blog)

    @Delete
    fun delete(blog: Blog)
}