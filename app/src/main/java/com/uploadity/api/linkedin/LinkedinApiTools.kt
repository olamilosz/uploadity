package com.uploadity.api.linkedin

import java.lang.StringBuilder

class LinkedinApiTools {

    private val redirectUri = "https://uploadity.net.pl/linkedin"

    fun generateAuthorizationUrl(clientId: String) : String {
        val authorizeUrl = StringBuilder()

        authorizeUrl.append("https://www.linkedin.com/oauth/v2/authorization/")
        authorizeUrl.append("?response_type=code")
        authorizeUrl.append("&client_id=$clientId")
        authorizeUrl.append("&redirect_uri=$redirectUri")
        authorizeUrl.append("&scope=w_member_social%20openid%20profile%20email")

        return authorizeUrl.toString()
    }
}