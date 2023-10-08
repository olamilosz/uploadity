package com.uploadity.api.twitter

import com.uploadity.api.twitter.datamodels.TwitterRequestTokenResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface TwitterApiInterface {

    @POST("oauth/request_token")
    fun requestToken(
        @Header("Authorization") authorizationHeader: String,
        @Query("oauth_callback") callbackUrl: String
    ): Call<ResponseBody>
}