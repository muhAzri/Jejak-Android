package com.muhazri.jejak.session

import com.muhazri.jejak.features.session.domain.entities.RoutePace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RoutePaceTest {

    @Test
    fun `recent pace uses the last thirty seconds`() {
        // 5 s per step for a while, then 3 s per step.
        val route = pacedRoute(List(20) { 5.0 } + List(12) { 3.0 })
        val pace = RoutePace.recent(route)

        assertNotNull(pace)
        assertEquals(3 / metersPerStep, pace!!, 0.01)
    }

    @Test
    fun `recent pace is null when standing still`() {
        assertNull(RoutePace.recent(pacedRoute(listOf(0.0, 30.0))))
    }

    @Test
    fun `extremes find the fastest and slowest stretch`() {
        val route = pacedRoute(List(40) { 6.0 } + List(40) { 3.0 })
        val extremes = RoutePace.extremes(route)

        assertNotNull(extremes)
        assertEquals(3 / metersPerStep, extremes!!.fastest, 0.01)
        assertEquals(6 / metersPerStep, extremes.slowest, 0.01)
    }

    @Test
    fun `no extremes for short routes`() {
        assertNull(RoutePace.extremes(pacedRoute(List(5) { 4.0 })))
    }
}
