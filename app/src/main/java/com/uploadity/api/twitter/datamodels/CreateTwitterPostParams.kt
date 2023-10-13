package com.uploadity.api.twitter.datamodels

import com.google.gson.annotations.SerializedName

data class CreateTwitterPostParams(
    @SerializedName("text") val text: String,
    @SerializedName("media") val media: MediaObject
)

data class MediaObject(
    @SerializedName("media_ids") val mediaIds: Array<String>
)
