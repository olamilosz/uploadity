package com.uploadity.tools

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserDataStore(private val context: Context?) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("datastore")
    }

    val linkedinAccessTokenKey = "linkedin_access_token_key"
    val linkedinIdKey = "linkedin_id_key"
    val tumblrAccessTokenKey = "tumblr_access_token_key"
    val twitterAccessTokenKey = "twitter_access_token_key"
    val twitterAccessTokenSecretKey = "twitter_access_token_secret_key"

    suspend fun getStringPreference(key: String): String {
        val stringPreferenceKey = stringPreferencesKey(key)

        return context!!.dataStore.data.map { preferences ->
            preferences[stringPreferenceKey] ?: ""
        }.first()
    }

    suspend fun saveStringPreference(key: String, value: String) {
        val stringPreferenceKey = stringPreferencesKey(key)

        context!!.dataStore.edit { preferences ->
            preferences[stringPreferenceKey] = value
        }
        Log.d("save preference!!!", "key $key val $value")
    }

    suspend fun deleteStringPreference(key: String) {
        val stringPreferenceKey = stringPreferencesKey(key)

        context!!.dataStore.edit { preferences->
            preferences.remove(stringPreferenceKey)
        }
    }

    suspend fun deleteStringPreferenceBySocialMediaName(socialMediaName: String) {
        when (socialMediaName) {
            "linkedin" -> {
                deleteStringPreference(linkedinAccessTokenKey)
                deleteStringPreference(linkedinIdKey)
            }

            "twitter" -> {
                deleteStringPreference(twitterAccessTokenKey)
                deleteStringPreference(twitterAccessTokenSecretKey)
            }

            "tumblr" -> {
                deleteStringPreference(tumblrAccessTokenKey)
            }
        }
    }
}