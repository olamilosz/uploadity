package com.uploadity.api.linkedin.datamodels

import com.google.gson.annotations.SerializedName

data class InitializeUploadResponseModel(
    @SerializedName("value") val value: String
)