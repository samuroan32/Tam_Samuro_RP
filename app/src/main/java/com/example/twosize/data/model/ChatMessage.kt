package com.example.twosize.data.model

data class ChatMessage(
    val messageId: String = "",
    val userId: String = "",
    val senderName: String = "",
    val message: String = "",
    val type: String = "user",
    val createdAt: Long = 0L
)
