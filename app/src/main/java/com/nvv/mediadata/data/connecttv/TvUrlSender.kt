package com.nvv.mediadata.data.connecttv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.IOException

private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()


suspend fun sendUrlToTv(
	client: OkHttpClient,
	tv: TvDevice,
	url: String,
): Result<Unit> = withContext(Dispatchers.IO) {
	runCatching {
		val safeUrl = url.trim()
		require(safeUrl.isNotEmpty()) { "URL must not be empty" }

		val hostName = tv.host.hostName ?: tv.host.hostAddress
		val targetUrl = "http://$hostName:${tv.port}/play"
		val jsonBody = "{" +
				"\"url\":" + safeUrl.toJsonStringLiteral() +
				"}"

		val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE)
		val request = Request.Builder()
			.url(targetUrl)
			.post(requestBody)
			.build()

		Timber.d("sendUrlToTv: sending URL to %s:%d", hostName, tv.port)

		client.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw IOException("HTTP ${response.code} from TV")
			}
		}
	}
}


private fun String.toJsonStringLiteral(): String {
	val escaped = buildString(length + 2) {
		append('"')
		for (ch in this@toJsonStringLiteral) {
			when (ch) {
				'\\' -> append("\\\\")
				'"' -> append("\\\"")
				'\b' -> append("\\b")
				'\u000C' -> append("\\f")
				'\n' -> append("\\n")
				'\r' -> append("\\r")
				'\t' -> append("\\t")
				else -> append(ch)
			}
		}
		append('"')
	}
	return escaped
}

