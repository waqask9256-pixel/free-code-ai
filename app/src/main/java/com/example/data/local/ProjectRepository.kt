package com.example.data.local

import com.example.data.model.ChatMessageEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val chatDao: ChatDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun ensureDefaultProjects() {
        val currentProjects = allProjects.first()
        if (currentProjects.isEmpty()) {
            for (starter in DefaultProjects.starterProjects) {
                createProjectFromStarter(starter)
            }
        }
    }

    suspend fun createProjectFromStarter(starter: StarterProject): Long {
        val project = ProjectEntity(
            name = starter.name,
            description = starter.description,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val projectId = projectDao.insertProject(project)
        starter.files.forEach { (fileName, content) ->
            val type = detectFileType(fileName)
            projectDao.insertFile(
                ProjectFileEntity(
                    projectId = projectId,
                    name = fileName,
                    content = content,
                    fileType = type
                )
            )
        }
        return projectId
    }

    suspend fun createProject(name: String, description: String, files: Map<String, String>): Long {
        val project = ProjectEntity(
            name = name,
            description = description,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val projectId = projectDao.insertProject(project)
        files.forEach { (fileName, content) ->
            val type = detectFileType(fileName)
            projectDao.insertFile(
                ProjectFileEntity(
                    projectId = projectId,
                    name = fileName,
                    content = content,
                    fileType = type
                )
            )
        }
        return projectId
    }

    fun getFilesForProject(projectId: Long): Flow<List<ProjectFileEntity>> {
        return projectDao.getFilesForProject(projectId)
    }

    suspend fun getFilesForProjectDirect(projectId: Long): List<ProjectFileEntity> {
        return projectDao.getFilesForProjectDirect(projectId)
    }

    suspend fun saveFile(projectId: Long, file: ProjectFileEntity) {
        projectDao.updateFile(file.copy(updatedAt = System.currentTimeMillis()))
        val project = projectDao.getProjectById(projectId)
        if (project != null) {
            projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun addFile(projectId: Long, fileName: String, content: String = ""): Long {
        val type = detectFileType(fileName)
        val file = ProjectFileEntity(
            projectId = projectId,
            name = fileName,
            content = content,
            fileType = type,
            updatedAt = System.currentTimeMillis()
        )
        return projectDao.insertFile(file)
    }

    suspend fun deleteFile(file: ProjectFileEntity) {
        projectDao.deleteFile(file)
    }

    suspend fun deleteProject(project: ProjectEntity) {
        projectDao.deleteProject(project)
        chatDao.clearMessages(project.id)
    }

    suspend fun updateProjectTitle(projectId: Long, newName: String, description: String = "") {
        val project = projectDao.getProjectById(projectId)
        if (project != null) {
            projectDao.updateProject(project.copy(name = newName, description = description, updatedAt = System.currentTimeMillis()))
        }
    }

    fun getChatMessages(projectId: Long): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessages(projectId)
    }

    suspend fun addChatMessage(message: ChatMessageEntity): Long {
        return chatDao.insertMessage(message)
    }

    suspend fun clearChatMessages(projectId: Long) {
        chatDao.clearMessages(projectId)
    }

    private fun detectFileType(fileName: String): String {
        return when {
            fileName.endsWith(".html", ignoreCase = true) || fileName.endsWith(".htm", ignoreCase = true) -> "HTML"
            fileName.endsWith(".css", ignoreCase = true) -> "CSS"
            fileName.endsWith(".js", ignoreCase = true) || fileName.endsWith(".jsx", ignoreCase = true) || fileName.endsWith(".ts", ignoreCase = true) -> "JAVASCRIPT"
            fileName.endsWith(".json", ignoreCase = true) -> "JSON"
            fileName.endsWith(".md", ignoreCase = true) -> "MARKDOWN"
            else -> "OTHER"
        }
    }
}
