package com.muhazri.jejak.features.home.domain.usecases

import com.muhazri.jejak.features.home.domain.entities.SessionSummary
import com.muhazri.jejak.features.home.domain.repositories.SessionRepository
import javax.inject.Inject

class GetLastSession @Inject constructor(private val repository: SessionRepository) {
    suspend operator fun invoke(): SessionSummary? = repository.latest()
}

class GetSession @Inject constructor(private val repository: SessionRepository) {
    suspend operator fun invoke(id: String): SessionSummary? = repository.find(id)
}

class DeleteSession @Inject constructor(private val repository: SessionRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
