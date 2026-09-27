package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class MainTab {
    EDITOR,
    PREVIEW,
    AI_CHAT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val projectFiles by viewModel.projectFiles.collectAsStateWithLifecycle()
    val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
    val activeFileContent by viewModel.activeFileContent.collectAsStateWithLifecycle()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val consoleLogs by viewModel.consoleLogs.collectAsStateWithLifecycle()
    val reloadTrigger by viewModel.reloadTrigger.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val isGeneratingProject by viewModel.isGeneratingProject.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(MainTab.EDITOR) }
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showProjectManagerDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showConsoleSheet by remember { mutableStateOf(false) }
    var showTopMenu by remember { mutableStateOf(false) }

    // Listen for snackbar notifications
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg, withDismissAction = true)
        }
    }

    // Handle back button when drawer or tab is open
    BackHandler(enabled = drawerState.isOpen || activeTab != MainTab.EDITOR) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            activeTab = MainTab.EDITOR
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Slate900,
                modifier = Modifier.width(300.dp)
            ) {
                FileExplorerDrawer(
                    currentProject = activeProject,
                    files = projectFiles,
                    activeFileId = activeFile?.id ?: 0L,
                    onSelectFile = { file ->
                        viewModel.selectFile(file)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onAddNewFile = { name ->
                        viewModel.addNewFile(name)
                    },
                    onDeleteFile = { file ->
                        viewModel.deleteFile(file)
                    },
                    onOpenProjectManager = {
                        coroutineScope.launch { drawerState.close() }
                        showProjectManagerDialog = true
                    },
                    onDownloadZip = {
                        viewModel.exportProjectZip(context)
                    },
                    onShareFile = { file ->
                        viewModel.shareSingleFile(context, file)
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = CyanNeon.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "{ / }",
                                    color = CyanNeon,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Free Code AI",
                                    color = Slate100,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeProject?.name ?: "No project",
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Files Menu", tint = Slate200)
                        }
                    },
                    actions = {
                        // New Project Button
                        IconButton(onClick = { showNewProjectDialog = true }) {
                            Icon(Icons.Default.AddBox, contentDescription = "New Project", tint = CyanNeon)
                        }

                        // Save Button
                        IconButton(
                            onClick = { viewModel.saveActiveFile() },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (hasUnsavedChanges) AmberNeon else Slate400
                            )
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }

                        // Download ZIP Button
                        IconButton(onClick = { viewModel.exportProjectZip(context) }) {
                            Icon(Icons.Default.Download, contentDescription = "Download Project ZIP", tint = Slate200)
                        }

                        // RUN Button (Highlighted Emerald Play action)
                        Button(
                            onClick = {
                                viewModel.runCode()
                                activeTab = MainTab.PREVIEW
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldNeon,
                                contentColor = Slate950
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .padding(end = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("RUN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Overflow Menu
                        Box {
                            IconButton(onClick = { showTopMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Slate200)
                            }

                            DropdownMenu(
                                expanded = showTopMenu,
                                onDismissRequest = { showTopMenu = false },
                                modifier = Modifier.background(Slate900)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Switch Project", color = Slate100) },
                                    leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, tint = CyanNeon) },
                                    onClick = {
                                        showTopMenu = false
                                        showProjectManagerDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("AI Engine Settings", color = Slate100) },
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = AmberNeon) },
                                    onClick = {
                                        showTopMenu = false
                                        showApiKeyDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Developer Console", color = Slate100) },
                                    leadingIcon = { Icon(Icons.Default.BugReport, contentDescription = null, tint = RoseNeon) },
                                    onClick = {
                                        showTopMenu = false
                                        showConsoleSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export Project ZIP", color = Slate100) },
                                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = EmeraldNeon) },
                                    onClick = {
                                        showTopMenu = false
                                        viewModel.exportProjectZip(context)
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Slate900,
                        titleContentColor = Slate100
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Slate900,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == MainTab.EDITOR,
                        onClick = { activeTab = MainTab.EDITOR },
                        icon = { Icon(Icons.Default.Code, contentDescription = "Code Editor") },
                        label = { Text("Editor", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanNeon,
                            selectedTextColor = CyanNeon,
                            indicatorColor = Slate800,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        )
                    )

                    NavigationBarItem(
                        selected = activeTab == MainTab.PREVIEW,
                        onClick = { activeTab = MainTab.PREVIEW },
                        icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Live Preview") },
                        label = { Text("Live Preview", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldNeon,
                            selectedTextColor = EmeraldNeon,
                            indicatorColor = Slate800,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        )
                    )

                    NavigationBarItem(
                        selected = activeTab == MainTab.AI_CHAT,
                        onClick = { activeTab = MainTab.AI_CHAT },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (isAiLoading) {
                                        Badge(containerColor = CyanNeon)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Coding Chat")
                            }
                        },
                        label = { Text("AI Assistant", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanNeon,
                            selectedTextColor = CyanNeon,
                            indicatorColor = Slate800,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        )
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Slate950
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isWideScreen = maxWidth >= 760.dp

                if (isWideScreen) {
                    // Responsive Split View for Tablets / Desktop
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left Pane: Editor or AI Chat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            if (activeTab == MainTab.AI_CHAT) {
                                AiChatSheet(
                                    messages = chatMessages,
                                    isAiLoading = isAiLoading,
                                    onSendMessage = { prompt -> viewModel.sendAiChatMessage(prompt) },
                                    onApplyCodeToEditor = { code -> viewModel.applyCodeToActiveFile(code) },
                                    onClearChat = { viewModel.clearChat() },
                                    onOpenApiKeyDialog = { showApiKeyDialog = true }
                                )
                            } else {
                                CodeEditorView(
                                    activeFile = activeFile,
                                    fileContent = activeFileContent,
                                    onContentChange = { newContent -> viewModel.onContentChange(newContent) },
                                    hasUnsavedChanges = hasUnsavedChanges,
                                    onSaveFile = { viewModel.saveActiveFile() },
                                    onAiAction = { action ->
                                        viewModel.sendAiChatMessage(action)
                                        activeTab = MainTab.AI_CHAT
                                    }
                                )
                            }
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Slate800)
                        )

                        // Right Pane: Live Preview
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            LivePreviewView(
                                files = projectFiles,
                                reloadTrigger = reloadTrigger,
                                consoleLogs = consoleLogs,
                                onAddLog = { type, msg -> viewModel.addConsoleLog(type, msg) },
                                onOpenConsole = { showConsoleSheet = true }
                            )
                        }
                    }
                } else {
                    // Mobile Screen: Tabbed Switching
                    when (activeTab) {
                        MainTab.EDITOR -> {
                            CodeEditorView(
                                activeFile = activeFile,
                                fileContent = activeFileContent,
                                onContentChange = { newContent -> viewModel.onContentChange(newContent) },
                                hasUnsavedChanges = hasUnsavedChanges,
                                onSaveFile = { viewModel.saveActiveFile() },
                                onAiAction = { action ->
                                    viewModel.sendAiChatMessage(action)
                                    activeTab = MainTab.AI_CHAT
                                }
                            )
                        }
                        MainTab.PREVIEW -> {
                            LivePreviewView(
                                files = projectFiles,
                                reloadTrigger = reloadTrigger,
                                consoleLogs = consoleLogs,
                                onAddLog = { type, msg -> viewModel.addConsoleLog(type, msg) },
                                onOpenConsole = { showConsoleSheet = true }
                            )
                        }
                        MainTab.AI_CHAT -> {
                            AiChatSheet(
                                messages = chatMessages,
                                isAiLoading = isAiLoading,
                                onSendMessage = { prompt -> viewModel.sendAiChatMessage(prompt) },
                                onApplyCodeToEditor = { code ->
                                    viewModel.applyCodeToActiveFile(code)
                                    activeTab = MainTab.EDITOR
                                },
                                onClearChat = { viewModel.clearChat() },
                                onOpenApiKeyDialog = { showApiKeyDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    // Developer Console Bottom Sheet
    if (showConsoleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConsoleSheet = false },
            containerColor = Slate950
        ) {
            DevConsoleSheet(
                logs = consoleLogs,
                onClearLogs = { viewModel.clearConsoleLogs() },
                onClose = { showConsoleSheet = false }
            )
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            isGeneratingWithAi = isGeneratingProject,
            onSelectStarter = { starter ->
                viewModel.createNewProjectFromStarter(starter)
                activeTab = MainTab.EDITOR
            },
            onGenerateWithAi = { prompt ->
                viewModel.generateProjectWithAi(prompt)
                showNewProjectDialog = false
                activeTab = MainTab.EDITOR
            },
            onDismiss = { showNewProjectDialog = false }
        )
    }

    // Project Manager Dialog
    if (showProjectManagerDialog) {
        ProjectManagerDialog(
            projects = allProjects,
            activeProjectId = activeProject?.id ?: 0L,
            onSelectProject = { proj ->
                viewModel.selectProject(proj)
            },
            onDeleteProject = { proj ->
                viewModel.deleteProject(proj)
            },
            onRenameProject = { id, name ->
                viewModel.renameProject(id, name)
            },
            onDismiss = { showProjectManagerDialog = false }
        )
    }

    // API Key & Model Settings Dialog
    if (showApiKeyDialog) {
        ApiKeySettingsDialog(
            currentApiKey = customApiKey,
            onSaveApiKey = { key -> viewModel.setCustomApiKey(key) },
            selectedModel = selectedModel,
            onSelectModel = { model -> viewModel.setSelectedModel(model) },
            onDismiss = { showApiKeyDialog = false }
        )
    }
}
