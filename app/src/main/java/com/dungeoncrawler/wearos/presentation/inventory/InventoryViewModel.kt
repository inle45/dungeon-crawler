package com.dungeoncrawler.wearos.presentation.inventory

import androidx.lifecycle.viewModelScope
import com.dungeoncrawler.wearos.core.haptics.HapticFeedbackManager
import com.dungeoncrawler.wearos.core.haptics.HapticPattern
import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.repository.InventoryRepository
import com.dungeoncrawler.wearos.domain.usecase.ComputeHeroPowerUseCase
import com.dungeoncrawler.wearos.domain.usecase.EquipItemUseCase
import com.dungeoncrawler.wearos.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class InventoryViewModel @Inject constructor(
    inventoryRepository: InventoryRepository,
    computeHeroPower: ComputeHeroPowerUseCase,
    private val equipItem: EquipItemUseCase,
    private val hapticFeedbackManager: HapticFeedbackManager,
) : MviViewModel<InventoryState, InventoryIntent, InventoryEffect>(InventoryState()) {

    init {
        viewModelScope.launch {
            combine(
                inventoryRepository.observeInventory(),
                computeHeroPower(),
            ) { items, power -> items to power }
                .collectLatest { (items, power) ->
                    setState {
                        copy(
                            // Strongest first, so an upgrade is the first thing under the crown.
                            items = items.sortedWith(
                                compareByDescending<EquipmentItem> { it.rarity.ordinal }
                                    .thenByDescending { it.powerScore },
                            ),
                            power = power,
                        )
                    }
                }
        }
    }

    override suspend fun handleIntent(intent: InventoryIntent) {
        when (intent) {
            is InventoryIntent.SelectItem -> setState { copy(selectedItem = intent.item) }

            is InventoryIntent.EquipSelected -> {
                val power = equipItem(intent.item)
                setState { copy(selectedItem = null) }

                if (intent.item.isConsumable) {
                    hapticFeedbackManager.play(HapticPattern.MICRO_EVENT_LOOT)
                    sendEffect(InventoryEffect.Consumed(intent.item, power.currentHp))
                } else {
                    hapticFeedbackManager.play(HapticPattern.PARRY_SUCCESS)
                    sendEffect(InventoryEffect.Equipped(intent.item))
                }
            }
        }
    }
}
