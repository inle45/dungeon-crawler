package com.dungeoncrawler.wearos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.dungeoncrawler.wearos.core.theme.DungeonCrawlerTheme
import com.dungeoncrawler.wearos.presentation.combat.BossCombatScreen
import com.dungeoncrawler.wearos.presentation.home.HomeScreen

@Composable
fun DungeonCrawlerApp() {
    DungeonCrawlerTheme {
        val navController = rememberSwipeDismissableNavController()

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME,
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToBossCombat = { navController.navigate(Routes.BOSS_COMBAT) },
                )
            }

            composable(Routes.BOSS_COMBAT) {
                BossCombatScreen(
                    onCombatResolved = { navController.popBackStack() },
                )
            }
        }
    }
}
