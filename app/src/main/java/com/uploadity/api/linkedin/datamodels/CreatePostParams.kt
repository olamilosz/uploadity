package com.uploadity.api.linkedin.datamodels

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class CreatePostParams(
    @SerializedName("author") val author: String,
    @SerializedName("commentary") val commentary: String,
    @SerializedName("visibility") val visibility: String,
    @SerializedName("distribution") val distribution: Distribution,
    @SerializedName("content") val content: Content,
    @SerializedName("lifecycleState") val lifecycleState: String,
    @SerializedName("isReshareDisabledByAuthor") val isReshareDisabledByAuthor: Boolean
)

data class Distribution(
    @SerializedName("feedDistribution") val feedDistribution: String,
    @SerializedName("targetEntities") val targetEntities: Array<String>,
    @SerializedName("thirdPartyDistributionChannels") val thirdPartyDistributionChannels: Array<String>
)

data class Content(
    @SerializedName("media") val media: Media
)

data class Media(
    @SerializedName("title") val title: String,
    @SerializedName("id") val id: String
)