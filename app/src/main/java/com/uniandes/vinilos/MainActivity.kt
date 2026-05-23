package com.uniandes.vinilos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.uniandes.vinilos.ui.navigation.AppNavigation
import com.uniandes.vinilos.ui.theme.VinilosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            !appViewModel.isReady.value
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val isDarkTheme by appViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val userRole by appViewModel.userRole.collectAsStateWithLifecycle()
            val colorBlindMode by appViewModel.colorBlindMode.collectAsStateWithLifecycle()

            VinilosTheme(darkTheme = isDarkTheme, colorBlindMode = colorBlindMode) {
                AppNavigation(
                    appViewModel = appViewModel,
                    userRole = userRole
                )
            }
        }
    }
}
