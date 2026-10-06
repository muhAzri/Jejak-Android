package com.muhazri.jejak.previews

import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.domain.entities.RoutePoint
import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import java.util.Calendar
import kotlin.math.sin

/** A 5.24 km run along a gently curving line, so the previews show a believable route. */
object SampleData {

    val route: List<RoutePoint> = run {
        val start = System.currentTimeMillis() - 1_062_000
        (0..300).map { step ->
            RoutePoint(
                latitude = -6.2 + step * 0.00009,
                longitude = 106.82 + sin(step / 40.0) * 0.002,
                timestamp = start + (step * 3_500L),
                segment = 0,
            )
        }
    }

    val session: SessionSummary = SessionSummary(
        activity = ActivityType.Run,
        startDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 12)
            set(Calendar.SECOND, 0)
        }.timeInMillis,
        distanceMeters = 5_240.0,
        durationSeconds = 28 * 60.0 + 41,
        route = route,
    )
}
