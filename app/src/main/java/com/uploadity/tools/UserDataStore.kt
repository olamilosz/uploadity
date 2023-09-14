package com.uploadity.tools

import android.content.Context
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
    }
}