package com.uploadity.api.linkedin

import com.uploadity.api.linkedin.datamodels.LinkedinAccessTokenParams
import com.uploadity.api.linkedin.datamodels.LinkedinAccessTokenResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.Headers
import retrofit2.http.POST

interface LinkedinApiInterface {
    @POST("oauth/v2/accessToken")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun getAccessToken(
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @POST("oauth/v2/accessToken")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun getAccessToken2(
        @Body requestBody: LinkedinAccessTokenParams
    ): Call<LinkedinAccessTokenResponse>
}