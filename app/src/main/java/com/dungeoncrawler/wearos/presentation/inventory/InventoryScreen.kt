package com.dungeoncrawler.wearos.presentation.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.dungeoncrawler.wearos.core.theme.HealthGreen
import com.dungeoncrawler.wearos.core.theme.OledBlack
import com.dungeoncrawler.wearos.core.theme.TextSecondary
import com.dungeoncrawler.wearos.domain.model.EquipmentItem
import com.dungeoncrawler.wearos.domain.model.ItemPassive
import com.dungeoncrawler.wearos.domain.model.StatBlock
import com.dungeoncrawler.wearos.presentation.components.rememberDrawableId
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults

private val StatDown = Color(0xFFE05252)

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack),
        state = listState,
        autoCentering = ScalingLazyColumnDefaults.responsive().autoCentering,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = "Inventaire",
                style = MaterialTheme.typography.title3,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        state.power?.let { power ->
            item {
                Text(
                    text = "ATQ ${power.total.attack} · DEF ${power.total.defense} · " +
                        "CRIT ${power.total.critRate}% · PV ${power.maxHp}",
                    style = MaterialTheme.typography.caption3,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }

        items(state.items, key = { it.id }) { item ->
            InventoryRow(
                item = item,
                isEquipped = state.isEquipped(item),
                isSelected = state.selectedItem?.id == item.id,
                comparison = state.comparison.takeIf { state.selectedItem?.id == item.id },
                onClick = {
                    if (state.selectedItem?.id == item.id) {
                        viewModel.processIntent(InventoryIntent.EquipSelected(item))
                    } else {
                        viewModel.processIntent(InventoryIntent.SelectItem(item))
                    }
                },
            )
        }
    }
}

@Composable
private fun InventoryRow(
    item: EquipmentItem,
    isEquipped: Boolean,
    isSelected: Boolean,
    comparison: StatBlock?,
    onClick: () -> Unit,
) {
    val rarityColor = Color(item.rarity.colorArgb)

    Chip(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .then(
                if (isSelected) {
                    Modifier.border(1.dp, rarityColor, RoundedCornerShape(26.dp))
                } else {
                    Modifier
                },
            ),
        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF0D0D0D)),
        icon = {
            Image(
                painter = painterResource(id = rememberDrawableId(item.iconRes)),
                contentDescription = item.slot.displayName,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )
        },
        label = {
            Text(
                text = item.name,
                color = rarityColor,
                style = MaterialTheme.typography.button,
                maxLines = 1,
            )
        },
        secondaryLabel = {
            Column {
                Text(
                    text = buildString {
                        append(item.rarity.displayName)
                        if (isEquipped) append(" · équipé")
                    },
                    style = MaterialTheme.typography.caption3,
                    color = if (isEquipped) HealthGreen else TextSecondary,
                    maxLines = 1,
                )
                // The comparator only opens on the tapped row: first tap inspects, second equips.
                if (isSelected) {
                    StatComparison(item = item, comparison = comparison)
                }
            }
        },
    )
}

@Composable
private fun StatComparison(item: EquipmentItem, comparison: StatBlock?) {
    if (item.isConsumable) {
        Text(
            text = "Restaure ${(item.healPercent * 100).toInt()} % des PV",
            style = MaterialTheme.typography.caption3,
            color = HealthGreen,
        )
        return
    }

    val deltas = comparison?.namedDeltas().orEmpty()

    if (deltas.isEmpty()) {
        Text(
            text = "Aucun changement de stats",
            style = MaterialTheme.typography.caption3,
            color = TextSecondary,
        )
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            deltas.forEach { (label, delta) ->
                Text(
                    text = "$label ${if (delta > 0) "+" else ""}$delta",
                    style = MaterialTheme.typography.caption3,
                    color = if (delta > 0) HealthGreen else StatDown,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }

    if (item.passive != ItemPassive.NONE) {
        Text(
            text = item.passive.displayName,
            style = MaterialTheme.typography.caption3,
            color = Color(item.rarity.colorArgb),
        )
    }
}

/** Only the stats that actually move, so the row stays readable on a 1.4" screen. */
private fun StatBlock.namedDeltas(): List<Pair<String, Int>> = listOf(
    "PV" to maxHp,
    "ATQ" to attack,
    "DEF" to defense,
    "CRIT" to critRate,
    "MAG" to magicPower,
    "RED" to damageReduction,
).filter { it.second != 0 }
