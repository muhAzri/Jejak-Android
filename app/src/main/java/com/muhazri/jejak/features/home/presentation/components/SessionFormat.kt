package com.muhazri.jejak.features.home.presentation.components

import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** Display formatting for session metrics, following the active locale ("5,24" in Indonesian). */
object SessionFormat {

    fun distance(meters: Double, unit: DistanceUnit): String =
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(meters / unit.metersPerUnit)

    /** "28:41", or "1:02:05" past an hour. */
    fun duration(seconds: Double): String {
        val total = seconds.roundToLong()
        val hours = total / 3600
        val minutes = (total % 3600) / 60
        val secs = total % 60
        return if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.ROOT, "%d:%02d", minutes, secs)
        }
    }

    /** Minutes per unit, e.g. "5:28". [secondsPerMeter] is null when no distance was covered. */
    fun pace(secondsPerMeter: Double?, unit: DistanceUnit): String {
        if (secondsPerMeter == null) return "–:––"
        val total = (secondsPerMeter * unit.metersPerUnit).roundToInt()
        return String.format(Locale.ROOT, "%d:%02d", total / 60, total % 60)
    }

    /** True when [millis] falls on today's date. */
    fun isToday(millis: Long): Boolean = isSameDay(millis, System.currentTimeMillis())

    /** True when [millis] falls on yesterday's date. */
    fun isYesterday(millis: Long): Boolean {
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return isSameDay(millis, yesterday.timeInMillis)
    }

    private fun isSameDay(a: Long, b: Long): Boolean {
        val first = Calendar.getInstance().apply { time = Date(a) }
        val second = Calendar.getInstance().apply { time = Date(b) }
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }
}
