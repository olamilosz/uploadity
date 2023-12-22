package com.uploadity.api.twitter

import com.uploadity.api.twitter.datamodels.CreateTwitterPostParams
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TwitterApiInterface {

    @POST("oauth/request_token")
    fun requestToken(
        @Header("Authorization") authorizationHeader: String,
        @Query("oauth_callback") callbackUrl: String
    ): Call<ResponseBody>

    @POST("oauth/access_token")
    fun accessToken(
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_verifier") oauthVerifier: String
    ): Call<ResponseBody>

    @POST("2/tweets")
    @Headers("Content-Type: application/json")
    fun createTwitterPost(
        @Header("Authorization") authorizationHeader: String,
        @Body requestBody: CreateTwitterPostParams
    ): Call<ResponseBody>

    @DELETE("2/tweets/{id}")
    fun deleteTwitterPost(
        @Header("Authorization") authorizationHeader: String,
        @Path("id") id: String
    ): Call<ResponseBody>
}