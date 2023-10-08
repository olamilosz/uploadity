package com.uploadity.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.uploadity.database.AppDatabase

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    val accountList = AppDatabase.getInstance(application).accountDao().getAllAccounts()

    fun <T> MutableLiveData<MutableList<T>>.add(item: T) {
        val updatedItems = this.value as ArrayList
        updatedItems.add(item)
        this.value = updatedItems
    }
}