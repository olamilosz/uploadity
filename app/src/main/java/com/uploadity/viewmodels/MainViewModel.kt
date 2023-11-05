package com.uploadity.viewmodels

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.uploadity.BuildConfig
import com.uploadity.api.linkedin.LinkedinApiInterface
import com.uploadity.api.linkedin.LinkedinApiServiceBuilder
import com.uploadity.api.linkedin.LinkedinApiTools
import com.uploadity.api.linkedin.LinkedinBaseApiInterface
import com.uploadity.api.linkedin.LinkedinBaseApiServiceBuilder
import com.uploadity.api.linkedin.datamodels.AccessTokenResponseModel
import com.uploadity.api.linkedin.datamodels.UserInfoResponseModel
import com.uploadity.api.tumblr.TumblrApiInterface
import com.uploadity.api.tumblr.TumblrApiServiceBuilder
import com.uploadity.api.tumblr.datamodels.TumblrAccessTokenParams
import com.uploadity.api.tumblr.datamodels.TumblrAccessTokenResponse
import com.uploadity.api.tumblr.tools.TumblrApiTools
import com.uploadity.api.twitter.TwitterApiInterface
import com.uploadity.api.twitter.TwitterApiServiceBuilder
import com.uploadity.api.twitter.tools.TwitterApiTools
import com.uploadity.database.AppDatabaseRepository
import com.uploadity.database.accounts.Account
import com.uploadity.database.blogs.Blog
import com.uploadity.tools.SocialMediaPlatforms
import com.uploadity.tools.UserDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(
    private val appDatabase: AppDatabaseRepository,
    private val userDataStore: UserDataStore
): ViewModel() {

    private val allAccountsLiveData: LiveData<List<Account>> = appDatabase.allAccounts.asLiveData()
    private var twitterAuthorizationIntent: MutableLiveData<Intent> = MutableLiveData()

    fun getAllAccounts(): LiveData<List<Account>> {
        return allAccountsLiveData
    }

    fun getTwitterAuthorizationIntent(): MutableLiveData<Intent> {
        return twitterAuthorizationIntent
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleLinkDataFromCallback(appLinkData: Uri) {
        when (appLinkData.lastPathSegment) {
            "linkedin" -> {
                val code = appLinkData.getQueryParameter("code") ?: ""

                if (code != "") {
                    Log.d("LINKEDIN CODE", code)

                    runBlocking {
                        getLinkedinAccessToken(code)
                    }

                } else {
                    Log.d("LINKEDIN CANCELLED LOGIN", code)
                }
            }

            "twitter" -> {
                val oauthToken = appLinkData.getQueryParameter("oauth_token") ?: ""
                val oauthVerifier = appLinkData.getQueryParameter("oauth_verifier") ?: ""

                if (oauthToken.isNotEmpty() && oauthVerifier.isNotEmpty()) {
                    getTwitterAccessToken(oauthToken, oauthVerifier)
                }
            }

            "tumblr" -> {
                val code = appLinkData.getQueryParameter("code") ?: ""

                if (code != "") {
                    Log.d("TUMBLR CODE", code)

                    runBlocking {
                        getTumblrAccessToken(code)
                    }

                } else {
                    Log.d("TUMBLR CANCELLED LOGIN", code)
                }
            }
        }
    }

    fun createAuthorizeLinkedinIntent(): Intent {
        val clientId = BuildConfig.LINKEDIN_CLIENT_ID
        val authorizationUrl = LinkedinApiTools().generateAuthorizationUrl(clientId)

        return Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
    }

     fun createAuthorizeTumblrIntent(): Intent {
        val clientId = BuildConfig.TUMBLR_CLIENT_ID
        val authorizationUrl = TumblrApiTools().generateAuthorizationUrl(clientId)

        return Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun requestTwitterToken() {
        val twitterApi = TwitterApiServiceBuilder.buildService(TwitterApiInterface::class.java)
        val callbackUrl = "https://uploadity.net.pl/twitter"

        val authorizationHeader = TwitterApiTools()
            .generateRequestTokenAuthorizationHeader(callbackUrl)

        Log.d("authorizationHeader", authorizationHeader)

        twitterApi.requestToken(
            authorizationHeader,
            callbackUrl

        ).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                val responseBody = response.body()

                if (responseBody != null) {
                    val responseParameterList = responseBody.string().split("&")
                    val responseParameterMap = mutableMapOf<String, String>()

                    for (parameter in responseParameterList) {
                        responseParameterMap[parameter.substringBefore("=").trim()] =
                            parameter.substringAfter("=").trim()
                    }

                    if (responseParameterMap["oauth_callback_confirmed"] == "true") {
                        val oauthToken = responseParameterMap["oauth_token"] ?: ""

                        if (oauthToken.isNotEmpty()) {
                            Log.d("oauth token", "token: $oauthToken")
                            twitterAuthorizationIntent.value = createAuthorizeTwitterIntent(oauthToken)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("requestToken", "onFailure code ${t.message}")
            }
        })
    }

    private fun createAuthorizeTwitterIntent(oauthToken: String): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://api.twitter.com/oauth/authorize?oauth_token=$oauthToken")
        )
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
                            userDataStore.saveStringPreference(userDataStore.twitterAccessTokenKey,
                                userOauthToken)
                        }

                        runBlocking {
                            userDataStore.saveStringPreference(userDataStore.twitterAccessTokenSecretKey,
                                userOauthTokenSecret)
                        }

                        runBlocking {
                            appDatabase.insert(
                                Account(
                                    0,
                                    userId,
                                    userName,
                                    "",
                                    "",
                                    SocialMediaPlatforms.TWITTER.platformName
                                )
                            )
                        }
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

                val accessToken = response.body()?.accessToken ?: ""

                if (accessToken.isNotEmpty()) {
                    //TODO datastore access token
                    runBlocking {
                        userDataStore.saveStringPreference(userDataStore.tumblrAccessTokenKey, accessToken)
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
                            SocialMediaPlatforms.TUMBLR.platformName
                        )

                        var accountId = 0

                        runBlocking {
                            accountId = appDatabase.insertWithId(account).toInt()
                        }

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

                                    runBlocking {
                                        appDatabase.insertBlog(
                                            Blog(
                                                0,
                                                accountId,
                                                blogName,
                                                blogTitle,
                                                blogUrl,
                                                blogUuid
                                            )
                                        )
                                    }
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

    private fun getLinkedinAccessToken(code: String) {
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
                val accessToken = response.body()?.accessToken ?: ""

                if (accessToken.isNotEmpty()) {
                    //TODO datastore access token
                    runBlocking {
                        userDataStore.saveStringPreference(userDataStore.linkedinAccessTokenKey, accessToken)
                    }

                    Log.d("access token success", accessToken)
                    getLinkedinUserInfo(accessToken)
                }
            }

            override fun onFailure(call: Call<AccessTokenResponseModel>, t: Throwable) {
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
                            userDataStore.saveStringPreference(userDataStore.linkedinIdKey, userInfo.id)
                        }

                        Log.d("user info id", "user id: ${userInfo.id}")

                        runBlocking {
                            appDatabase.insert(
                                Account(
                                    0,
                                    userInfo.id,
                                    userInfo.name,
                                    userInfo.picture,
                                    userInfo.email,
                                    SocialMediaPlatforms.LINKEDIN.platformName
                                )
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<UserInfoResponseModel>, t: Throwable) {
                    Log.e("User info failure", t.message.toString())
                }
            })
        }
    }
}

class MainViewModelFactory(
    private val appDatabase: AppDatabaseRepository,
    private val userDataStore: UserDataStore
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(appDatabase, userDataStore) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}