package com.muhazri.jejak.core.time

/** The wall clock, behind an interface so sessions can be driven by a fake one in tests. */
fun interface TimeSource {
    fun nowMillis(): Long

    companion object {
        val System = TimeSource { java.lang.System.currentTimeMillis() }
    }
}
