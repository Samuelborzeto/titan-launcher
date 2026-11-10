package com.titanlauncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.titanlauncher.apps.AppRepository
import com.titanlauncher.drawer.AppDrawer
import com.titanlauncher.home.HomeScreen
import com.titanlauncher.settings.SettingsScreen
import com.titanlauncher.settings.SettingsStore
import com.titanlauncher.ui.TitanTheme

private enum class Screen { Home, Drawer, Settings }

class MainActivity : ComponentActivity() {
    private lateinit var repo: AppRepository
    private lateinit var settings: SettingsStore
    private var screen by mutableStateOf(Screen.Home)

    override fun onCreate(savedInstanceState: Bundle?) {
        // O launcher é sempre escuro: ícones claros nas barras do sistema.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        repo = AppRepository(this, lifecycleScope)
        settings = SettingsStore(this)

        setContent {
            TitanTheme {
                val titanEnabled by settings.titanEnabled.collectAsState(initial = true)

                // Na tela inicial o botão voltar não faz nada: o launcher nunca fecha.
                BackHandler(enabled = screen == Screen.Home) {}

                Box(Modifier.fillMaxSize()) {
                    HomeScreen(
                        repo = repo,
                        titanEnabled = titanEnabled && screen == Screen.Home,
                        onOpenDrawer = { screen = Screen.Drawer },
                        onOpenSettings = { screen = Screen.Settings },
                    )
                    AnimatedVisibility(
                        visible = screen == Screen.Drawer,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                    ) {
                        AppDrawer(
                            repo = repo,
                            onClose = { screen = Screen.Home },
                            onOpenSettings = { screen = Screen.Settings },
                        )
                    }
                    AnimatedVisibility(
                        visible = screen == Screen.Settings,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        SettingsScreen(store = settings, onBack = { screen = Screen.Home })
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        repo.start()
    }

    override fun onDestroy() {
        repo.stop()
        super.onDestroy()
    }

    // Botão home com o launcher já aberto: volta para a tela inicial.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        screen = Screen.Home
    }
}
