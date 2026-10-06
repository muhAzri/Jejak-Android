package com.muhazri.jejak.features.session.domain.entities

/** GPS quality as shown in the session's top chip. */
enum class GPSSignal {
    /** No usable fix yet. */
    Searching,
    Good,

    /** Fixes are imprecise or have stopped arriving; the route is estimated and current pace hidden. */
    Weak,
    ;

    companion object {
        /** Accuracy (meters) at or below which a fix counts as good. */
        const val GOOD_ACCURACY = 20.0

        fun of(accuracy: Double): GPSSignal =
            if (accuracy in 0.0..GOOD_ACCURACY) Good else Weak
    }
}
