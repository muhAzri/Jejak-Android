package com.muhazri.jejak.features.session.domain.repositories

import com.muhazri.jejak.features.session.domain.entities.LocationSample
import kotlinx.coroutines.flow.Flow

interface LocationTrackingService {

    /**
     * Starts precise location updates (continuing while the screen is off) and emits every fix
     * until the flow's collector stops listening or [stop] is called.
     */
    fun start(): Flow<LocationSample>

    fun stop()
}
