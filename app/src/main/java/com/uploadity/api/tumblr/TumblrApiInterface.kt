package com.uploadity.api.tumblr

import com.uploadity.api.tumblr.datamodels.TumblrAccessTokenParams
import com.uploadity.api.tumblr.datamodels.TumblrAccessTokenResponse
import com.uploadity.api.tumblr.datamodels.TumblrCreatePostParams
import com.uploadity.api.tumblr.datamodels.TumblrDeletePostParams
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TumblrApiInterface {

    @POST("v2/oauth2/token")
    fun getAccessToken(
        @Body requestBody: TumblrAccessTokenParams
    ): Call<TumblrAccessTokenResponse>

    @GET("v2/user/info")
    fun getUserInfo(
        @Header("Authorization") authorization: String
    ): Call<ResponseBody>

    @POST("v2/blog/{blog-identifier}/post")
    @Headers("Content-Type: multipart/form-data")
    fun createTextPost(
        @Header("Authorization") authorization: String,
        @Path("blog-identifier") blogIdentifier: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @POST("v2/blog/{blog-identifier}/post")
    @Headers("Content-Type: application/json")
    fun createPost(
        @Header("Authorization") authorization: String,
        @Path("blog-identifier") blogIdentifier: String,
        @Body requestBody: TumblrCreatePostParams
    ): Call<ResponseBody>

    @POST("v2/blog/{blog-identifier}/post/delete")
    fun deletePost(
        @Header("Authorization") authorization: String,
        @Path("blog-identifier") blogIdentifier: String,
        @Body requestBody: TumblrDeletePostParams
    ): Call<ResponseBody>
}