package com.nvv.mediadata.data.connecttv

import android.net.Uri
import android.util.Patterns
import fi.iki.elonen.NanoHTTPD
import timber.log.Timber


class TvHttpPlayServer(
	port: Int,
	private val callback: TvPlayRequestCallback,
) : NanoHTTPD(port) {

	companion object {
		const val DEFAULT_PORT: Int = 8_899
		private const val MIME_JSON = "application/json"
	}

	override fun serve(session: IHTTPSession): Response {
		return try {
			if (session.method != Method.POST || session.uri != "/play") {
				newFixedLengthResponse(
					Response.Status.NOT_FOUND,
					MIME_PLAINTEXT,
					"Not found",
				)
			} else {
				handlePlayRequest(session)
			}
		} catch (t: Throwable) {
			Timber.e(t, "TvHttpPlayServer: unexpected error")
			newFixedLengthResponse(
				Response.Status.INTERNAL_ERROR,
				MIME_PLAINTEXT,
				"Internal server error",
			)
		}
	}

	private fun handlePlayRequest(session: IHTTPSession): Response {
		val bodyMap = mutableMapOf<String, String>()
		return try {
			session.parseBody(bodyMap)
			val rawBody = bodyMap["postData"].orEmpty()
			val url = extractUrlFromJson(rawBody)

			if (url.isNullOrBlank()) {
				return newFixedLengthResponse(
					Response.Status.BAD_REQUEST,
					MIME_PLAINTEXT,
					"Missing url",
				)
			}

			if (!isValidHttpUrl(url)) {
				return newFixedLengthResponse(
					Response.Status.BAD_REQUEST,
					MIME_PLAINTEXT,
					"Invalid url",
				)
			}

			Timber.i("TvHttpPlayServer: received play request for URL=%s", url)
			callback.onPlayRequested(url)

			newFixedLengthResponse(
				Response.Status.OK,
				MIME_JSON,
				"""{"status":"ok"}""",
			)
		} catch (e: Exception) {
			Timber.w(e, "TvHttpPlayServer: failed to handle /play request")
			newFixedLengthResponse(
				Response.Status.BAD_REQUEST,
				MIME_PLAINTEXT,
				"Bad request",
			)
		}
	}

	private fun extractUrlFromJson(json: String): String? {
		val keyIndex = json.indexOf("\"url\"")
		if (keyIndex == -1) return null

		val colonIndex = json.indexOf(':', startIndex = keyIndex)
		if (colonIndex == -1) return null

		val firstQuote = json.indexOf('"', startIndex = colonIndex + 1)
		if (firstQuote == -1) return null

		val secondQuote = json.indexOf('"', startIndex = firstQuote + 1)
		if (secondQuote == -1 || secondQuote <= firstQuote) return null

		return json.substring(firstQuote + 1, secondQuote).trim()
	}

	private fun isValidHttpUrl(url: String): Boolean {
		if (!Patterns.WEB_URL.matcher(url).matches()) return false
		val parsed = runCatching { Uri.parse(url) }.getOrNull() ?: return false
		val scheme = parsed.scheme?.lowercase()
		return scheme == "http" || scheme == "https"
	}
}

