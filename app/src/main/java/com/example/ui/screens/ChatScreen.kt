package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ChatMessage
import com.example.ui.JarvisScreen
import com.example.ui.MainViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { viewModel.navigateTo(JarvisScreen.HOME) }

    val messages by viewModel.messages.collectAsState()
    val inputQuery by viewModel.inputQuery.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val status by viewModel.assistantStatus.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val isListening by viewModel.speechManager.isListening.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val speechError by viewModel.speechManager.speechError.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to latest message when messages change or generation starts
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleListening()
        } else {
            Toast.makeText(context, "Microphone access is required for voice input.", Toast.LENGTH_SHORT).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "chat_mic_pulse")
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse_anim"
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
        // Top Bar with Clear Chat Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                JarvisTopBar(
                    title = "NEURAL TERMINAL",
                    subtitle = "GEMINI SYNAPSE LINK",
                    status = status,
                    language = language,
                    showBackButton = true,
                    onBackClick = { viewModel.navigateTo(JarvisScreen.HOME) },
                    showSettingsButton = false
                )
            }

            IconButton(
                onClick = { showClearDialog = true },
                modifier = Modifier
                    .padding(end = 12.dp)
                    .testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Conversation",
                    tint = JarvisTextSecondary
                )
            }
        }

        // Active Voice / TTS Floating Control Strip
        AnimatedVisibility(visible = isListening || isSpeaking || speechError != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            speechError != null -> JarvisLaserRed.copy(alpha = 0.15f)
                            isListening -> JarvisNeonMint.copy(alpha = 0.15f)
                            else -> JarvisHoloViolet.copy(alpha = 0.15f)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            speechError != null -> JarvisLaserRed
                            isListening -> JarvisNeonMint
                            else -> JarvisHoloViolet
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        speechError != null -> speechError ?: ""
                        isListening -> "LISTENING… Speak clearly in ${language.displayName}"
                        else -> "JARVIS VOCAL SYNTHESIZER ACTIVE"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = when {
                            speechError != null -> JarvisLaserRed
                            isListening -> JarvisNeonMint
                            else -> JarvisHoloViolet
                        },
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (isSpeaking) {
                    IconButton(
                        onClick = { viewModel.stopSpeaking() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Speech",
                            tint = JarvisLaserRed
                        )
                    }
                }
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    isSpeaking = isSpeaking,
                    onSpeak = { viewModel.speakMessage(message.content) },
                    onStop = { viewModel.stopSpeaking() },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Message", message.content))
                        Toast.makeText(context, "Transmitted to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (isGenerating) {
                item {
                    GeneratingBubble()
                }
            }
        }

        // Bottom Command Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisSurface.copy(alpha = 0.95f))
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Microphone Button
            IconButton(
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
                    .size(46.dp)
                    .scale(micPulse)
                    .clip(CircleShape)
                    .background(if (isListening) JarvisNeonMint.copy(alpha = 0.2f) else JarvisSurfaceElevated)
                    .border(1.dp, if (isListening) JarvisNeonMint else JarvisCardBorder, CircleShape)
                    .testTag("chat_mic_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = if (isListening) JarvisNeonMint else JarvisCyan
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input Field
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { viewModel.updateInputQuery(it) },
                placeholder = {
                    Text(
                        text = "Transmit directive in ${language.displayName}…",
                        style = MaterialTheme.typography.bodyMedium.copy(color = JarvisTextMuted)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisCardBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    cursorColor = JarvisCyan,
                    focusedContainerColor = JarvisBackground.copy(alpha = 0.7f),
                    unfocusedContainerColor = JarvisBackground.copy(alpha = 0.7f)
                ),
                singleLine = false,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        viewModel.sendMessage()
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = { viewModel.sendMessage() },
                enabled = inputQuery.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputQuery.isNotBlank() && !isGenerating) JarvisCyan.copy(alpha = 0.2f)
                        else JarvisSurfaceElevated.copy(alpha = 0.4f)
                    )
                    .border(
                        1.dp,
                        if (inputQuery.isNotBlank() && !isGenerating) JarvisCyan
                        else JarvisCardBorder,
                        CircleShape
                    )
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputQuery.isNotBlank() && !isGenerating) JarvisCyan else JarvisTextMuted
                )
            }
        }
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "PURGE CONVERSATION LOGS",
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
                        showClearDialog = false
                    }
                ) {
                    Text("CONFIRM PURGE", color = JarvisLaserRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("CANCEL", color = JarvisCyan)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit,
    onCopy: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val bubbleBorder = when {
        message.isError -> JarvisLaserRed
        message.isUser -> JarvisElectricBlue.copy(alpha = 0.5f)
        else -> JarvisCardBorderActive.copy(alpha = 0.45f)
    }

    val bubbleBackground = when {
        message.isError -> JarvisLaserRed.copy(alpha = 0.12f)
        message.isUser -> Color(0xFF0F1E36)
        else -> JarvisSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = alignment
    ) {
        // Identity tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp, end = 4.dp)
        ) {
            Text(
                text = if (message.isUser) "OPERATOR" else if (message.isError) "SYSTEM ANOMALY" else "JARVIS AI",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = when {
                        message.isError -> JarvisLaserRed
                        message.isUser -> JarvisCyan
                        else -> JarvisNeonMint
                    },
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall.copy(color = JarvisTextMuted)
            )
        }

        // Bubble Content
        Box(
            modifier = Modifier
                .clip(bubbleShape)
                .background(bubbleBackground)
                .border(1.dp, bubbleBorder, bubbleShape)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (message.isError) JarvisLaserRed else JarvisTextPrimary,
                        lineHeight = 22.sp
                    )
                )

                // Actions for AI responses: Speak, Stop, Copy
                if (!message.isUser && !message.isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Message",
                                tint = JarvisTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                if (isSpeaking) onStop() else onSpeak()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isSpeaking) "Stop Reading" else "Speak Aloud",
                                tint = if (isSpeaking) JarvisLaserRed else JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratingBubble() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "JARVIS AI // SYNTHESIZING",
            style = MaterialTheme.typography.labelSmall.copy(
                color = JarvisCyan,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(JarvisSurface)
                .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = JarvisCyan
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Processing synaptic pathways…",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = JarvisTextSecondary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            )
        }
    }
}
