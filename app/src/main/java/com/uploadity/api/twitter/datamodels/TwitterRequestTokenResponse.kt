package com.uploadity.api.twitter.datamodels

import com.google.gson.annotations.SerializedName

data class TwitterRequestTokenResponse(
    @SerializedName("oauth_token") val oauthToken: String,
    @SerializedName("oauth_token_secret") val oauthTokenSecret: String,
    @SerializedName("oauth_callback_confirmed") val oauthCallbackConfirmed: String
)