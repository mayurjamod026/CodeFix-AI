package com.example.data.model

data class ExecutionResult(
    val stdout: String? = null,
    val stderr: String? = null,
    val compilationOutput: String? = null,
    val exitCode: Int = 0,
    val executionTimeMs: Long = 0,
    val status: ExecutionStatus = ExecutionStatus.SUCCESS,
    val htmlPreviewContent: String? = null
)

enum class ExecutionStatus(val label: String) {
    SUCCESS("Success"),
    COMPILATION_ERROR("Compilation Error"),
    RUNTIME_ERROR("Runtime Error"),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded"),
    SANDBOX_ERROR("Sandbox Error")
}
