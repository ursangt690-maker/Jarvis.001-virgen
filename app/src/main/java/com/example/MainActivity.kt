package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JarvisScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.JarvisAITheme
import com.example.ui.theme.JarvisBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JarvisAITheme {
                val viewModel: MainViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JarvisBackground)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                (fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) +
                                        slideInHorizontally { width -> width / 3 })
                                    .togetherWith(
                                        fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) +
                                                slideOutHorizontally { width -> -width / 3 }
                                    )
                            },
                            label = "jarvis_screen_transition"
                        ) { screen ->
                            when (screen) {
                                JarvisScreen.HOME -> HomeScreen(viewModel = viewModel)
                                JarvisScreen.CHAT -> ChatScreen(viewModel = viewModel)
                                JarvisScreen.NOTES -> NotesScreen(viewModel = viewModel)
                                JarvisScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
