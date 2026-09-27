package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import com.example.ui.theme.*

@Composable
fun FileExplorerDrawer(
    currentProject: ProjectEntity?,
    files: List<ProjectFileEntity>,
    activeFileId: Long,
    onSelectFile: (ProjectFileEntity) -> Unit,
    onAddNewFile: (String) -> Unit,
    onDeleteFile: (ProjectFileEntity) -> Unit,
    onOpenProjectManager: () -> Unit,
    onDownloadZip: () -> Unit,
    onShareFile: (ProjectFileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }
    var fileToDelete by remember { mutableStateOf<ProjectFileEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Slate900)
            .padding(16.dp)
    ) {
        // Project Header
        Surface(
            color = Slate850,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenProjectManager() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = "Project",
                    tint = CyanNeon,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentProject?.name ?: "No Project",
                        color = Slate100,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Tap to switch or create project",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Files Header & Add File Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FILES (${files.size})",
                color = Slate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = {
                    newFileNameInput = ""
                    showNewFileDialog = true
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add New File",
                    tint = CyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // File List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(files, key = { it.id }) { file ->
                val isActive = file.id == activeFileId
                val icon = when (file.fileType) {
                    "HTML" -> Icons.Default.Html
                    "CSS" -> Icons.Default.Palette
                    "JAVASCRIPT" -> Icons.Default.Javascript
                    else -> Icons.Default.Code
                }
                val iconColor = when (file.fileType) {
                    "HTML" -> SyntaxTag
                    "CSS" -> SyntaxFunction
                    "JAVASCRIPT" -> SyntaxAttr
                    else -> EmeraldNeon
                }

                Surface(
                    color = if (isActive) Slate800 else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectFile(file) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icon,
                            contentDescription = file.fileType,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = file.name,
                            color = if (isActive) CyanNeon else Slate200,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )

                        // File actions
                        IconButton(
                            onClick = { onShareFile(file) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share File",
                                tint = Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (files.size > 1) {
                            IconButton(
                                onClick = { fileToDelete = file },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete File",
                                    tint = RoseNeon.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Download Project as ZIP Button
        Button(
            onClick = onDownloadZip,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldNeon,
                contentColor = Slate950
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Download ZIP Archive", fontWeight = FontWeight.Bold)
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File", color = Slate100) },
            text = {
                Column {
                    Text(
                        "Enter file name with extension (e.g., component.html, style.css, utils.js):",
                        fontSize = 13.sp,
                        color = Slate400
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        placeholder = { Text("newfile.js", color = Slate600) },
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
                        val name = newFileNameInput.trim()
                        if (name.isNotEmpty()) {
                            onAddNewFile(name)
                            showNewFileDialog = false
                        }
                    }
                ) {
                    Text("Create", color = CyanNeon)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }

    // Confirm Delete File Dialog
    fileToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File?", color = Slate100) },
            text = { Text("Are you sure you want to delete '${target.name}'? This action cannot be undone.", color = Slate300) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteFile(target)
                        fileToDelete = null
                    }
                ) {
                    Text("Delete", color = RoseNeon)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}
