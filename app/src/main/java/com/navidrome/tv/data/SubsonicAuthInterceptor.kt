package com.navidrome.tv.data

import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest
import kotlin.random.Random

private fun randomSalt(): String =
    (1..12).map { "abcdefghijklmnopqrstuvwxyz0123456789"[Random.nextInt(36)] }.joinToString("")

private fun md5(input: String): String {
    val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

class SubsonicAuthInterceptor(
    private val credentialsProvider: () -> Credentials?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val creds = credentialsProvider() ?: return chain.proceed(chain.request())
        val salt = randomSalt()
        val token = md5(creds.password + salt)
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("u", creds.username)
            .addQueryParameter("t", token)
            .addQueryParameter("s", salt)
            .addQueryParameter("v", "1.16.1")
            .addQueryParameter("c", "NavidromeTV")
            .addQueryParameter("f", "json")
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}

fun buildStreamUrl(creds: Credentials, id: String, maxBitRate: Int = 0): String {
    val base = normalizeBaseUrl(creds.baseUrl)
    val salt = randomSalt()
    val token = md5(creds.password + salt)
    val bitRate = if (maxBitRate > 0) "&maxBitRate=$maxBitRate" else ""
    return "${base}rest/stream.view?id=$id&u=${creds.username}&t=$token&s=$salt&v=1.16.1&c=NavidromeTV&f=json$bitRate"
}

fun coverArtUrl(creds: Credentials?, coverArtId: String?, size: Int = 600): String? {
    if (creds == null || coverArtId.isNullOrBlank()) return null
    val base = normalizeBaseUrl(creds.baseUrl)
    val salt = randomSalt()
    val token = md5(creds.password + salt)
    return "${base}rest/getCoverArt.view?id=$coverArtId&size=$size&u=${creds.username}&t=$token&s=$salt&v=1.16.1&c=NavidromeTV"
}
