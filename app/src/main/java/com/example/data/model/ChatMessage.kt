package com.example.data.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val content: String,
    val codeSnippet: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    AI_TUTOR
}
