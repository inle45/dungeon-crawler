package com.dungeoncrawler.wearos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.dungeoncrawler.wearos.core.theme.DungeonCrawlerTheme
import com.dungeoncrawler.wearos.presentation.combat.BossCombatScreen
import com.dungeoncrawler.wearos.presentation.dungeon.DungeonClearScreen
import com.dungeoncrawler.wearos.presentation.dungeon.DungeonNavigationScreen
import com.dungeoncrawler.wearos.presentation.inventory.InventoryScreen

@Composable
fun DungeonCrawlerApp() {
    DungeonCrawlerTheme {
        val navController = rememberSwipeDismissableNavController()

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.DUNGEON,
        ) {
            composable(Routes.DUNGEON) {
                DungeonNavigationScreen(
                    onNavigateToBossCombat = { navController.navigate(Routes.BOSS_COMBAT) },
                    onNavigateToInventory = { navController.navigate(Routes.INVENTORY) },
                    onNavigateToDungeonClear = { navController.navigate(Routes.DUNGEON_CLEAR) },
                )
            }

            composable(Routes.BOSS_COMBAT) {
                BossCombatScreen(
                    // A cleared floor drops back to the dungeon view; a cleared dungeon replaces
                    // the fight on the stack so swiping back can't re-enter a dead boss.
                    onFloorCleared = { navController.popBackStack() },
                    onDungeonCleared = {
                        navController.navigate(Routes.DUNGEON_CLEAR) {
                            popUpTo(Routes.DUNGEON) { inclusive = false }
                        }
                    },
                    onDefeated = { navController.popBackStack() },
                )
            }

            composable(Routes.DUNGEON_CLEAR) {
                DungeonClearScreen(
                    onChoiceApplied = {
                        navController.navigate(Routes.DUNGEON) {
                            popUpTo(Routes.DUNGEON) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.INVENTORY) {
                InventoryScreen()
            }
        }
    }
}
