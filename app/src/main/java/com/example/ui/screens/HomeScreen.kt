package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AssistantStatus
import com.example.data.model.SupportedLanguage
import com.example.ui.JarvisScreen
import com.example.ui.MainViewModel
import com.example.ui.components.JarvisCard
import com.example.ui.components.JarvisOrb
import com.example.ui.components.JarvisTopBar
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCardBorderActive
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisHoloViolet
import com.example.ui.theme.JarvisLaserRed
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningAmber

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status by viewModel.assistantStatus.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val isListening by viewModel.speechManager.isListening.collectAsState()
    val rmsLevel by viewModel.speechManager.rmsLevel.collectAsState()
    val speechError by viewModel.speechManager.speechError.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val ttsError by viewModel.ttsManager.errorMessage.collectAsState()

    val batteryInfo by viewModel.batteryMonitor.batteryInfo.collectAsState()
    val isTorchOn by viewModel.flashlightManager.isTorchOn.collectAsState()
    val isTorchAvailable by viewModel.flashlightManager.isAvailable.collectAsState()
    val torchError by viewModel.flashlightManager.errorMessage.collectAsState()
    val notes by viewModel.notesList.collectAsState()

    var permissionWarning by remember { mutableStateOf<String?>(null) }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionWarning = null
            viewModel.toggleListening()
        } else {
            permissionWarning = "Microphone access is required for voice commands."
        }
    }

    // Camera permission launcher for torch
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.flashlightManager.toggleFlashlight()
        } else {
            permissionWarning = "Camera permission needed to toggle flashlight."
        }
    }

    val scrollState = rememberScrollState()

    val statusColor by animateColorAsState(
        targetValue = when (status) {
            AssistantStatus.IDLE -> JarvisCyan
            AssistantStatus.LISTENING -> JarvisNeonMint
            AssistantStatus.THINKING -> JarvisCyan
            AssistantStatus.SPEAKING -> JarvisHoloViolet
            AssistantStatus.ERROR -> JarvisLaserRed
        },
        animationSpec = tween(350),
        label = "home_status_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF04070D),
                        JarvisBackground,
                        Color(0xFF070E1A)
                    )
                )
            )
    ) {
        JarvisTopBar(
            title = "JARVIS AI",
            subtitle = "ARC-7 QUANTUM NEURAL CORE",
            status = status,
            language = language,
            showBackButton = false,
            onSettingsClick = { viewModel.navigateTo(JarvisScreen.SETTINGS) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Central Animated AI Orb
            JarvisOrb(
                status = status,
                rmsLevel = rmsLevel,
                size = 180.dp,
                onClick = {
                    if (isSpeaking) {
                        viewModel.stopSpeaking()
                    } else {
                        val hasMic = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasMic) {
                            viewModel.toggleListening()
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Banner HUD
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when (status) {
                        AssistantStatus.IDLE -> "SYSTEM ONLINE // AWAITING COMMAND"
                        AssistantStatus.LISTENING -> "AUDIO RECEPTORS ENGAGED // LISTENING…"
                        AssistantStatus.THINKING -> "NEURAL SYNAPSE GENERATING RESPONSE…"
                        AssistantStatus.SPEAKING -> "VOCAL SYNTHESIZER ACTIVE // SPEAKING"
                        AssistantStatus.ERROR -> "SYSTEM ALERT // CHECK LOGS"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Error or Permission Warnings if present
            AnimatedVisibility(visible = permissionWarning != null || speechError != null || ttsError != null) {
                val warnText = permissionWarning ?: speechError ?: ttsError ?: ""
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, JarvisLaserRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .background(JarvisLaserRed.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = warnText,
                        style = MaterialTheme.typography.bodySmall.copy(color = JarvisLaserRed),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Control Bar: Microphone + Chat Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Listening Toggle Button
                Button(
                    onClick = {
                        val hasMic = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasMic) {
                            viewModel.toggleListening()
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .scale(micScale)
                        .testTag("home_mic_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) JarvisNeonMint else JarvisSurfaceElevated,
                        contentColor = if (isListening) Color(0xFF002920) else JarvisCyan
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isListening) JarvisNeonMint else JarvisCardBorderActive
                    )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening) "STOP" else "VOICE COMMAND",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Chat Screen Navigation Button
                FilledTonalButton(
                    onClick = { viewModel.navigateTo(JarvisScreen.CHAT) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("home_chat_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = JarvisCyan.copy(alpha = 0.15f),
                        contentColor = JarvisCyan
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Open Chat"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TERMINAL",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // TTS Quick Speak / Stop Bar if active
            AnimatedVisibility(visible = isSpeaking) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { viewModel.stopSpeaking() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisLaserRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisLaserRed)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop Speech")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SILENCE VOCALS", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Telemetry & Hardware Section
            Text(
                text = "SUBSYSTEM TELEMETRY",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = JarvisTextMuted,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            // Flashlight Subsystem Card
            JarvisCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_flashlight"),
                borderColor = if (isTorchOn) JarvisWarningAmber else JarvisCardBorder,
                onClick = {
                    val hasCam = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasCam) {
                        viewModel.flashlightManager.toggleFlashlight()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isTorchOn) JarvisWarningAmber.copy(alpha = 0.2f)
                                    else JarvisSurfaceElevated
                                )
                                .border(
                                    1.dp,
                                    if (isTorchOn) JarvisWarningAmber else JarvisCardBorder,
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flashlight",
                                tint = if (isTorchOn) JarvisWarningAmber else JarvisTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "PHOTON EMITTER",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = JarvisTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = when {
                                    !isTorchAvailable -> torchError ?: "Flash unavailable"
                                    isTorchOn -> "EMITTER ACTIVE // TORCH ON"
                                    else -> "STANDBY // TAP TO TOGGLE"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isTorchOn) JarvisWarningAmber else JarvisTextMuted
                                )
                            )
                        }
                    }

                    // On/Off State Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isTorchOn) JarvisWarningAmber.copy(alpha = 0.25f)
                                else Color(0xFF1E293B)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isTorchOn) "ACTIVE" else "OFF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isTorchOn) JarvisWarningAmber else JarvisTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Battery Telemetry Card
            JarvisCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_battery"),
                borderColor = if (batteryInfo.isCharging) JarvisNeonMint else JarvisCardBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (batteryInfo.isCharging) JarvisNeonMint.copy(alpha = 0.2f)
                                    else JarvisSurfaceElevated
                                )
                                .border(
                                    1.dp,
                                    if (batteryInfo.isCharging) JarvisNeonMint else JarvisCardBorder,
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Battery Status",
                                tint = if (batteryInfo.isCharging) JarvisNeonMint else JarvisCyan
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "CORE POWER MATRIX",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = JarvisTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (batteryInfo.isAvailable) {
                                    "${batteryInfo.statusText} // ${batteryInfo.powerSource}"
                                } else {
                                    "Battery telemetry syncing…"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (batteryInfo.isCharging) JarvisNeonMint else JarvisTextMuted
                                )
                            )
                        }
                    }

                    // Battery Percentage Gauge
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (batteryInfo.level >= 0) "${batteryInfo.level}%" else "--%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = when {
                                    batteryInfo.isCharging -> JarvisNeonMint
                                    batteryInfo.level <= 20 -> JarvisLaserRed
                                    else -> JarvisCyan
                                },
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Memory Bank (Local Notes) Card
            JarvisCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_notes"),
                onClick = { viewModel.navigateTo(JarvisScreen.NOTES) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(JarvisSurfaceElevated)
                                .border(1.dp, JarvisCardBorder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Local Memory Notes",
                                tint = JarvisHoloViolet
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "MEMORY BANK",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = JarvisTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${notes.size} ENCRYPTED LOCAL ENTRIES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = JarvisTextMuted
                                )
                            )
                        }
                    }

                    // Open Arrow / Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisHoloViolet.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "VIEW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = JarvisHoloViolet,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Language Fast Switcher Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisSurfaceElevated.copy(alpha = 0.6f))
                    .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SupportedLanguage.entries.forEach { lang ->
                    val isSelected = lang == language
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) JarvisCyan.copy(alpha = 0.25f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) JarvisCyan else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 8.dp)
                            .testTag("lang_toggle_${lang.code}")
                            .clickable { viewModel.setLanguage(lang) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lang.nativeName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) JarvisCyan else JarvisTextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
