package com.uploadity.api.tumblr.datamodels

import com.google.gson.annotations.SerializedName

data class TumblrCreatePostParams(
    @SerializedName("type") val type: String,
    @SerializedName("caption") val text: String,
    @SerializedName("data64") val base64EncodedImageString: String,
)