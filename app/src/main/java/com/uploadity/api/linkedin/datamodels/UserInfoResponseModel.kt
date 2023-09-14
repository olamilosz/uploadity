package com.uploadity.api.linkedin.datamodels

import com.google.gson.annotations.SerializedName

data class UserInfoResponseModel(
    @SerializedName("sub") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("given_name") val givenName: String,
    @SerializedName("family_name") val familyName: String,
    @SerializedName("picture") val picture: String,
    @SerializedName("email") val email: String,
    @SerializedName("email_verified") val emailVerified: Boolean
)