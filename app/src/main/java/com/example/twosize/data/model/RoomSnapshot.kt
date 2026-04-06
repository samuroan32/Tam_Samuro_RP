package com.example.twosize.data.model

data class RoomSnapshot(
    val roomCode: String = "",
    val participants: List<Participant> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val events: List<GrowthRecord> = emptyList(),
    val roomMeta: RoomMeta = RoomMeta()
)
