package com.uploadity.database

import androidx.annotation.WorkerThread
import com.uploadity.database.accounts.Account
import com.uploadity.database.accounts.AccountDao
import kotlinx.coroutines.flow.Flow

class AppDatabaseRepository(private val accountDao: AccountDao) {
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccountsFlow()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(account: Account) {
        accountDao.insert(account)
    }
}