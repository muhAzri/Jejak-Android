package com.muhazri.jejak.core.network.interceptors

import com.muhazri.jejak.core.network.ApiException
import com.muhazri.jejak.core.network.models.ApiErrorDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Converts error responses into [ApiException] so the error envelope is parsed in a
 * single place instead of inside every Retrofit service call.
 */
@Singleton
class ErrorInterceptor @Inject constructor(
    private val json: Json,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) return response

        throw ApiException(
            code = response.code,
            message = response.readErrorMessage() ?: response.message,
        )
    }

    private fun Response.readErrorMessage(): String? = runCatching {
        val payload = peekBody(MAX_ERROR_BODY_BYTES).string()
        val error = json.decodeFromString<ApiErrorDto>(payload)
        error.message ?: error.error
    }.getOrNull()?.takeIf { it.isNotBlank() }

    private companion object {
        const val MAX_ERROR_BODY_BYTES = 1L * 1024 * 1024
    }
}
