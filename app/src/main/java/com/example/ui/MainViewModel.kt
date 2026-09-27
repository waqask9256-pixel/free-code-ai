package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.export.ZipExporter
import com.example.data.local.AppDatabase
import com.example.data.local.ProjectRepository
import com.example.data.local.StarterProject
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import com.example.ui.components.ConsoleLogItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(db.projectDao(), db.chatDao())

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val geminiService = GeminiService { _customApiKey.value }

    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
    val activeProject: StateFlow<ProjectEntity?> = _activeProject.asStateFlow()

    private val _projectFiles = MutableStateFlow<List<ProjectFileEntity>>(emptyList())
    val projectFiles: StateFlow<List<ProjectFileEntity>> = _projectFiles.asStateFlow()

    private val _activeFile = MutableStateFlow<ProjectFileEntity?>(null)
    val activeFile: StateFlow<ProjectFileEntity?> = _activeFile.asStateFlow()

    private val _activeFileContent = MutableStateFlow("")
    val activeFileContent: StateFlow<String> = _activeFileContent.asStateFlow()

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()

    private val _consoleLogs = MutableStateFlow<List<ConsoleLogItem>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLogItem>> = _consoleLogs.asStateFlow()

    private val _reloadTrigger = MutableStateFlow(0)
    val reloadTrigger: StateFlow<Int> = _reloadTrigger.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _isGeneratingProject = MutableStateFlow(false)
    val isGeneratingProject: StateFlow<Boolean> = _isGeneratingProject.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultProjects()

            // Observe projects and select the first project if none is active
            repository.allProjects.collectLatest { projects ->
                if (_activeProject.value == null && projects.isNotEmpty()) {
                    selectProject(projects.first())
                }
            }
        }
    }

    fun selectProject(project: ProjectEntity) {
        _activeProject.value = project
        viewModelScope.launch {
            // Collect project files
            repository.getFilesForProject(project.id).collectLatest { files ->
                _projectFiles.value = files
                val currentActive = _activeFile.value
                val matched = files.find { it.id == currentActive?.id }
                if (matched != null) {
                    _activeFile.value = matched
                } else if (files.isNotEmpty()) {
                    // Default to index.html or first file
                    val defaultFile = files.find { it.name.equals("index.html", ignoreCase = true) } ?: files.first()
                    _activeFile.value = defaultFile
                    _activeFileContent.value = defaultFile.content
                    _hasUnsavedChanges.value = false
                }
            }
        }

        viewModelScope.launch {
            // Collect chat messages
            repository.getChatMessages(project.id).collectLatest { msgs ->
                _chatMessages.value = msgs
            }
        }
    }

    fun selectFile(file: ProjectFileEntity) {
        if (_hasUnsavedChanges.value) {
            saveActiveFile()
        }
        _activeFile.value = file
        _activeFileContent.value = file.content
        _hasUnsavedChanges.value = false
    }

    fun onContentChange(newContent: String) {
        _activeFileContent.value = newContent
        _hasUnsavedChanges.value = (_activeFile.value?.content != newContent)
    }

    fun saveActiveFile() {
        val file = _activeFile.value ?: return
        val currentProjectId = _activeProject.value?.id ?: return
        val updated = file.copy(content = _activeFileContent.value)
        viewModelScope.launch {
            repository.saveFile(currentProjectId, updated)
            _activeFile.value = updated
            _hasUnsavedChanges.value = false
            _snackbarMessage.emit("Saved ${file.name}")
        }
    }

    fun runCode() {
        saveActiveFile()
        _reloadTrigger.value = _reloadTrigger.value + 1
        viewModelScope.launch {
            _snackbarMessage.emit("Preview updated")
        }
    }

    fun createNewProjectFromStarter(starter: StarterProject) {
        viewModelScope.launch {
            val newId = repository.createProjectFromStarter(starter)
            val all = repository.allProjects.first()
            val newProj = all.find { it.id == newId }
            if (newProj != null) {
                selectProject(newProj)
                _snackbarMessage.emit("Created ${starter.name}")
            }
        }
    }

    fun generateProjectWithAi(prompt: String) {
        _isGeneratingProject.value = true
        viewModelScope.launch {
            val result = geminiService.generateFullProject(prompt, _selectedModel.value)
            _isGeneratingProject.value = false
            val files = result.getOrElse { emptyMap() }
            if (files.isNotEmpty()) {
                val projectName = prompt.take(24).trim().ifEmpty { "AI Project" }
                val newId = repository.createProject(projectName, "Generated: $prompt", files)
                val all = repository.allProjects.first()
                val newProj = all.find { it.id == newId }
                if (newProj != null) {
                    selectProject(newProj)
                    _snackbarMessage.emit("AI generated $projectName successfully!")
                }
            } else {
                _snackbarMessage.emit("Could not generate project. Please try again.")
            }
        }
    }

    fun addNewFile(fileName: String) {
        val projectId = _activeProject.value?.id ?: return
        viewModelScope.launch {
            val defaultContent = when {
                fileName.endsWith(".html", ignoreCase = true) -> "<!DOCTYPE html>\n<html>\n<head>\n  <title>$fileName</title>\n</head>\n<body>\n\n</body>\n</html>"
                fileName.endsWith(".css", ignoreCase = true) -> "/* Stylesheet for $fileName */\n"
                fileName.endsWith(".js", ignoreCase = true) -> "// Script for $fileName\n"
                else -> ""
            }
            val newFileId = repository.addFile(projectId, fileName, defaultContent)
            _snackbarMessage.emit("Added $fileName")
        }
    }

    fun deleteFile(file: ProjectFileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file)
            _snackbarMessage.emit("Deleted ${file.name}")
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            val remaining = repository.allProjects.first()
            if (remaining.isNotEmpty()) {
                selectProject(remaining.first())
            }
            _snackbarMessage.emit("Deleted ${project.name}")
        }
    }

    fun renameProject(projectId: Long, newName: String) {
        viewModelScope.launch {
            repository.updateProjectTitle(projectId, newName)
            _snackbarMessage.emit("Renamed to $newName")
        }
    }

    fun sendAiChatMessage(userMessage: String) {
        val projectId = _activeProject.value?.id ?: 0L
        viewModelScope.launch {
            // Save user message
            val userEntity = ChatMessageEntity(
                projectId = projectId,
                sender = "USER",
                text = userMessage,
                timestamp = System.currentTimeMillis()
            )
            repository.addChatMessage(userEntity)

            _isAiLoading.value = true

            // Send to Gemini
            val activeFileName = _activeFile.value?.name
            val activeContent = _activeFileContent.value
            val history = _chatMessages.value.map { Pair(it.sender, it.text) }

            val responseResult = geminiService.generateCodingResponse(
                userPrompt = userMessage,
                contextCode = activeContent,
                activeFileName = activeFileName,
                chatHistory = history,
                modelName = _selectedModel.value
            )

            _isAiLoading.value = false

            val aiText = responseResult.getOrElse { "Could not complete request: ${it.message}" }
            val aiEntity = ChatMessageEntity(
                projectId = projectId,
                sender = "AI",
                text = aiText,
                timestamp = System.currentTimeMillis()
            )
            repository.addChatMessage(aiEntity)
        }
    }

    fun applyCodeToActiveFile(code: String) {
        _activeFileContent.value = code
        _hasUnsavedChanges.value = true
        saveActiveFile()
        _reloadTrigger.value = _reloadTrigger.value + 1
        viewModelScope.launch {
            _snackbarMessage.emit("Applied code to ${_activeFile.value?.name}")
        }
    }

    fun clearChat() {
        val projectId = _activeProject.value?.id ?: return
        viewModelScope.launch {
            repository.clearChatMessages(projectId)
            _snackbarMessage.emit("Chat cleared")
        }
    }

    fun addConsoleLog(type: String, message: String) {
        val newLog = ConsoleLogItem(type = type, message = message)
        _consoleLogs.value = (_consoleLogs.value + newLog).takeLast(100)
    }

    fun clearConsoleLogs() {
        _consoleLogs.value = emptyList()
    }

    fun exportProjectZip(context: Context) {
        val project = _activeProject.value ?: return
        val files = _projectFiles.value
        val uri = ZipExporter.exportProjectToZip(context, project.name, files)
        if (uri != null) {
            ZipExporter.shareZipFile(context, uri, project.name)
        } else {
            viewModelScope.launch {
                _snackbarMessage.emit("Failed to create ZIP")
            }
        }
    }

    fun shareSingleFile(context: Context, file: ProjectFileEntity) {
        ZipExporter.shareSingleFile(context, file.name, file.content)
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
        viewModelScope.launch {
            _snackbarMessage.emit("API Key saved")
        }
    }

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
        viewModelScope.launch {
            _snackbarMessage.emit("Selected model: $model")
        }
    }
}
