package com.muhazri.jejak.session

import com.muhazri.jejak.features.session.domain.entities.RouteElevation
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteElevationTest {

    @Test
    fun `ignores wobble below the threshold`() {
        val route = altitudeRoute(listOf(10.0, 12.0, 10.0, 12.0, 10.0, 12.0))
        assertEquals(0.0, RouteElevation.gain(route), 0.0)
    }

    @Test
    fun `counts climbs and skips descents`() {
        val route = altitudeRoute(listOf(10.0, 14.0, 20.0, 15.0, 18.0, 25.0, null, 25.0))
        assertEquals(20.0, RouteElevation.gain(route), 0.0)
    }

    @Test
    fun `does not climb across a pause`() {
        val route = altitudeRoute(listOf(10.0, 10.0, 40.0, 40.0), segments = listOf(0, 0, 1, 1))
        assertEquals(0.0, RouteElevation.gain(route), 0.0)
    }
}
