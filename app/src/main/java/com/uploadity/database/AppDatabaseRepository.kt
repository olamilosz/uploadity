package com.uploadity.database

import androidx.annotation.WorkerThread
import com.uploadity.database.accounts.Account
import com.uploadity.database.accounts.AccountDao
import com.uploadity.database.blogs.Blog
import com.uploadity.database.blogs.BlogDao
import com.uploadity.database.posts.Post
import com.uploadity.database.posts.PostDao
import kotlinx.coroutines.flow.Flow

class AppDatabaseRepository(
    private val accountDao: AccountDao,
    private val blogDao: BlogDao,
    private val postDao: PostDao
) {
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccountsFlow()
    val allPosts: Flow<List<Post>> = postDao.getAllPostsFlow()
    val allPublishedPosts: Flow<List<Post>> = postDao.getAllPublishedPostsFlow()
    val allUnpublishedPosts: Flow<List<Post>> = postDao.getAllUnpublishedPostsFlow()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(account: Account) {
        accountDao.insert(account)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertWithId(account: Account): Long {
        return accountDao.insert(account)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertBlog(blog: Blog) {
        blogDao.insert(blog)
    }
}