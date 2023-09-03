package com.uploadity.api.linkedin.datamodels

import retrofit2.http.Field

data class LinkedinAccessTokenParams(
    @Field(value = "grant_type", encoded = true)
    val grant_type: String,
    @Field(value = "code", encoded = true)
    val code : String,
    @Field(value = "client_id", encoded = true)
    val client_id : String,
    @Field(value = "client_secret", encoded = true)
    val client_secret : String,
    @Field(value = "redirect_uri", encoded = true)
    val redirect_uri : String
)
