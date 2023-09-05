package com.uploadity.api.linkedin

import com.uploadity.BuildConfig
import com.uploadity.api.linkedin.datamodels.AccessTokenResponseModel
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LinkedinAccessTokenApiInterface {
    companion object {
        val LINKEDIN_CLIENT_ID = BuildConfig.LINKEDIN_CLIENT_ID
        val LINKEDIN_CLIENT_SECRET = BuildConfig.LINKEDIN_CLIENT_SECRET
    }

    @POST("oauth/v2/accessToken")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun getAccessToken(
        @Body requestBody: RequestBody
    ): Call<AccessTokenResponseModel>
}