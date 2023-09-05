package com.uploadity.api.linkedin

import com.uploadity.api.linkedin.datamodels.UserInfoResponseModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface LinkedinApiInterface {
    @GET("v2/userinfo")
    fun getUserInfo(
        @Header("Authorization") authorization: String
    ): Call<UserInfoResponseModel>
}