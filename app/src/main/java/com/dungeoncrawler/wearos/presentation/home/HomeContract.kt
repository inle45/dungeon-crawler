package com.dungeoncrawler.wearos.presentation.home

import com.dungeoncrawler.wearos.domain.model.BossEncounter
import com.dungeoncrawler.wearos.domain.model.Equipment
import com.dungeoncrawler.wearos.domain.model.MicroEvent
import com.dungeoncrawler.wearos.domain.model.PlayerStats
import com.dungeoncrawler.wearos.presentation.mvi.MviEffect
import com.dungeoncrawler.wearos.presentation.mvi.MviIntent
import com.dungeoncrawler.wearos.presentation.mvi.MviState

data class HomeState(
    val player: PlayerStats = PlayerStats(),
    val equipment: Equipment = Equipment(),
    val lastMicroEvent: MicroEvent? = null,
) : MviState {
    val stepsIntoMicroEventCycle: Int get() = (player.totalSteps % 200).toInt()
    val stepsIntoBossCycle: Long get() = player.totalSteps % 2000
    val bossProgressRatio: Float get() = stepsIntoBossCycle / 2000f
}

sealed class HomeIntent : MviIntent {
    data object DismissMicroEventBanner : HomeIntent()
}

sealed class HomeEffect : MviEffect {
    data class NavigateToBossCombat(val boss: BossEncounter) : HomeEffect()
}
