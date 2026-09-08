package com.example.data.model

data class AiFixResult(
    val originalIssue: String,
    val correctedCode: String,
    val explanation: String,
    val changesMade: List<String>
)
