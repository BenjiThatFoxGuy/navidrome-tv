package com.navidrome.tv.di

import android.content.Context
import com.navidrome.tv.data.Credentials
import com.navidrome.tv.data.CredentialsStore
import com.navidrome.tv.data.MusicRepository
import com.navidrome.tv.data.SubsonicApi
import com.navidrome.tv.data.SubsonicAuthInterceptor
import com.navidrome.tv.data.normalizeBaseUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceLocator {

    private lateinit var appContext: Context
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var credentialsStore: CredentialsStore
        private set

    val credentials = MutableStateFlow<Credentials?>(null)

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(SubsonicAuthInterceptor { credentials.value })
            .build()
    }

    @Volatile private var api: SubsonicApi? = null

    lateinit var repository: MusicRepository
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
        credentialsStore = CredentialsStore(appContext)
        repository = MusicRepository(
            apiProvider = { api ?: error("API not initialized") },
            credentialsProvider = { credentials.value }
        )
        scope.launch {
            credentialsStore.flow.collect { creds ->
                credentials.value = creds
                if (creds != null) {
                    api = buildApi(creds.baseUrl)
                } else {
                    api = null
                }
            }
        }
    }

    fun buildApi(baseUrl: String): SubsonicApi =
        Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(baseUrl))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SubsonicApi::class.java)

    fun apiOrNull(): SubsonicApi? = api
}
