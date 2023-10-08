package com.uploadity.api.tumblr.tools

import java.lang.StringBuilder

class TumblrApiTools {

    private val redirectUri = "https://uploadity.net.pl/tumblr"

    fun generateAuthorizationUrl(clientId: String): String {
        val authorizeUrl = StringBuilder()

        authorizeUrl.append("https://www.tumblr.com/oauth2/authorize/")
        authorizeUrl.append("?response_type=code")
        authorizeUrl.append("&client_id=$clientId")
        authorizeUrl.append("&redirect_uri=$redirectUri")
        authorizeUrl.append("&scope=basic write")
        authorizeUrl.append("&state=${generateState()}")

        return authorizeUrl.toString()
    }

    private fun generateState(): String {
        val alphanumeric = ('A'..'Z') + ('a'..'z')

        return buildString {
            repeat(11) {
                append(alphanumeric.random())
            }
        }
    }
}