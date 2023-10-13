package com.uploadity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.uploadity.api.linkedin.LinkedinApiInterface
import com.uploadity.api.linkedin.LinkedinApiServiceBuilder
import com.uploadity.api.linkedin.LinkedinBaseApiInterface
import com.uploadity.api.linkedin.LinkedinBaseApiServiceBuilder
import com.uploadity.api.linkedin.datamodels.AccessTokenResponseModel
import com.uploadity.api.linkedin.datamodels.UserInfoResponseModel
import com.uploadity.api.tumblr.TumblrApiInterface
import com.uploadity.api.tumblr.TumblrApiServiceBuilder
import com.uploadity.api.tumblr.datamodels.TumblrAccessTokenParams
import com.uploadity.api.tumblr.datamodels.TumblrAccessTokenResponse
import com.uploadity.api.twitter.TwitterApiInterface
import com.uploadity.api.twitter.TwitterApiServiceBuilder
import com.uploadity.database.AppDatabase
import com.uploadity.database.accounts.Account
import com.uploadity.database.blogs.Blog
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
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appDao: AppDatabase
    private val viewModel: AccountViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
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

                "tumblr" -> {
                    //TODO: posprzątać i ujednolicić kod
                    val code = appLinkData.getQueryParameter("code") ?: ""

                    if (code != "") {
                        Log.d("TUMBLR CODE", code)

                        Snackbar.make(
                            binding.root,
                            "Tumblr success",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        GlobalScope.launch {
                            getTumblrAccessToken(code)
                        }

                    } else {
                        Snackbar.make(
                            binding.root,
                            "Tumblr cancelled login",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

                "twitter" -> {
                    val oauthToken = appLinkData.getQueryParameter("oauth_token") ?: ""
                    val oauthVerifier = appLinkData.getQueryParameter("oauth_verifier") ?: ""

                    if (oauthToken.isNotEmpty() && oauthVerifier.isNotEmpty()) {
                        getTwitterAccessToken(oauthToken, oauthVerifier)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTwitterAccessToken(oauthToken: String, oauthVerifier: String) {
        val twitterApi = TwitterApiServiceBuilder.buildService(TwitterApiInterface::class.java)
        val requestUrl = "https://api.twitter.com/oauth/access_token"
        val parameterMap = mutableMapOf<String, String>()
        parameterMap["oauth_token"] = oauthToken
        parameterMap["oauth_verifier"] = oauthVerifier

        twitterApi.accessToken(
            oauthToken,
            oauthVerifier

        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseBody = response.body()

                if (responseBody != null) {
                    val responseParameterList = responseBody.string().split("&")
                    val responseParameterMap = mutableMapOf<String, String>()

                    for (parameter in responseParameterList) {
                        responseParameterMap[parameter.substringBefore("=").trim()] =
                            parameter.substringAfter("=").trim()
                    }

                    Log.d("access token twitter", "map: $responseParameterList")

                    val userOauthToken = responseParameterMap["oauth_token"]
                    val userOauthTokenSecret = responseParameterMap["oauth_token_secret"]
                    val userId = responseParameterMap["user_id"]
                    val userName = responseParameterMap["screen_name"]

                    if (userOauthToken != null && userOauthTokenSecret != null && userId != null
                            && userName != null) {

                        runBlocking {
                            addStringDataStoreValue(
                                getString(R.string.twitter_access_token_key), userOauthToken)
                        }

                        runBlocking {
                            addStringDataStoreValue(
                                getString(R.string.twitter_access_token_secret_key), userOauthTokenSecret)
                        }

                        //TODO: można dodać verify_credentials

                        appDao.accountDao().insert(
                            Account(
                                0,
                                userId,
                                userName,
                                "",
                                "",
                                "twitter"
                            )
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("access token twitter error", "error: ${t.message}")
            }
        })
    }

    private fun getTumblrAccessToken(code: String) {
        val tumblrApi = TumblrApiServiceBuilder.buildService(TumblrApiInterface::class.java)
        val clientId = BuildConfig.TUMBLR_CLIENT_ID
        val clientSecret = BuildConfig.TUMBLR_CLIENT_SECRET

        tumblrApi.getAccessToken(
            TumblrAccessTokenParams(
                "authorization_code",
                code,
                clientId,
                clientSecret,
                "https://uploadity.net.pl/tumblr"
            )

        ).enqueue(object: Callback<TumblrAccessTokenResponse> {
            override fun onResponse(
                call: Call<TumblrAccessTokenResponse>,
                response: Response<TumblrAccessTokenResponse>
            ) {
                Log.d("TumblrAccessToken onResponse", "code ${response.code()} ${response.message()}")

                Snackbar.make(
                    binding.root,
                    "Tumblr access token success",
                    Snackbar.LENGTH_SHORT
                ).show()

                val accessToken = response.body()?.accessToken ?: ""

                if (accessToken.isNotEmpty()) {
                    //TODO datastore access token
                    runBlocking {
                        addStringDataStoreValue(getString(R.string.tumblr_access_token_key), accessToken)
                    }

                    Log.d("tumblrApi access token success", accessToken)
                    getTumblrUserInfo(tumblrApi, accessToken)
                }
            }

            override fun onFailure(call: Call<TumblrAccessTokenResponse>, t: Throwable) {
                Log.e("TumblrAccessToken onFailure", "${t.message}")
            }
        })
    }

    private fun getTumblrUserInfo(tumblrApi: TumblrApiInterface, accessToken: String) {
        tumblrApi.getUserInfo(
            "Bearer $accessToken"
        ).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                val userInfo = response.body()?.string() ?: ""
                Log.d("getTumblrUserInfo onResponse", userInfo)

                if (userInfo.isNotEmpty()) {
                    try {
                        val userObject = JSONObject(userInfo)
                            .getJSONObject("response")
                            .getJSONObject("user")

                        val name = userObject.getString("name")
                        val account = Account(
                            0,
                            "",
                            name,
                            "",
                            "",
                            "tumblr"
                        )

                        val accountId = appDao.accountDao().insert(account)

                        val blogs = userObject.getJSONArray("blogs")

                        for (i in 0 until blogs.length()) {
                            if (!blogs.isNull(i)) {
                                val blog = blogs.getJSONObject(i)
                                val isAdmin = blog.getBoolean("admin")

                                if (isAdmin) {
                                    val blogName = blog.getString("name")
                                    val blogTitle = blog.getString("title")
                                    val blogUrl = blog.getString("url")
                                    val blogUuid = blog.getString("uuid")

                                    appDao.blogDao().insert(
                                        Blog(
                                            0,
                                            accountId.toInt(),
                                            blogName,
                                            blogTitle,
                                            blogUrl,
                                            blogUuid
                                        )
                                    )
                                }
                            }
                        }

                    } catch (e: JSONException) {
                        Log.e("getTumblrUserInfo", "JSONException ${e.message}")
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("getTumblrUserInfo onFailure", t.message.toString())
            }
        })
    }

    private fun getAccessToken(code: String) {
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
                Snackbar.make(
                    binding.root,
                    "Linkedin access token success",
                    Snackbar.LENGTH_SHORT
                ).show()

                val accessToken = response.body()?.accessToken ?: ""

                if (accessToken.isNotEmpty()) {
                    //TODO datastore access token
                    runBlocking {
                        addStringDataStoreValue(getString(R.string.linkedin_access_token_key), accessToken)
                    }

                    Log.d("access token success", accessToken)
                    getLinkedinUserInfo(accessToken)
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

    private fun getLinkedinUserInfo(accessToken: String) {
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
