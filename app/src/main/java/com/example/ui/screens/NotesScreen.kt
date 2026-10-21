package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NoteEntity
import com.example.ui.JarvisScreen
import com.example.ui.MainViewModel
import com.example.ui.components.JarvisCard
import com.example.ui.components.JarvisTopBar
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
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
fun NotesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateTo(JarvisScreen.HOME) }

    val notes by viewModel.notesList.collectAsState()
    val searchQuery by viewModel.notesSearchQuery.collectAsState()
    val status by viewModel.assistantStatus.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = JarvisBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    noteToEdit = null
                    showEditDialog = true
                },
                containerColor = JarvisHoloViolet,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_note")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                title = "MEMORY BANK",
                subtitle = "SECURE ENCRYPTED LOCAL STORAGE",
                status = status,
                showBackButton = true,
                onBackClick = { viewModel.navigateTo(JarvisScreen.HOME) },
                showSettingsButton = false
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateNotesSearch(it) },
                placeholder = {
                    Text(
                        "Search memory records…",
                        style = MaterialTheme.typography.bodyMedium.copy(color = JarvisTextMuted)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = JarvisCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateNotesSearch("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = JarvisTextMuted
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("notes_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisCardBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    cursorColor = JarvisCyan,
                    focusedContainerColor = JarvisSurface.copy(alpha = 0.8f),
                    unfocusedContainerColor = JarvisSurface.copy(alpha = 0.8f)
                ),
                singleLine = true
            )

            // Notes Count Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${notes.size} ENTRIES STORED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = JarvisTextMuted,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "DEVICE-LOCAL (OFFLINE)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = JarvisNeonMint,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = JarvisCardBorder,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No records in memory bank." else "No records matching query.",
                            style = MaterialTheme.typography.bodyLarge.copy(color = JarvisTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the '+' button to log a directive or note.",
                            style = MaterialTheme.typography.bodySmall.copy(color = JarvisTextMuted)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteItemCard(
                            note = note,
                            onEdit = {
                                noteToEdit = note
                                showEditDialog = true
                            },
                            onDelete = {
                                noteToDelete = note
                            },
                            onAskJarvis = {
                                viewModel.discussNoteInChat(note)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Note Dialog
    if (showEditDialog) {
        NoteEditDialog(
            existingNote = noteToEdit,
            onDismiss = { showEditDialog = false },
            onSave = { title, content, tag ->
                viewModel.saveNote(
                    id = noteToEdit?.id ?: 0L,
                    title = title,
                    content = content,
                    tag = tag
                )
                showEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = {
                Text(
                    text = "EXPUNGE MEMORY RECORD",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = JarvisLaserRed,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete '${noteToDelete?.title}' from local storage?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = JarvisTextSecondary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteToDelete?.let { viewModel.deleteNote(it) }
                        noteToDelete = null
                    }
                ) {
                    Text("DELETE RECORD", color = JarvisLaserRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("CANCEL", color = JarvisCyan)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun NoteItemCard(
    note: NoteEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAskJarvis: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val formattedDate = remember(note.timestamp) { dateFormat.format(Date(note.timestamp)) }

    JarvisCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("note_item_${note.id}"),
        onClick = onEdit
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = JarvisTextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisHoloViolet.copy(alpha = 0.2f))
                        .border(1.dp, JarvisHoloViolet.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = note.tag.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = JarvisHoloViolet,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium.copy(color = JarvisTextSecondary),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall.copy(color = JarvisTextMuted)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Discuss with JARVIS Action
                    IconButton(
                        onClick = onAskJarvis,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Discuss with JARVIS",
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Edit Action
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Note",
                            tint = JarvisTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete Action
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = JarvisLaserRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteEditDialog(
    existingNote: NoteEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, tag: String) -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var tag by remember { mutableStateOf(existingNote?.tag ?: "GENERAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingNote == null) "NEW MEMORY ENTRY" else "MODIFY RECORD",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = JarvisCyan,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Identifier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisCardBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Memory Content / Directives") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisCardBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tag selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("GENERAL", "LOG", "MISSION", "DIRECTIVE").forEach { t ->
                        val isSelected = tag == t
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) JarvisHoloViolet.copy(alpha = 0.3f) else JarvisSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) JarvisHoloViolet else JarvisCardBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable { tag = t }
                        ) {
                            Text(
                                text = t,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) JarvisHoloViolet else JarvisTextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onSave(title.ifBlank { "Untitled Record" }, content, tag)
                    }
                },
                enabled = title.isNotBlank() || content.isNotBlank()
            ) {
                Text("SAVE RECORD", color = JarvisCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = JarvisTextMuted)
            }
        },
        containerColor = JarvisSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
