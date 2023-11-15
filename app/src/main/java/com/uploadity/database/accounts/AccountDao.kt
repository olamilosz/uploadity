package com.uploadity.database.accounts

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM accounts")
    fun getAllAccountsLiveData(): LiveData<List<Account>>

    @Query("SELECT * FROM accounts")
    fun getAllAccountsFlow(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccount(id: Int): Account?

    @Query("SELECT * FROM accounts WHERE socialMediaServiceName = :socialMediaName LIMIT 1")
    fun getAccountBySocialMediaName(socialMediaName: String): Account?

    @Insert
    fun insert(account: Account): Long

    @Update
    fun update(account: Account)

    @Delete
    fun delete(account: Account)
}