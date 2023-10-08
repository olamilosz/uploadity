package com.uploadity.api.tumblr.datamodels

import com.google.gson.annotations.SerializedName

data class TumblrAccessTokenParams(
    @SerializedName("grant_type") val grantType: String,
    @SerializedName("code") val code: String,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("redirect_uri") val redirectUri: String
)
