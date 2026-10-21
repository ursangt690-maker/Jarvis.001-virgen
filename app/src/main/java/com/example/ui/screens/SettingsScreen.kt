package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.GeminiModelConstants
import com.example.data.model.SupportedLanguage
import com.example.ui.JarvisScreen
import com.example.ui.MainViewModel
import com.example.ui.components.JarvisCard
import com.example.ui.components.JarvisTopBar
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCardBorderActive
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisHoloViolet
import com.example.ui.theme.JarvisLaserRed
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningAmber

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { viewModel.navigateTo(JarvisScreen.HOME) }

    val status by viewModel.assistantStatus.collectAsState()
    val activeLanguage by viewModel.currentLanguage.collectAsState()
    val configuredApiKey by viewModel.apiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val connectionTestState by viewModel.connectionTestState.collectAsState()

    val speechRate by viewModel.speechRate.collectAsState()
    val speechPitch by viewModel.speechPitch.collectAsState()
    val autoSpeak by viewModel.autoSpeakEnabled.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val availableVoices by viewModel.ttsManager.availableVoices.collectAsState()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsState()

    var keyInput by remember(configuredApiKey) { mutableStateOf(configuredApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var keySavedNotice by remember { mutableStateOf(false) }
    var showClearChatDialog by remember { mutableStateOf(false) }

    // Dynamic Permission tracking
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var camGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
    }

    val camPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        camGranted = granted
    }

    val scrollState = rememberScrollState()

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
            title = "CORE MATRIX",
            subtitle = "SYSTEM ARCHITECTURE & PROTOCOLS",
            status = status,
            language = activeLanguage,
            showBackButton = true,
            onBackClick = { viewModel.navigateTo(JarvisScreen.HOME) },
            showSettingsButton = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // SECTION 1: GEMINI AI BRAIN
            SectionHeader(icon = Icons.Default.Key, title = "GEMINI AI SYNAPSE CONFIGURATION")

            JarvisCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Gemini API Key",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = JarvisTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Secure local key for direct neural access to Gemini API.",
                        style = MaterialTheme.typography.bodySmall.copy(color = JarvisTextMuted)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = {
                            keyInput = it
                            keySavedNotice = false
                        },
                        placeholder = { Text("AIzaSy…", color = JarvisTextMuted) },
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isKeyVisible) "Hide Key" else "Show Key",
                                    tint = JarvisCyan
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_api_key_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.setApiKey(keyInput)
                                keySavedNotice = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_save_key_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JarvisCyan.copy(alpha = 0.25f),
                                contentColor = JarvisCyan
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan)
                        ) {
                            Text("SAVE KEY", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { viewModel.testGeminiConnection() },
                            enabled = !connectionTestState.isLoading && keyInput.isNotBlank(),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("settings_test_conn_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisNeonMint),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisNeonMint)
                        ) {
                            if (connectionTestState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = JarvisNeonMint
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PINGING…", style = MaterialTheme.typography.labelSmall)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TEST LINK", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    if (keySavedNotice) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Key saved successfully to encrypted local app preferences.",
                            style = MaterialTheme.typography.labelSmall.copy(color = JarvisNeonMint)
                        )
                    }

                    // Connection Test Results Display
                    AnimatedVisibility(visible = connectionTestState.successMessage != null || connectionTestState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val isSuccess = connectionTestState.successMessage != null
                        val displayMsg = connectionTestState.successMessage ?: connectionTestState.errorMessage ?: ""

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSuccess) JarvisNeonMint.copy(alpha = 0.12f)
                                    else JarvisLaserRed.copy(alpha = 0.12f)
                                )
                                .border(
                                    1.dp,
                                    if (isSuccess) JarvisNeonMint else JarvisLaserRed,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isSuccess) JarvisNeonMint else JarvisLaserRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = displayMsg,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSuccess) JarvisNeonMint else JarvisLaserRed
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Model Selector
                    Text(
                        text = "Active Gemini Model",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = JarvisTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GeminiModelConstants.AVAILABLE_MODELS.forEach { modelName ->
                            val isChosen = modelName == selectedModel
                            val shortLabel = when (modelName) {
                                GeminiModelConstants.MODEL_FLASH -> "3.5 Flash"
                                GeminiModelConstants.MODEL_PRO -> "3.1 Pro"
                                GeminiModelConstants.MODEL_LITE -> "Flash Lite"
                                else -> modelName
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) JarvisCyan.copy(alpha = 0.25f) else JarvisSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isChosen) JarvisCyan else JarvisCardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 8.dp)
                                    .clickable { viewModel.setSelectedModel(modelName) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shortLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isChosen) JarvisCyan else JarvisTextMuted,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Security Advisory
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF131C2E))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = JarvisWarningAmber,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Security Advisory: API keys packaged in distributed APKs are subject to client extraction. For production systems, route requests via a backend gateway.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = JarvisTextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 2: MULTILINGUAL MATRIX
            SectionHeader(icon = Icons.Default.Language, title = "LANGUAGE SYNCHRONIZATION")

            JarvisCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Operational Language",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = JarvisTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Configures speech recognition and vocal synthesis localized dialects.",
                        style = MaterialTheme.typography.bodySmall.copy(color = JarvisTextMuted)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SupportedLanguage.entries.forEach { lang ->
                        val isSelected = lang == activeLanguage
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) JarvisCyan.copy(alpha = 0.15f) else JarvisSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) JarvisCyan else JarvisCardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setLanguage(lang) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) JarvisCyan else JarvisTextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "${lang.nativeName} (${lang.localeTag})",
                                    style = MaterialTheme.typography.labelSmall.copy(color = JarvisTextMuted)
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 3: VOCAL SYNTHESIZER (TTS)
            SectionHeader(icon = Icons.Default.RecordVoiceOver, title = "AUDIO SYNTHESIZER // TEXT-TO-SPEECH")

            JarvisCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    // Auto Speak Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Vocalize Responses",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = JarvisTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Automatically speak newly received Gemini responses aloud.",
                                style = MaterialTheme.typography.bodySmall.copy(color = JarvisTextMuted)
                            )
                        }

                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { viewModel.setAutoSpeak(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisCyan,
                                checkedTrackColor = JarvisCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = JarvisTextMuted,
                                uncheckedTrackColor = JarvisSurfaceElevated
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speech Rate Slider
                    Text(
                        text = "Speech Rate: ${String.format("%.2fx", speechRate)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = JarvisTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Slider(
                        value = speechRate,
                        onValueChange = { viewModel.setSpeechRate(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisSurfaceElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Speech Pitch Slider
                    Text(
                        text = "Vocal Pitch: ${String.format("%.2fx", speechPitch)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = JarvisTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Slider(
                        value = speechPitch,
                        onValueChange = { viewModel.setSpeechPitch(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisHoloViolet,
                            activeTrackColor = JarvisHoloViolet,
                            inactiveTrackColor = JarvisSurfaceElevated
                        )
                    )

                    // Voice selection info
                    if (availableVoices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${availableVoices.size} System TTS voices detected on device",
                            style = MaterialTheme.typography.labelSmall.copy(color = JarvisTextMuted)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Voice & Stop Speaking Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testVoiceOutput() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_test_voice_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JarvisHoloViolet.copy(alpha = 0.25f),
                                contentColor = JarvisHoloViolet
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisHoloViolet)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TEST VOICE", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_stop_voice_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisLaserRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisLaserRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("STOP", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 4: HARDWARE PERMISSIONS & TROUBLESHOOTING
            SectionHeader(icon = Icons.Default.Tune, title = "PERMISSIONS & DIAGNOSTICS")

            JarvisCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    // Microphone permission
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Microphone Sensor",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = JarvisTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Required for Speech Recognition",
                                style = MaterialTheme.typography.labelSmall.copy(color = JarvisTextMuted)
                            )
                        }

                        if (micGranted) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisNeonMint.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "GRANTED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = JarvisNeonMint,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan)
                            ) {
                                Text("REQUEST", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Camera permission
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Camera Flashlight Sensor",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = JarvisTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Required for Photon Emitter (Torch)",
                                style = MaterialTheme.typography.labelSmall.copy(color = JarvisTextMuted)
                            )
                        }

                        if (camGranted) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisNeonMint.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "GRANTED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = JarvisNeonMint,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { camPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan)
                            ) {
                                Text("REQUEST", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Troubleshooting Protocols:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = JarvisCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Speech recognition relies on Google Speech Services. Ensure an active network connection or download offline speech packs in Android Settings.\n" +
                               "• If Nepali or Hindi TTS voice does not sound, install the language voice data via Android Settings -> System -> Languages -> Text-to-speech output.\n" +
                               "• Verify that the Gemini API Key is valid and has Gemini API enabled in Google AI Studio.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = JarvisTextMuted,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 5: DATA MANAGEMENT & PURGE
            SectionHeader(icon = Icons.Default.DeleteSweep, title = "DATA RETENTION")

            JarvisCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Purge Conversation Logs",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = JarvisTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Clears all neural chat history from RAM buffer.",
                            style = MaterialTheme.typography.labelSmall.copy(color = JarvisTextMuted)
                        )
                    }

                    OutlinedButton(
                        onClick = { showClearChatDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisLaserRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisLaserRed)
                    ) {
                        Text("PURGE", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 6: ABOUT JARVIS
            SectionHeader(icon = Icons.Default.Info, title = "ABOUT JARVIS AI")

            JarvisCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "JARVIS AI ASSISTANT",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = JarvisCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0.0 // Quantum Build Release\n" +
                               "Engineered in native Android Kotlin with Jetpack Compose.\n" +
                               "Subsystems: Gemini Flash 3.5 AI, Android SpeechRecognizer, Android TextToSpeech, CameraManager Hardware Torch, BatteryManager Telemetry, Room SQLite Storage.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = JarvisTextSecondary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            title = {
                Text(
                    text = "PURGE DIALOGUE LOGS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = JarvisLaserRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Confirm purging the neural dialogue buffer? This action cannot be reversed.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = JarvisTextSecondary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChatHistory()
                        showClearChatDialog = false
                    }
                ) {
                    Text("PURGE", color = JarvisLaserRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text("CANCEL", color = JarvisCyan)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = JarvisCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                color = JarvisCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
    }
}
