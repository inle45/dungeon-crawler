package com.dungeoncrawler.wearos.data.health

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import com.dungeoncrawler.wearos.domain.repository.HealthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bridges Health Services' background [StepTrackingService] to the rest of the app: the service
 * pushes step/heart-rate updates into these shared flows, this class exposes them as [Flow]s.
 */
@Singleton
class HealthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : HealthRepository {

    private val passiveMonitoringClient: PassiveMonitoringClient =
        HealthServices.getClient(context).passiveMonitoringClient

    private val _stepDelta = MutableSharedFlow<Long>(extraBufferCapacity = 16)
    private val _heartRate = MutableSharedFlow<Double>(extraBufferCapacity = 16)

    override fun observeStepDelta(): Flow<Long> = _stepDelta.asSharedFlow()
    override fun observeHeartRate(): Flow<Double> = _heartRate.asSharedFlow()

    override suspend fun startPassiveMonitoring() {
        val config = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.STEPS, DataType.HEART_RATE_BPM))
            .build()
        passiveMonitoringClient.setPassiveListenerServiceAsync(
            StepTrackingService::class.java,
            config,
        )
    }

    override suspend fun stopPassiveMonitoring() {
        passiveMonitoringClient.clearPassiveListenerServiceAsync()
    }

    internal fun emitStepDelta(delta: Long) {
        _stepDelta.tryEmit(delta)
    }

    internal fun emitHeartRate(bpm: Double) {
        _heartRate.tryEmit(bpm)
    }
}
