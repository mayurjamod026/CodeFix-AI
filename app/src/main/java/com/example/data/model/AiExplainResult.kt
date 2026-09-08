package com.example.data.model

data class AiExplainResult(
    val summary: String,
    val mode: ExplainMode,
    val languageLocale: ExplainLanguage,
    val lineExplanations: List<LineExplanation> = emptyList(),
    val beginnerConcepts: List<String> = emptyList(),
    val keyTakeaways: List<String> = emptyList()
)

data class LineExplanation(
    val lineRange: String,
    val codeSnippet: String,
    val explanation: String
)

enum class ExplainMode(val displayName: String) {
    FULL("Full Explanation"),
    LINE_BY_LINE("Line-by-Line"),
    BEGINNER("Beginner Friendly")
}

enum class ExplainLanguage(val displayName: String, val promptName: String) {
    ENGLISH("English", "English"),
    HINDI("Hindi (हिन्दी)", "Hindi"),
    GUJARATI("Gujarati (ગુજરાતી)", "Gujarati")
}
