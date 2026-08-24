package com.dungeoncrawler.wearos.data.health

import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Registered with [androidx.health.services.client.PassiveMonitoringClient] so step and heart
 * rate updates keep arriving while the app is backgrounded or the watch face is showing.
 */
@AndroidEntryPoint
class StepTrackingService : PassiveListenerService() {

    @Inject
    lateinit var healthRepository: HealthRepositoryImpl

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        dataPoints.getData(DataType.STEPS).forEach { point ->
            healthRepository.emitStepDelta(point.value)
        }
        dataPoints.getData(DataType.HEART_RATE_BPM).forEach { point ->
            healthRepository.emitHeartRate(point.value)
        }
    }
}
