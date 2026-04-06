package com.example.twosize.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.twosize.data.model.JoinRequest
import com.example.twosize.data.model.Participant
import com.example.twosize.data.model.RoomSnapshot
import com.example.twosize.data.remote.FirebaseRoomService
import com.example.twosize.data.repository.RoomRepository
import com.example.twosize.data.repository.UserPrefsRepository
import com.example.twosize.util.ChangeMode
import com.example.twosize.util.RelationshipAnalyzer
import com.example.twosize.util.RelationshipSummary
import com.example.twosize.util.UnitType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class JoinFormState(
    val displayName: String = "",
    val startingSize: String = "",
    val unitType: UnitType = UnitType.CM,
    val roomCode: String = "",
    val error: String? = null
)

data class MainUiState(
    val joined: Boolean = false,
    val loading: Boolean = false,
    val roomCode: String = "",
    val userId: String = "",
    val me: Participant? = null,
    val partner: Participant? = null,
    val snapshot: RoomSnapshot = RoomSnapshot(),
    val mode: ChangeMode = ChangeMode.BALANCED,
    val relationship: RelationshipSummary = RelationshipSummary("N/A", "Waiting for partner"),
    val lastAction: String = "",
    val chatDraft: String = "",
    val joinForm: JoinFormState = JoinFormState()
)

class MainViewModel(
    private val roomRepository: RoomRepository,
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun updateJoinName(value: String) = _uiState.update { it.copy(joinForm = it.joinForm.copy(displayName = value, error = null)) }
    fun updateJoinSize(value: String) = _uiState.update { it.copy(joinForm = it.joinForm.copy(startingSize = value, error = null)) }
    fun updateJoinRoom(value: String) = _uiState.update { it.copy(joinForm = it.joinForm.copy(roomCode = value.uppercase(), error = null)) }
    fun updateJoinUnit(value: UnitType) = _uiState.update { it.copy(joinForm = it.joinForm.copy(unitType = value, error = null)) }

    fun joinRoom() {
        val form = _uiState.value.joinForm
        if (form.displayName.isBlank() || form.startingSize.isBlank() || form.roomCode.isBlank()) {
            _uiState.update { it.copy(joinForm = form.copy(error = "All fields are required")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            runCatching {
                val userId = userPrefsRepository.userIdFlow.first() ?: UUID.randomUUID().toString().also {
                    userPrefsRepository.saveUserId(it)
                }
                roomRepository.joinRoom(
                    JoinRequest(
                        displayName = form.displayName,
                        startingSizeInput = form.startingSize,
                        unitType = form.unitType,
                        roomCode = form.roomCode
                    ),
                    userId = userId
                )
                userPrefsRepository.saveRoomCode(form.roomCode)
                _uiState.update {
                    it.copy(
                        joined = true,
                        loading = false,
                        roomCode = form.roomCode,
                        userId = userId,
                        lastAction = "Joined ${form.roomCode}"
                    )
                }
                startObservingRoom(form.roomCode, userId)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        joinForm = it.joinForm.copy(error = throwable.message ?: "Failed to join")
                    )
                }
            }
        }
    }

    fun reconnectIfPossible() {
        viewModelScope.launch {
            val existingUserId = userPrefsRepository.userIdFlow.first() ?: return@launch
            val existingRoom = userPrefsRepository.roomCodeFlow.first() ?: return@launch
            if (existingRoom.isNotBlank()) {
                _uiState.update {
                    it.copy(
                        joined = true,
                        userId = existingUserId,
                        roomCode = existingRoom,
                        joinForm = it.joinForm.copy(roomCode = existingRoom)
                    )
                }
                startObservingRoom(existingRoom, existingUserId)
            }
        }
    }

    private fun startObservingRoom(roomCode: String, userId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            roomRepository.observeRoom(roomCode).collect { snapshot ->
                val me = snapshot.participants.firstOrNull { it.userId == userId }
                val partner = snapshot.participants.firstOrNull { it.userId != userId }
                val relationship = if (me != null && partner != null) {
                    RelationshipAnalyzer.analyze(me.currentSizeBigInt, partner.currentSizeBigInt)
                } else {
                    RelationshipSummary("N/A", "Waiting for partner")
                }
                _uiState.update {
                    it.copy(
                        snapshot = snapshot,
                        me = me,
                        partner = partner,
                        relationship = relationship
                    )
                }
            }
        }
    }

    fun setMode(mode: ChangeMode) = _uiState.update { it.copy(mode = mode) }

    fun grow() = runAction("grow")
    fun shrink() = runAction("shrink")

    private fun runAction(type: String) {
        val room = _uiState.value.roomCode
        val user = _uiState.value.userId
        if (room.isBlank() || user.isBlank()) return

        viewModelScope.launch {
            runCatching {
                if (type == "grow") roomRepository.grow(room, user, _uiState.value.mode)
                else roomRepository.shrink(room, user, _uiState.value.mode)
            }.onSuccess { rec ->
                _uiState.update {
                    it.copy(
                        lastAction = "${type.replaceFirstChar(Char::uppercase)} ${rec.deltaMm} mm"
                    )
                }
            }
        }
    }

    fun updateChatDraft(value: String) = _uiState.update { it.copy(chatDraft = value) }

    fun sendChat() {
        val text = _uiState.value.chatDraft.trim()
        val me = _uiState.value.me ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            roomRepository.sendChat(_uiState.value.roomCode, me.userId, me.displayName, text)
            _uiState.update { it.copy(chatDraft = "") }
        }
    }

    fun setOnline(online: Boolean) {
        val room = _uiState.value.roomCode
        val userId = _uiState.value.userId
        if (room.isBlank() || userId.isBlank()) return
        viewModelScope.launch {
            runCatching { roomRepository.setOnline(room, userId, online) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        setOnline(false)
    }

    companion object {
        fun factory(userPrefsRepository: UserPrefsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(
                        roomRepository = RoomRepository(FirebaseRoomService()),
                        userPrefsRepository = userPrefsRepository
                    ) as T
                }
            }
    }
}
