package com.uploadity.api.linkedin

import com.uploadity.api.linkedin.datamodels.UserInfoResponseModel
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

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
}