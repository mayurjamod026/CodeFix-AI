package com.example.data.model

data class AiAnalysisResult(
    val problems: List<CodeProblem> = emptyList(),
    val summary: String = "",
    val qualityScore: Int = 85
)

data class CodeProblem(
    val line: Int,
    val category: ProblemCategory,
    val issue: String,
    val whyItHappens: String,
    val suggestedSolution: String
)

enum class ProblemCategory(val displayName: String) {
    SYNTAX_ERROR("Syntax Error"),
    LOGICAL_ISSUE("Logical Issue"),
    RUNTIME_PROBLEM("Runtime Problem"),
    COMMON_MISTAKE("Common Mistake"),
    SECURITY_RISK("Security Risk"),
    BEST_PRACTICE("Best Practice")
}
