package com.example.twosize.data.repository

import com.example.twosize.data.model.ChatMessage
import com.example.twosize.data.model.GrowthRecord
import com.example.twosize.data.model.JoinRequest
import com.example.twosize.data.model.Participant
import com.example.twosize.data.model.RoomSnapshot
import com.example.twosize.data.remote.FirebaseRoomService
import com.example.twosize.util.ChangeMode
import com.example.twosize.util.SizeParser
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RoomRepository(
    private val firebaseRoomService: FirebaseRoomService
) {
    suspend fun joinRoom(request: JoinRequest, userId: String): Participant {
        val mm = SizeParser.decimalToMillimeters(request.startingSizeInput, request.unitType)
            ?: throw IllegalArgumentException("Invalid starting size")
        val participant = Participant(
            userId = userId,
            displayName = request.displayName.trim(),
            startingSizeMm = mm.toString(),
            currentSizeMm = mm.toString(),
            online = true,
            lastUpdatedAt = System.currentTimeMillis()
        )
        firebaseRoomService.joinRoom(request.roomCode.trim(), participant)
        return participant
    }

    fun observeRoom(roomCode: String): Flow<RoomSnapshot> = firebaseRoomService.observeRoom(roomCode)

    suspend fun grow(roomCode: String, userId: String, mode: ChangeMode): GrowthRecord =
        firebaseRoomService.grow(roomCode, userId, mode)

    suspend fun shrink(roomCode: String, userId: String, mode: ChangeMode): GrowthRecord =
        firebaseRoomService.shrink(roomCode, userId, mode)

    suspend fun sendChat(roomCode: String, userId: String, senderName: String, text: String) {
        val msg = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            userId = userId,
            senderName = senderName,
            message = text,
            type = "user",
            createdAt = System.currentTimeMillis()
        )
        firebaseRoomService.sendChat(roomCode, msg)
    }

    suspend fun setOnline(roomCode: String, userId: String, online: Boolean) {
        firebaseRoomService.setOnline(roomCode, userId, online)
    }
}
