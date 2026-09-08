package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("UPDATE projects SET title = :newTitle, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renameProject(id: Long, newTitle: String, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<HistoryEntryEntity>>

    @Query("SELECT * FROM history_entries WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getHistoryByProject(projectId: Long): Flow<List<HistoryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntryEntity): Long

    @Query("DELETE FROM history_entries WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history_entries")
    suspend fun clearAllHistory()
}
