package com.uploadity.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.uploadity.BuildConfig
import com.uploadity.R
import com.uploadity.api.linkedin.LinkedinBaseApiInterface
import com.uploadity.api.linkedin.LinkedinBaseApiServiceBuilder
import com.uploadity.api.linkedin.datamodels.AccessTokenResponseModel
import com.uploadity.database.AppDatabase
import com.uploadity.database.AppDatabaseRepository
import com.uploadity.database.accounts.Account
import com.uploadity.database.accounts.AccountDao
import com.uploadity.tools.SocialMediaPlatforms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountViewModel(private val repository: AppDatabaseRepository): ViewModel() {

    //private var allAccounts: LiveData<List<Account>> = repository.allAccounts.asLiveData()
    private val allAccountsLiveData: LiveData<List<Account>> = repository.allAccounts.asLiveData()
    var loginStatusMessage = MutableLiveData<String>()
    fun createAccount(account: Account) = viewModelScope.launch {
        repository.insert(account)
    }

    //fun getAllAccounts(): LiveData<List<Account>>
    //val getAllAccounts: LiveData<List<Account>> = accountDao.getAllAccountsFlow().asLiveData()
    //val appDatabase: AppDatabase? = null
    //val accountListLiveData: LiveData<List<Account>>? = null


    fun getAllAccounts(): LiveData<List<Account>> {
        return allAccountsLiveData
    }

    fun handleLinkDataFromCallback(appLinkData: Uri) {
        when (appLinkData.lastPathSegment) {
            "linkedin" -> {
                val code = appLinkData.getQueryParameter("code") ?: ""

                if (code != "") {
                    Log.d("LINKEDIN CODE", code)

                    runBlocking {
                        //getAccessToken(code)
                    }

                } else {
                    loginStatusMessage = MutableLiveData("Linkedin cancelled login")
                }
            }
            "twitter" -> {}
            "tumblr" -> {}
        }
    }private fun getAccessToken(code: String) {
        val linkedinApi = LinkedinBaseApiServiceBuilder.buildService(LinkedinBaseApiInterface::class.java)
        val clientId = BuildConfig.LINKEDIN_CLIENT_ID
        val clientSecret = BuildConfig.LINKEDIN_CLIENT_SECRET
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = "grant_type=authorization_code&client_id=$clientId&client_secret=$clientSecret&redirect_uri=https://uploadity.net.pl/linkedin&code=$code"

        linkedinApi.getAccessToken(
            body.toRequestBody(mediaType)
        ).enqueue(object : Callback<AccessTokenResponseModel> {
            override fun onResponse(
                call: Call<AccessTokenResponseModel>,
                response: Response<AccessTokenResponseModel>
            ) {
                loginStatusMessage = MutableLiveData("Linkedin login success")

                val accessToken = response.body()?.accessToken ?: ""

                if (accessToken.isNotEmpty()) {
                    //TODO datastore access token
                    /*runBlocking {
                        addStringDataStoreValue(getString(R.string.linkedin_access_token_key), accessToken)
                    }*/

                    Log.d("access token success", accessToken)
                    //getLinkedinUserInfo(accessToken)
                }
            }

            override fun onFailure(call: Call<AccessTokenResponseModel>, t: Throwable) {
                /*Snackbar.make(
                    binding.root,
                    "Linkedin get access token fail",
                    Snackbar.LENGTH_SHORT
                ).show()*/

                Log.e("access token fail", t.message ?: "")
            }
        })
    }


}

class AccountViewModelFactory(private val repository: AppDatabaseRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}