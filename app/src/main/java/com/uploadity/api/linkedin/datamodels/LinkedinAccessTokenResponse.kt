package com.uploadity.api.linkedin.datamodels

data class LinkedinAccessTokenResponse(
    val access_token: String,
    val expires_in : Int,
    val refresh_token : String,
    val refresh_token_expires_in : Int,
    val scope : String
)