package com.muhazri.jejak.core.network

import java.io.IOException

/**
 * Raised for any non-2xx HTTP response so repositories can translate a single
 * exception type into their own domain failures.
 */
class ApiException(
    val code: Int,
    override val message: String,
) : IOException(message)
