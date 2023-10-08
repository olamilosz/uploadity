package com.uploadity.api.tumblr.datamodels

import com.google.gson.annotations.SerializedName

data class TumblrAccessTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("scope") val scope: String,
    @SerializedName("token_type") val tokenType: String
)