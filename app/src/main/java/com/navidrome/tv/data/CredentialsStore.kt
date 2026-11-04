package com.navidrome.tv.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("navidrome_settings")

class CredentialsStore(private val context: Context) {

    private val keyBase = stringPreferencesKey("base_url")
    private val keyUser = stringPreferencesKey("username")
    private val keyPass = stringPreferencesKey("password")

    val flow: Flow<Credentials?> = context.dataStore.data.map { p ->
        val base = p[keyBase] ?: return@map null
        val user = p[keyUser] ?: return@map null
        val pass = p[keyPass] ?: return@map null
        Credentials(base, user, pass)
    }

    suspend fun save(creds: Credentials) {
        context.dataStore.edit { p ->
            p[keyBase] = creds.baseUrl
            p[keyUser] = creds.username
            p[keyPass] = creds.password
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
