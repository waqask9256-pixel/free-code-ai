package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectFileEntity
import com.example.ui.theme.*

@Composable
fun CodeEditorView(
    activeFile: ProjectFileEntity?,
    fileContent: String,
    onContentChange: (String) -> Unit,
    hasUnsavedChanges: Boolean,
    onSaveFile: () -> Unit,
    onAiAction: (action: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(activeFile?.id) {
        mutableStateOf(TextFieldValue(fileContent))
    }

    // Keep textFieldValue in sync if external change happened (e.g. AI applied code)
    LaunchedEffect(fileContent) {
        if (textFieldValue.text != fileContent) {
            textFieldValue = TextFieldValue(fileContent)
        }
    }

    // History for undo/redo
    var undoStack by remember(activeFile?.id) { mutableStateOf(listOf<String>()) }
    var redoStack by remember(activeFile?.id) { mutableStateOf(listOf<String>()) }

    var isWordWrapEnabled by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAiMenu by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val lines = remember(textFieldValue.text) {
        textFieldValue.text.split("\n")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Editor Header
        Surface(
            color = Slate900,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Active file indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val badgeColor = when (activeFile?.fileType) {
                        "HTML" -> SyntaxTag
                        "CSS" -> SyntaxFunction
                        "JAVASCRIPT" -> SyntaxAttr
                        else -> EmeraldNeon
                    }

                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activeFile?.fileType ?: "CODE",
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = activeFile?.name ?: "No file open",
                        color = Slate100,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    if (hasUnsavedChanges) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CyanNeon, RoundedCornerShape(4.dp))
                        )
                    }
                }

                // Header tools: AI Assist, Search, Word wrap, Undo, Redo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // AI Quick Assistant Button
                    Box {
                        FilledTonalButton(
                            onClick = { showAiMenu = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CyanNeon.copy(alpha = 0.15f),
                                contentColor = CyanNeon
                            )
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Actions", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("AI Assist", fontSize = 12.sp)
                        }

                        DropdownMenu(
                            expanded = showAiMenu,
                            onDismissRequest = { showAiMenu = false },
                            modifier = Modifier.background(Slate900)
                        ) {
                            DropdownMenuItem(
                                text = { Text("💡 Explain Code", color = Slate100) },
                                leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AmberNeon) },
                                onClick = {
                                    showAiMenu = false
                                    onAiAction("Explain this code step-by-step")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🐛 Find Bugs & Debug", color = Slate100) },
                                leadingIcon = { Icon(Icons.Default.BugReport, contentDescription = null, tint = RoseNeon) },
                                onClick = {
                                    showAiMenu = false
                                    onAiAction("Find potential bugs, missing tags, or errors in this code and provide the fixed version")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("⚡ Optimize & Improve", color = Slate100) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = EmeraldNeon) },
                                onClick = {
                                    showAiMenu = false
                                    onAiAction("Optimize and modernize this code with best practices")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("✨ Format & Beautify", color = Slate100) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, contentDescription = null, tint = CyanNeon) },
                                onClick = {
                                    showAiMenu = false
                                    onAiAction("Cleanly format and indent this code")
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = { isSearchVisible = !isSearchVisible },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchVisible) CyanNeon else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isWordWrapEnabled = !isWordWrapEnabled },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.WrapText,
                            contentDescription = "Toggle Word Wrap",
                            tint = if (isWordWrapEnabled) CyanNeon else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(textFieldValue.text))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Search Bar (if visible)
        if (isSearchVisible) {
            Surface(
                color = Slate850,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search in file...", fontSize = 13.sp, color = Slate400) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate200,
                            focusedIndicatorColor = CyanNeon,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { isSearchVisible = false; searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Close search", tint = Slate400)
                    }
                }
            }
        }

        // Main Code Editor Area (Gutter + Text Field)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Slate950)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
            ) {
                // Line Number Gutter
                Column(
                    modifier = Modifier
                        .background(Slate900)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .widthIn(min = 36.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lines.size) {
                        Text(
                            text = "$i",
                            color = Slate600,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Editor Content
                val contentModifier = if (isWordWrapEnabled) {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScroll)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                }

                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        if (newValue.text != textFieldValue.text) {
                            undoStack = undoStack + textFieldValue.text
                            redoStack = emptyList()
                            onContentChange(newValue.text)
                        }
                        textFieldValue = newValue
                    },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = Slate100,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(CyanNeon),
                    modifier = contentModifier
                )
            }
        }

        // Quick Code Snippets Toolbar (for fast coding on mobile)
        Surface(
            color = Slate900,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            val snippets = when (activeFile?.fileType) {
                "HTML" -> listOf("<div>", "</div>", "class=\"\"", "id=\"\"", "<button>", "<input>", "<span>", "<p>", "<section>", "<h1>", "<script>", "<style>")
                "CSS" -> listOf("display: flex;", "display: grid;", "justify-content: center;", "align-items: center;", "background: ", "color: ", "padding: ", "border-radius: ", "gap: ", "margin: ", ":hover", "@media")
                "JAVASCRIPT" -> listOf("const ", "let ", "function ", "=>", "() => {}", "console.log()", "document.getElementById('')", "addEventListener('', () => {})", "if ()", "return ", "async ", "await ")
                else -> listOf("{", "}", "[", "]", "(", ")", "\"", "'", "<", ">", "=", ";", ":", "/", "+", "-")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (snip in snippets) {
                    ElevatedButton(
                        onClick = {
                            val currentText = textFieldValue.text
                            val selection = textFieldValue.selection
                            val newText = currentText.substring(0, selection.start) + snip + currentText.substring(selection.end)
                            val newCursor = selection.start + snip.length
                            undoStack = undoStack + currentText
                            onContentChange(newText)
                            textFieldValue = TextFieldValue(
                                text = newText,
                                selection = androidx.compose.ui.text.TextRange(newCursor)
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Slate800,
                            contentColor = CyanNeon
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(snip, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
