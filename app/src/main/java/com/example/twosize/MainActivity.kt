package com.example.twosize

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.twosize.data.repository.UserPrefsRepository
import com.example.twosize.ui.screens.JoinScreen
import com.example.twosize.ui.screens.MainScreen
import com.example.twosize.ui.screens.StatsScreen
import com.example.twosize.ui.theme.TwinScaleTheme
import com.example.twosize.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.factory(UserPrefsRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.setOnline(true)
            }
        }

        setContent {
            TwinScaleTheme {
                AppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun AppRoot(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val clipboard: ClipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.reconnectIfPossible()
    }

    LaunchedEffect(uiState.joined) {
        if (uiState.joined && backStack?.destination?.route != "main") {
            navController.navigate("main")
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "join",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("join") {
                JoinScreen(
                    state = uiState.joinForm,
                    loading = uiState.loading,
                    onNameChange = viewModel::updateJoinName,
                    onSizeChange = viewModel::updateJoinSize,
                    onRoomChange = viewModel::updateJoinRoom,
                    onUnitChange = viewModel::updateJoinUnit,
                    onJoinClick = viewModel::joinRoom,
                    onCopyRoomCode = {
                        clipboard.setText(AnnotatedString(uiState.joinForm.roomCode))
                    }
                )
            }
            composable("main") {
                MainScreen(
                    me = uiState.me,
                    partner = uiState.partner,
                    messages = uiState.snapshot.messages,
                    mode = uiState.mode,
                    relationshipSummary = uiState.relationship,
                    lastAction = uiState.lastAction,
                    roomCode = uiState.roomCode,
                    chatDraft = uiState.chatDraft,
                    onModeChange = viewModel::setMode,
                    onGrow = viewModel::grow,
                    onShrink = viewModel::shrink,
                    onChatDraftChange = viewModel::updateChatDraft,
                    onSendChat = viewModel::sendChat,
                    onCopyRoom = { clipboard.setText(AnnotatedString(uiState.roomCode)) },
                    onOpenStats = { navController.navigate("stats") }
                )
            }
            composable("stats") {
                StatsScreen(me = uiState.me, modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
