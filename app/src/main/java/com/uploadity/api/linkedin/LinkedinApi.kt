package com.uploadity.api.linkedin

import com.uploadity.api.linkedin.datamodels.LinkedinAccessTokenParams
import com.uploadity.api.linkedin.datamodels.LinkedinAccessTokenResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class LinkedinApi {

    private val redirectUri = "https://uploadity.net.pl/linkedin"

    fun generateAuthorizationUrl(clientId: String) : String {
        val authorizeUrl = StringBuilder()

        authorizeUrl.append("https://www.linkedin.com/oauth/v2/authorization/")
        authorizeUrl.append("?response_type=code")
        authorizeUrl.append("&client_id=$clientId")
        authorizeUrl.append("&redirect_uri=$redirectUri")
        authorizeUrl.append("&scope=w_member_social")

        return authorizeUrl.toString()
    }
}