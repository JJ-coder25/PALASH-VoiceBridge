package com.palash.voicebridge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.palash.voicebridge.PalashApp
import com.palash.voicebridge.ui.home.HomeScreen
import com.palash.voicebridge.ui.settings.SettingsScreen
import com.palash.voicebridge.ui.voice.VoiceTranslationScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    app: PalashApp
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToVoice = { navController.navigate(Screen.VoiceTranslation.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.VoiceTranslation.route) {
            VoiceTranslationScreen(
                app = app,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                app = app,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
