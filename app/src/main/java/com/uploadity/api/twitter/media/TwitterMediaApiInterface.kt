package com.uploadity.api.twitter.media

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface TwitterMediaApiInterface {

    @FormUrlEncoded
    @POST("1.1/media/upload.json?media_category=tweet_image")
    //@Headers("Content-Type: image/jpeg")
    fun uploadMedia(
        @Header("Authorization") authorizationHeader: String,
        //@Query("media_category") mediaCategory: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>
}