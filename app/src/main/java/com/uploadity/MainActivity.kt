package com.uploadity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.uploadity.api.linkedin.LinkedinAccessTokenApiInterface
import com.uploadity.api.linkedin.LinkedinAccessTokenServiceBuilder
import com.uploadity.api.linkedin.LinkedinApiInterface
import com.uploadity.api.linkedin.LinkedinApiServiceBuilder
import com.uploadity.api.linkedin.datamodels.AccessTokenResponseModel
import com.uploadity.api.linkedin.datamodels.UserInfoResponseModel
import com.uploadity.database.AppDatabase
import com.uploadity.database.accounts.Account
import com.uploadity.databinding.ActivityMainBinding
import com.uploadity.tools.UserDataStore
import com.uploadity.viewmodels.AccountViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appDao: AppDatabase
    private val viewModel: AccountViewModel by viewModels()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appDao = AppDatabase.getInstance(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, NewPostActivity::class.java)
            this.startActivity(intent)
        }

        val appLinkIntent: Intent = intent
        val appLinkData: Uri? = appLinkIntent.data

        if (appLinkData != null) {
            when (appLinkData.lastPathSegment) {
                "linkedin" -> {
                    val code = appLinkData.getQueryParameter("code") ?: ""

                    if (code != "") {
                        Log.d("LINKEDIN CODE", code)

                        GlobalScope.launch {
                            getAccessToken(code)
                        }

                    } else {
                        Snackbar.make(
                            binding.root,
                            "Linkedin cancelled login",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun getAccessToken(code: String) {
        val linkedinApi = LinkedinAccessTokenServiceBuilder.buildService(LinkedinAccessTokenApiInterface::class.java)
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
                Snackbar.make(
                    binding.root,
                    "Linkedin access token success",
                    Snackbar.LENGTH_SHORT
                ).show()

                val accessToken = response.body()?.accessToken ?: ""

                if (accessToken.isNotEmpty()) {
                    /*val sharedPreferences = this@MainActivity.getPreferences(Context.MODE_PRIVATE) ?: return
                    with (sharedPreferences.edit()) {
                        putString(getString(R.string.linkedin_access_token_key), accessToken)
                        apply()
                    }*/

                    //TODO datastore access token

                    runBlocking {
                        addStringDataStoreValue(getString(R.string.linkedin_access_token_key), accessToken)
                    }

                    Log.d("access token success", accessToken)
                    getLinkedinUserInfo()
                }
            }

            override fun onFailure(call: Call<AccessTokenResponseModel>, t: Throwable) {
                Snackbar.make(
                    binding.root,
                    "Linkedin get access token fail",
                    Snackbar.LENGTH_SHORT
                ).show()

                Log.e("access token fail", t.message ?: "")
            }
        })
    }

    private fun getLinkedinUserInfo() {
        val sharedPreferences = this@MainActivity.getPreferences(Context.MODE_PRIVATE) ?: return
        val accessToken = sharedPreferences.getString(getString(R.string.linkedin_access_token_key), "")
        val linkedinApi = LinkedinApiServiceBuilder.buildService(LinkedinApiInterface::class.java)

        if (accessToken != "") {
            linkedinApi.getUserInfo(
                "Bearer $accessToken"
            ).enqueue(object : Callback<UserInfoResponseModel> {
                override fun onResponse(
                    call: Call<UserInfoResponseModel>,
                    response: Response<UserInfoResponseModel>
                ) {
                    val userInfo = response.body()

                    if (userInfo != null) {
                        /*with (sharedPreferences.edit()) {
                            putString(getString(R.string.linkedin_id_key), userInfo.id)
                            apply()
                        }*/

                        //TODO datastore id

                        runBlocking {
                            addStringDataStoreValue(getString(R.string.linkedin_id_key), userInfo.id)
                        }

                        Log.d("user info id", "user id: ${userInfo.id}")

                        appDao.accountDao().insert(
                            Account(
                                0,
                                userInfo.id,
                                userInfo.name,
                                userInfo.picture,
                                userInfo.email,
                                "linkedin"
                            )
                        )
                    }
                }

                override fun onFailure(call: Call<UserInfoResponseModel>, t: Throwable) {
                    Log.e("User info failure", t.message.toString())
                }
            })
        }
    }

    private suspend fun addStringDataStoreValue(key: String, value: String) {
        val store = UserDataStore(applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            store.saveStringPreference(key, value)
        }

        Log.d("MAIN activity SAVE PREFERENCE", "key $key value $value")
    }
}