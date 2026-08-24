package com.dungeoncrawler.wearos.domain.repository

import kotlinx.coroutines.flow.Flow

/** Backed by Health Services' PassiveMonitoringClient — steps and heart rate collected passively. */
interface HealthRepository {
    fun observeStepDelta(): Flow<Long>
    fun observeHeartRate(): Flow<Double>
    suspend fun startPassiveMonitoring()
    suspend fun stopPassiveMonitoring()
}
