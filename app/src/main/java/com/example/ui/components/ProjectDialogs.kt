package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DefaultProjects
import com.example.data.local.StarterProject
import com.example.data.model.ProjectEntity
import com.example.ui.theme.*

@Composable
fun NewProjectDialog(
    isGeneratingWithAi: Boolean,
    onSelectStarter: (StarterProject) -> Unit,
    onGenerateWithAi: (prompt: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Templates, 1 = AI Prompt
    var aiPromptInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Create New Project", color = Slate100, fontWeight = FontWeight.Bold)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = CyanNeon,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Starter Templates", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Generate with AI", fontSize = 12.sp)
                            }
                        }
                    )
                }
            }
        },
        text = {
            if (selectedTab == 0) {
                // Templates list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DefaultProjects.starterProjects) { starter ->
                        Surface(
                            color = Slate850,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectStarter(starter)
                                    onDismiss()
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = starter.name,
                                    color = CyanNeon,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = starter.description,
                                    color = Slate300,
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    starter.files.keys.forEach { fileName ->
                                        Surface(
                                            color = Slate900,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = fileName,
                                                color = Slate400,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // AI Generator prompt
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        "Describe what web app or game you want Free Code AI to create:",
                        fontSize = 13.sp,
                        color = Slate300
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = aiPromptInput,
                        onValueChange = { aiPromptInput = it },
                        placeholder = {
                            Text(
                                "e.g., A neon cyber stopwatch with lap tracking and sound alerts, or an interactive quiz game about space...",
                                color = Slate600,
                                fontSize = 12.sp
                            )
                        },
                        minLines = 4,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    if (isGeneratingWithAi) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyanNeon, strokeWidth = 2.dp)
                            Text("Generating HTML, CSS, and JS files...", color = CyanNeon, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (aiPromptInput.isNotBlank()) {
                                    onGenerateWithAi(aiPromptInput.trim())
                                }
                            },
                            enabled = aiPromptInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate950),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Generate Project", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate400)
            }
        },
        containerColor = Slate900
    )
}

@Composable
fun ProjectManagerDialog(
    projects: List<ProjectEntity>,
    activeProjectId: Long,
    onSelectProject: (ProjectEntity) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit,
    onRenameProject: (Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    var projectToRename by remember { mutableStateOf<ProjectEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Switch or Manage Projects", color = Slate100, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(projects, key = { it.id }) { proj ->
                    val isCurrent = proj.id == activeProjectId
                    Surface(
                        color = if (isCurrent) Slate800 else Slate850,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectProject(proj)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (isCurrent) CyanNeon else Slate400,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = proj.name,
                                        color = if (isCurrent) CyanNeon else Slate100,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (isCurrent) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            color = CyanNeon.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "ACTIVE",
                                                color = CyanNeon,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                if (proj.description.isNotBlank()) {
                                    Text(
                                        text = proj.description,
                                        color = Slate400,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    projectToRename = proj
                                    renameInput = proj.name
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Slate400, modifier = Modifier.size(16.dp))
                            }

                            if (projects.size > 1) {
                                IconButton(
                                    onClick = { onDeleteProject(proj) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseNeon, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Slate300)
            }
        },
        containerColor = Slate900
    )

    // Rename Dialog
    projectToRename?.let { target ->
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            title = { Text("Rename Project", color = Slate100) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = renameInput.trim()
                        if (name.isNotEmpty()) {
                            onRenameProject(target.id, name)
                            projectToRename = null
                        }
                    }
                ) {
                    Text("Save", color = CyanNeon)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}

@Composable
fun ApiKeySettingsDialog(
    currentApiKey: String,
    onSaveApiKey: (String) -> Unit,
    selectedModel: String,
    onSelectModel: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, contentDescription = null, tint = CyanNeon)
                Spacer(Modifier.width(8.dp))
                Text("AI Engine Settings", color = Slate100, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldNeon, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("100% Free Core Features", color = EmeraldNeon, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "HTML/CSS/JS editing, live preview, starter templates, developer console, and ZIP export are completely free and work offline.",
                            color = Slate300,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text("AI Model Selection:", color = Slate200, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedModel == "gemini-3.5-flash",
                        onClick = { onSelectModel("gemini-3.5-flash") },
                        label = { Text("Gemini 3.5 Flash (Fast)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate800,
                            selectedLabelColor = CyanNeon
                        )
                    )

                    FilterChip(
                        selected = selectedModel == "gemini-3.1-pro-preview",
                        onClick = { onSelectModel("gemini-3.1-pro-preview") },
                        label = { Text("Gemini 3.1 Pro (Deep)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate800,
                            selectedLabelColor = CyanNeon
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text("Custom Gemini API Key (Optional):", color = Slate200, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Leave blank to use pre-configured environment credentials.", color = Slate400, fontSize = 10.sp)
                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text("AIzaSy...", color = Slate600, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSaveApiKey(apiKeyInput.trim())
                    onDismiss()
                }
            ) {
                Text("Save", color = CyanNeon)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate400)
            }
        },
        containerColor = Slate900
    )
}
