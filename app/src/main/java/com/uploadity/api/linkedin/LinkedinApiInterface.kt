package com.uploadity.api.linkedin

import com.uploadity.api.linkedin.datamodels.CreatePostParams
import com.uploadity.api.linkedin.datamodels.UserInfoResponseModel
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

interface LinkedinApiInterface {
    @GET("v2/userinfo")
    fun getUserInfo(
        @Header("Authorization") authorization: String
    ): Call<UserInfoResponseModel>

    @POST("rest/images?action=initializeUpload")
    @Headers("LinkedIn-Version: 202308",
        "X-Restli-Protocol-Version: 2.0.0",
        "Content-Type: application/json")
    fun initializeImageUpload(
        @Header("Authorization") authorization: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @POST("rest/posts")
    @Headers("LinkedIn-Version: 202308",
        "X-Restli-Protocol-Version: 2.0.0",
        "Content-Type: application/json")
    fun createPost(
        @Query("oauth2_access_token") accessToken: String,
        @Body requestBody: CreatePostParams
    ): Call<ResponseBody>

    @POST("rest/posts/{shareUrn}")
    @Headers("LinkedIn-Version: 202308",
        "X-RestLi-Method: PARTIAL_UPDATE",
        "Content-Type: application/json")
    fun editPost(
        @Header("Authorization") authorization: String,
        @Path("shareUrn") shareUrn: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @DELETE("/rest/posts/{encodedPostUrn}")
    @Headers("LinkedIn-Version: 202308")
    fun deletePost(
        @Header("Authorization") authorization: String,
        @Path("encodedPostUrn") encodedPostUrn: String
    ): Call<ResponseBody>
}