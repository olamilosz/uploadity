package com.uploadity.api.tumblr.datamodels

import retrofit2.http.Field

data class TumblrAuthorizeParams(
    @Field(value = "client_id", encoded = true)
    val clientId: String,
    @Field(value = "response_type", encoded = true)
    val responseType: String,
    @Field(value = "scope", encoded = true)
    val scope: String,
    @Field(value = "state", encoded = true)
    val state: String,
    @Field(value = "redirect_uri", encoded = true)
    val redirectUri: String,
)