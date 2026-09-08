package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.HistoryEntryEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.ProjectRepository
import com.example.data.model.AiAnalysisResult
import com.example.data.model.AiExplainResult
import com.example.data.model.AiFixResult
import com.example.data.model.ChatMessage
import com.example.data.model.ExecutionResult
import com.example.data.model.ExecutionStatus
import com.example.data.model.ExplainLanguage
import com.example.data.model.ExplainMode
import com.example.data.model.Language
import com.example.data.model.MessageSender
import com.example.data.model.PracticeChallenge
import com.example.service.GeminiAiService
import com.example.service.SandboxExecutionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppScreen(val title: String) {
    HOME("Home"),
    EDITOR("Code Editor"),
    CHAT("AI Tutor"),
    PRACTICE("Practice"),
    PROJECTS("Projects & Logs")
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val currentLanguage: Language = Language.PYTHON,
    val currentCode: String = Language.PYTHON.sampleCode,
    val currentProject: ProjectEntity? = null,
    val projects: List<ProjectEntity> = emptyList(),
    val history: List<HistoryEntryEntity> = emptyList(),
    val isDarkTheme: Boolean = true,
    // Execution
    val isExecuting: Boolean = false,
    val executionResult: ExecutionResult? = null,
    val consoleVisible: Boolean = false,
    // AI Analysis
    val isAnalyzing: Boolean = false,
    val analysisResult: AiAnalysisResult? = null,
    val showAnalysisSheet: Boolean = false,
    // AI Fix
    val isFixing: Boolean = false,
    val fixResult: AiFixResult? = null,
    val showFixDialog: Boolean = false,
    // AI Explain
    val isExplaining: Boolean = false,
    val explainResult: AiExplainResult? = null,
    val showExplainDialog: Boolean = false,
    val selectedExplainMode: ExplainMode = ExplainMode.FULL,
    val selectedExplainLanguage: ExplainLanguage = ExplainLanguage.ENGLISH,
    // AI Chat
    val chatMessages: List<ChatMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    // Toast / Feedback
    val userNotification: String? = null
)

class CodeFixViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(database.projectDao(), database.historyDao())
    private val executionService = SandboxExecutionService()
    private val geminiService = GeminiAiService()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Observe projects from Room
        viewModelScope.launch {
            repository.allProjects.collectLatest { list ->
                _uiState.value = _uiState.value.copy(projects = list)
            }
        }
        // Observe history from Room
        viewModelScope.launch {
            repository.allHistory.collectLatest { list ->
                _uiState.value = _uiState.value.copy(history = list)
            }
        }

        // Add welcome message to AI chat
        _uiState.value = _uiState.value.copy(
            chatMessages = listOf(
                ChatMessage(
                    sender = MessageSender.AI_TUTOR,
                    content = "Hello! I am your CodeFix AI programming tutor. I have loaded your current editor code context. Ask me anything: error explanations, logic reviews, BCA exam tips, or line-by-line breakdowns!"
                )
            )
        )
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun toggleTheme() {
        _uiState.value = _uiState.value.copy(isDarkTheme = !_uiState.value.isDarkTheme)
    }

    fun selectLanguage(language: Language) {
        // If code is still default sample, replace with new sample
        val current = _uiState.value.currentLanguage
        val currentCode = _uiState.value.currentCode
        val isDefaultSample = currentCode == current.sampleCode

        val newCode = if (isDefaultSample) language.sampleCode else currentCode
        _uiState.value = _uiState.value.copy(
            currentLanguage = language,
            currentCode = newCode
        )
    }

    fun updateCode(code: String) {
        _uiState.value = _uiState.value.copy(currentCode = code)
    }

    fun clearCode() {
        _uiState.value = _uiState.value.copy(currentCode = "")
    }

    fun dismissNotification() {
        _uiState.value = _uiState.value.copy(userNotification = null)
    }

    fun setNotification(msg: String) {
        _uiState.value = _uiState.value.copy(userNotification = msg)
    }

    fun runCode() {
        val state = _uiState.value
        val code = state.currentCode
        val lang = state.currentLanguage

        if (code.isBlank()) {
            _uiState.value = state.copy(
                userNotification = "Code is empty. Please enter some code to run."
            )
        }

        _uiState.value = _uiState.value.copy(
            isExecuting = true,
            consoleVisible = true
        )

        viewModelScope.launch {
            val result = executionService.execute(code, lang)
            _uiState.value = _uiState.value.copy(
                isExecuting = false,
                executionResult = result,
                consoleVisible = true
            )

            // Log to Room History
            repository.logHistory(
                HistoryEntryEntity(
                    projectId = state.currentProject?.id,
                    projectTitle = state.currentProject?.title ?: "Scratchpad (${lang.displayName})",
                    languageId = lang.id,
                    codeSnapshot = code,
                    output = result.stdout,
                    error = result.stderr ?: result.compilationOutput,
                    executionStatus = result.status.name,
                    actionType = "RUN"
                )
            )
        }
    }

    fun analyzeCode() {
        val state = _uiState.value
        val code = state.currentCode
        val lang = state.currentLanguage

        if (code.isBlank()) {
            _uiState.value = state.copy(
                userNotification = "Code is empty. Please enter some code before analyzing."
            )
        }

        _uiState.value = _uiState.value.copy(
            isAnalyzing = true,
            showAnalysisSheet = true
        )

        viewModelScope.launch {
            val analysis = geminiService.analyzeCode(code, lang)
            _uiState.value = _uiState.value.copy(
                isAnalyzing = false,
                analysisResult = analysis,
                showAnalysisSheet = true
            )

            // Log to Room History
            repository.logHistory(
                HistoryEntryEntity(
                    projectId = state.currentProject?.id,
                    projectTitle = state.currentProject?.title ?: "Scratchpad (${lang.displayName})",
                    languageId = lang.id,
                    codeSnapshot = code,
                    executionStatus = "ANALYSIS_COMPLETE",
                    aiAnalysisSummary = "${analysis.qualityScore}/100: ${analysis.summary}",
                    actionType = "ANALYZE"
                )
            )
        }
    }

    fun requestFix() {
        val state = _uiState.value
        val code = state.currentCode
        val lang = state.currentLanguage
        val errorContext = state.executionResult?.stderr ?: state.executionResult?.compilationOutput

        if (code.isBlank()) {
            _uiState.value = state.copy(
                userNotification = "Code is empty. Please enter some code before requesting a fix."
            )
        }

        _uiState.value = _uiState.value.copy(
            isFixing = true,
            showFixDialog = true
        )

        viewModelScope.launch {
            val fix = geminiService.fixCode(code, lang, errorContext)
            _uiState.value = _uiState.value.copy(
                isFixing = false,
                fixResult = fix,
                showFixDialog = true
            )

            // Log to Room History
            repository.logHistory(
                HistoryEntryEntity(
                    projectId = state.currentProject?.id,
                    projectTitle = state.currentProject?.title ?: "Scratchpad (${lang.displayName})",
                    languageId = lang.id,
                    codeSnapshot = code,
                    executionStatus = "FIX_GENERATED",
                    aiFixSuggestion = fix.explanation,
                    actionType = "FIX"
                )
            )
        }
    }

    fun applyFix(newCode: String) {
        _uiState.value = _uiState.value.copy(
            currentCode = newCode,
            showFixDialog = false,
            userNotification = "AI fix applied to editor successfully!"
        )
    }

    fun dismissFixDialog() {
        _uiState.value = _uiState.value.copy(showFixDialog = false)
    }

    fun dismissAnalysisSheet() {
        _uiState.value = _uiState.value.copy(showAnalysisSheet = false)
    }

    fun dismissExplainDialog() {
        _uiState.value = _uiState.value.copy(showExplainDialog = false)
    }

    fun setConsoleVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(consoleVisible = visible)
    }

    fun requestExplanation(mode: ExplainMode? = null, language: ExplainLanguage? = null) {
        val state = _uiState.value
        val code = state.currentCode
        val lang = state.currentLanguage
        val finalMode = mode ?: state.selectedExplainMode
        val finalLang = language ?: state.selectedExplainLanguage

        _uiState.value = state.copy(
            isExplaining = true,
            showExplainDialog = true,
            selectedExplainMode = finalMode,
            selectedExplainLanguage = finalLang
        )

        viewModelScope.launch {
            val explanation = geminiService.explainCode(code, lang, finalMode, finalLang)
            _uiState.value = _uiState.value.copy(
                isExplaining = false,
                explainResult = explanation,
                showExplainDialog = true
            )

            // Log to Room History
            repository.logHistory(
                HistoryEntryEntity(
                    projectId = state.currentProject?.id,
                    projectTitle = state.currentProject?.title ?: "Scratchpad (${lang.displayName})",
                    languageId = lang.id,
                    codeSnapshot = code,
                    executionStatus = "EXPLAINED",
                    explanationSnippet = explanation.summary,
                    actionType = "EXPLAIN"
                )
            )
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val currentMsgs = _uiState.value.chatMessages.toMutableList()
        val userMsg = ChatMessage(sender = MessageSender.USER, content = text)
        currentMsgs.add(userMsg)

        _uiState.value = _uiState.value.copy(
            chatMessages = currentMsgs,
            isChatLoading = true
        )

        viewModelScope.launch {
            val reply = geminiService.chatWithCode(
                messages = currentMsgs,
                currentCode = _uiState.value.currentCode,
                language = _uiState.value.currentLanguage
            )

            val updatedMsgs = _uiState.value.chatMessages.toMutableList()
            updatedMsgs.add(ChatMessage(sender = MessageSender.AI_TUTOR, content = reply))
            _uiState.value = _uiState.value.copy(
                chatMessages = updatedMsgs,
                isChatLoading = false
            )
        }
    }

    fun saveProject(title: String) {
        val state = _uiState.value
        val entity = ProjectEntity(
            id = state.currentProject?.id ?: 0L,
            title = title.ifBlank { "Untitled ${state.currentLanguage.displayName} Project" },
            languageId = state.currentLanguage.id,
            code = state.currentCode,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            val savedId = repository.saveProject(entity)
            _uiState.value = _uiState.value.copy(
                currentProject = entity.copy(id = savedId),
                userNotification = "Project '${entity.title}' saved successfully!"
            )
        }
    }

    fun createNewProject(title: String, language: Language) {
        val entity = ProjectEntity(
            title = title.ifBlank { "New ${language.displayName} Project" },
            languageId = language.id,
            code = language.sampleCode
        )

        viewModelScope.launch {
            val newId = repository.saveProject(entity)
            val created = entity.copy(id = newId)
            _uiState.value = _uiState.value.copy(
                currentProject = created,
                currentLanguage = language,
                currentCode = created.code,
                currentScreen = AppScreen.EDITOR,
                userNotification = "Created new project '${created.title}'"
            )
        }
    }

    fun openProject(project: ProjectEntity) {
        val lang = Language.fromId(project.languageId)
        _uiState.value = _uiState.value.copy(
            currentProject = project,
            currentLanguage = lang,
            currentCode = project.code,
            currentScreen = AppScreen.EDITOR,
            userNotification = "Opened project '${project.title}'"
        )
    }

    fun renameProject(id: Long, newTitle: String) {
        viewModelScope.launch {
            repository.renameProject(id, newTitle)
            if (_uiState.value.currentProject?.id == id) {
                _uiState.value = _uiState.value.copy(
                    currentProject = _uiState.value.currentProject?.copy(title = newTitle)
                )
            }
            _uiState.value = _uiState.value.copy(
                userNotification = "Renamed project to '$newTitle'"
            )
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_uiState.value.currentProject?.id == id) {
                _uiState.value = _uiState.value.copy(currentProject = null)
            }
            _uiState.value = _uiState.value.copy(
                userNotification = "Project deleted."
            )
        }
    }

    fun loadPracticeChallenge(challenge: PracticeChallenge) {
        val starter = challenge.starterCodes[challenge.defaultLanguage]
            ?: challenge.defaultLanguage.sampleCode

        _uiState.value = _uiState.value.copy(
            currentLanguage = challenge.defaultLanguage,
            currentCode = starter,
            currentProject = null,
            currentScreen = AppScreen.EDITOR,
            userNotification = "Loaded '${challenge.title}' into editor. Good luck!"
        )
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.value = _uiState.value.copy(userNotification = "Execution history cleared.")
        }
    }
}
