package com.navidrome.tv.data

data class Credentials(
    val baseUrl: String,
    val username: String,
    val password: String
)

fun normalizeBaseUrl(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "http://localhost/"
    val withScheme = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://"))
        "http://$trimmed" else trimmed
    return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
}
