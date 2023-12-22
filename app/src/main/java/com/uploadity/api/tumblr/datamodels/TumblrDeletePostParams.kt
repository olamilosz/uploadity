package com.uploadity.api.tumblr.datamodels

import com.google.gson.annotations.SerializedName

data class TumblrDeletePostParams(
    @SerializedName("id") val id: String
)
