package com.uploadity.api.linkedin

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LinkedinBaseApiServiceBuilder {
    private val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

    private val gson = GsonBuilder().setLenient().create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.linkedin.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    fun<T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }
}