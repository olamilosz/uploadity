package com.uploadity.api.linkedin.datamodels

import com.google.gson.annotations.SerializedName

data class AccessTokenResponseModel(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("scope") val scope: String
)
