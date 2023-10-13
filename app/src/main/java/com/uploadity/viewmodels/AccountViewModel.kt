package com.uploadity.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.uploadity.database.accounts.Account
import com.uploadity.database.accounts.AccountDao
import kotlinx.coroutines.launch

class AccountViewModel(private val accountDao: AccountDao): ViewModel() {

    val getAllAccounts: LiveData<List<Account>> = accountDao.getAllAccountsFlow().asLiveData()

    fun insert(account: Account) = viewModelScope.launch {
        accountDao.insert(account)
    }
}

class AccountViewModelFactory(private val accountDao: AccountDao): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel(accountDao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}