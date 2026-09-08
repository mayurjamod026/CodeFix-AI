package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val languageId: String,
    val code: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history_entries")
data class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long? = null,
    val projectTitle: String = "Untitled",
    val languageId: String,
    val codeSnapshot: String,
    val output: String? = null,
    val error: String? = null,
    val executionStatus: String,
    val aiAnalysisSummary: String? = null,
    val aiFixSuggestion: String? = null,
    val explanationSnippet: String? = null,
    val actionType: String, // "RUN", "ANALYZE", "FIX", "EXPLAIN"
    val timestamp: Long = System.currentTimeMillis()
)
