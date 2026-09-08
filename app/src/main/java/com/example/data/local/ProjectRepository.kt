package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val historyDao: HistoryDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allHistory: Flow<List<HistoryEntryEntity>> = historyDao.getAllHistory()

    suspend fun getProjectById(id: Long): ProjectEntity? = projectDao.getProjectById(id)

    suspend fun saveProject(project: ProjectEntity): Long {
        return if (project.id == 0L) {
            projectDao.insertProject(project)
        } else {
            projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
            project.id
        }
    }

    suspend fun renameProject(id: Long, newTitle: String) {
        projectDao.renameProject(id, newTitle)
    }

    suspend fun deleteProject(id: Long) {
        projectDao.deleteProjectById(id)
    }

    suspend fun logHistory(entry: HistoryEntryEntity): Long {
        return historyDao.insertHistory(entry)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }
}
