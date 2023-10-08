package com.uploadity.api.twitter.tools

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import com.uploadity.BuildConfig
import okio.ByteString.Companion.decodeHex
import java.net.URLEncoder
import java.time.Instant
import java.util.Base64

class TwitterApiTools {

    private val callbackUrlKeyParamName = "oauth_callback"
    private val consumerKeyParamName = "oauth_consumer_key"
    private val nonceParamName = "oauth_nonce"
    private val signatureParamName = "oauth_signature"
    private val signatureMethodParamName = "oauth_signature_method"
    private val timestampParamName = "oauth_timestamp"
    private val versionParamName = "oauth_version"

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateRequestTokenAuthorizationHeader(encodedCallbackUrl: String): String {
        val requestUrl = "https://api.twitter.com/oauth/request_token"
        val parameterMap = generateSignatureParameterMap()
        parameterMap[callbackUrlKeyParamName] = encodedCallbackUrl

        val signature = generateSignature("POST", requestUrl, "",
            parameterMap)

        parameterMap[signatureParamName] = URLEncoder.encode(signature, "UTF-8")

        val sortedParameterList = parameterMap.toSortedMap().toList()
        val authorizationHeader = StringBuilder()
        authorizationHeader.append("OAuth ")

        for (parameter in sortedParameterList) {
            authorizationHeader.append("${parameter.first}=\"${parameter.second}\"")

            if (parameter != sortedParameterList.last()) {
                authorizationHeader.append(", ")
            }

            Log.d("param authorizationHeader", "${parameter.first} ${parameter.second}")
        }

        return authorizationHeader.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateSignatureParameterMap(): MutableMap<String, String> {
        val parameterMap = mutableMapOf<String, String>()

        parameterMap[consumerKeyParamName] = BuildConfig.TWITTER_CLIENT_ID
        parameterMap[nonceParamName] = generateNonce()
        parameterMap[signatureMethodParamName] = "HMAC-SHA1"
        parameterMap[timestampParamName] = Instant.now().epochSecond.toString()
        parameterMap[versionParamName] = "1.0"

        return parameterMap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateSignature(httpMethod: String, requestUrl: String, accessTokenSecret: String,
                                  parameterMap: Map<String, String>): String {
        val consumerSecret = BuildConfig.TWITTER_CLIENT_SECRET
        val signatureBuilder = StringBuilder()
        val signatureBaseString = StringBuilder()
        val sortedParameterList = parameterMap.toSortedMap().toList()
        val signatureSigningKey = "$consumerSecret&$accessTokenSecret"

        signatureBuilder.append("$httpMethod&")
        signatureBuilder.append(URLEncoder.encode(requestUrl, "UTF-8"))
        signatureBuilder.append("&")

        for (parameter in sortedParameterList) {
            signatureBaseString.append("${parameter.first}=${parameter.second}")

            if (parameter != sortedParameterList.last()) {
                signatureBaseString.append("&")
            }
        }

        signatureBuilder.append(URLEncoder.encode(signatureBaseString.toString(), "UTF-8"))

        val hmacUtils = HmacUtils(HmacAlgorithms.HMAC_SHA_1, signatureSigningKey).hmacHex(signatureBuilder.toString())
        val signature = Base64.getEncoder().encodeToString(hmacUtils.decodeHex().toByteArray())

        Log.d("consumer secret", consumerSecret)
        Log.d("signature base string", signatureBaseString.toString())
        Log.d("signature before", signatureBuilder.toString())
        Log.d("signature after", signature)
        Log.d("signing key", signatureSigningKey)

        return signature
    }

    private fun generateNonce() : String {
        val alphanumeric = ('A'..'Z') + ('a'..'z')

        return buildString {
            repeat(11) {
                append(alphanumeric.random())
            }
        }
    }
}