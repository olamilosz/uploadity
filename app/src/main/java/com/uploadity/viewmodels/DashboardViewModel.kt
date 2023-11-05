package com.uploadity.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.uploadity.database.AppDatabaseRepository
import com.uploadity.database.accounts.Account
import com.uploadity.database.posts.Post

class DashboardViewModel(private val appDatabase: AppDatabaseRepository) : ViewModel() {

    private val allPostFlowList = appDatabase.allPosts.asLiveData()
    private val publishedPostFlowList = appDatabase.allPublishedPosts.asLiveData()
    private val unpublishedPostFlowList = appDatabase.allUnpublishedPosts.asLiveData()

    fun getAllPosts(): LiveData<List<Post>> {
        return allPostFlowList
    }

    fun getAllPublishedPosts(): LiveData<List<Post>> {
        return publishedPostFlowList
    }

    fun getAllUnpublishedPosts(): LiveData<List<Post>> {
        return unpublishedPostFlowList
    }
}

class DashboardViewModelFactory(private val appDatabase: AppDatabaseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(appDatabase) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}