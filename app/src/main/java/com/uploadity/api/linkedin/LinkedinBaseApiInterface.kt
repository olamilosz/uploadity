package com.uploadity.api.linkedin

import com.uploadity.api.linkedin.datamodels.AccessTokenResponseModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface LinkedinBaseApiInterface {

    @POST("oauth/v2/accessToken")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun getAccessToken(
        @Body requestBody: RequestBody
    ): Call<AccessTokenResponseModel>

    @Multipart
    @PUT("{uploadImageUrl}")
    fun uploadImage(
        @Header("Authorization") authorization: String,
        @Path(value = "uploadImageUrl") uploadImageUrl: String,
        @Part image: MultipartBody.Part
    ): Call <ResponseBody>
}