package com.example.twosize.data.remote

import com.example.twosize.data.model.ChatMessage
import com.example.twosize.data.model.GrowthRecord
import com.example.twosize.data.model.Participant
import com.example.twosize.data.model.RoomMeta
import com.example.twosize.data.model.RoomSnapshot
import com.example.twosize.util.ChangeMode
import com.example.twosize.util.SizeChangeCalculator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.math.BigInteger

class FirebaseRoomService(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val calculator: SizeChangeCalculator = SizeChangeCalculator()
) {
    private fun roomRef(roomCode: String): DatabaseReference =
        database.reference.child("rooms").child(roomCode)

    suspend fun joinRoom(roomCode: String, participant: Participant) {
        val participantsRef = roomRef(roomCode).child("participants")
        val snapshot = participantsRef.get().await()
        val existing = snapshot.childrenCount
        if (existing >= 2 && snapshot.child(participant.userId).value == null) {
            throw IllegalStateException("Room already has two participants")
        }

        participantsRef.child(participant.userId).setValue(participant).await()
        roomRef(roomCode).child("meta").setValue(
            RoomMeta(roomCode = roomCode, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        ).await()

        val joinMessage = ChatMessage(
            messageId = roomRef(roomCode).child("messages").push().key ?: System.currentTimeMillis().toString(),
            userId = participant.userId,
            senderName = participant.displayName,
            message = "${participant.displayName} joined the room",
            type = "system",
            createdAt = System.currentTimeMillis()
        )
        roomRef(roomCode).child("messages").child(joinMessage.messageId).setValue(joinMessage).await()

        val record = GrowthRecord(
            eventId = roomRef(roomCode).child("events").push().key ?: System.currentTimeMillis().toString(),
            userId = participant.userId,
            type = "join",
            deltaMm = "0",
            sizeAfterMm = participant.currentSizeMm,
            createdAt = System.currentTimeMillis()
        )
        roomRef(roomCode).child("events").child(record.eventId).setValue(record).await()
    }

    fun observeRoom(roomCode: String): Flow<RoomSnapshot> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val participants = snapshot.child("participants").children
                    .mapNotNull { it.getValue(Participant::class.java) }
                    .sortedBy { it.displayName }
                val messages = snapshot.child("messages").children
                    .mapNotNull { it.getValue(ChatMessage::class.java) }
                    .sortedBy { it.createdAt }
                val events = snapshot.child("events").children
                    .mapNotNull { it.getValue(GrowthRecord::class.java) }
                    .sortedByDescending { it.createdAt }
                val meta = snapshot.child("meta").getValue(RoomMeta::class.java) ?: RoomMeta(roomCode = roomCode)
                trySend(
                    RoomSnapshot(
                        roomCode = roomCode,
                        participants = participants,
                        messages = messages,
                        events = events,
                        roomMeta = meta
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        roomRef(roomCode).addValueEventListener(listener)
        awaitClose { roomRef(roomCode).removeEventListener(listener) }
    }

    suspend fun grow(roomCode: String, userId: String, mode: ChangeMode): GrowthRecord {
        return mutateSize(roomCode, userId, mode, "grow")
    }

    suspend fun shrink(roomCode: String, userId: String, mode: ChangeMode): GrowthRecord {
        return mutateSize(roomCode, userId, mode, "shrink")
    }

    private suspend fun mutateSize(roomCode: String, userId: String, mode: ChangeMode, type: String): GrowthRecord {
        val ref = roomRef(roomCode).child("participants").child(userId)
        val snapshot = ref.get().await()
        val participant = snapshot.getValue(Participant::class.java)
            ?: throw IllegalStateException("Participant missing")
        val current = participant.currentSizeMm.toBigIntegerOrNull() ?: BigInteger.ONE
        val result = if (type == "grow") calculator.grow(current, mode) else calculator.shrink(current, mode)

        val updated = participant.copy(
            currentSizeMm = result.newSize.toString(),
            totalGrowthMm = if (type == "grow") {
                ((participant.totalGrowthMm.toBigIntegerOrNull() ?: BigInteger.ZERO) + result.delta).toString()
            } else participant.totalGrowthMm,
            totalShrinkMm = if (type == "shrink") {
                ((participant.totalShrinkMm.toBigIntegerOrNull() ?: BigInteger.ZERO) + result.delta).toString()
            } else participant.totalShrinkMm,
            biggestIncreaseMm = if (type == "grow") {
                maxBigInt(participant.biggestIncreaseMm.toBigIntegerOrNull() ?: BigInteger.ZERO, result.delta).toString()
            } else participant.biggestIncreaseMm,
            biggestDecreaseMm = if (type == "shrink") {
                maxBigInt(participant.biggestDecreaseMm.toBigIntegerOrNull() ?: BigInteger.ZERO, result.delta).toString()
            } else participant.biggestDecreaseMm,
            actionCount = participant.actionCount + 1,
            online = true,
            lastUpdatedAt = System.currentTimeMillis()
        )
        ref.setValue(updated).await()

        val record = GrowthRecord(
            eventId = roomRef(roomCode).child("events").push().key ?: System.currentTimeMillis().toString(),
            userId = userId,
            type = type,
            deltaMm = result.delta.toString(),
            sizeAfterMm = result.newSize.toString(),
            createdAt = System.currentTimeMillis()
        )
        roomRef(roomCode).child("events").child(record.eventId).setValue(record).await()

        val actionMsg = ChatMessage(
            messageId = roomRef(roomCode).child("messages").push().key ?: System.currentTimeMillis().toString(),
            userId = userId,
            senderName = participant.displayName,
            message = if (type == "grow") "${participant.displayName} grew by ${result.delta} mm" else "${participant.displayName} shrank by ${result.delta} mm",
            type = "system",
            createdAt = System.currentTimeMillis()
        )
        roomRef(roomCode).child("messages").child(actionMsg.messageId).setValue(actionMsg).await()

        return record
    }

    suspend fun sendChat(roomCode: String, message: ChatMessage) {
        roomRef(roomCode).child("messages").child(message.messageId).setValue(message).await()
    }

    suspend fun setOnline(roomCode: String, userId: String, online: Boolean) {
        roomRef(roomCode).child("participants").child(userId).child("online").setValue(online).await()
    }

    private fun maxBigInt(a: BigInteger, b: BigInteger): BigInteger = if (a > b) a else b
}
