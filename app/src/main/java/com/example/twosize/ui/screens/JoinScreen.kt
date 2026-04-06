package com.example.twosize.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.twosize.ui.components.ActionButton
import com.example.twosize.ui.components.GlowCard
import com.example.twosize.util.UnitType
import com.example.twosize.viewmodel.JoinFormState

@Composable
fun JoinScreen(
    state: JoinFormState,
    loading: Boolean,
    onNameChange: (String) -> Unit,
    onSizeChange: (String) -> Unit,
    onRoomChange: (String) -> Unit,
    onUnitChange: (UnitType) -> Unit,
    onJoinClick: () -> Unit,
    onCopyRoomCode: () -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("TwinScale", style = MaterialTheme.typography.headlineMedium)
        GlowCard(title = "Join private room") {
            OutlinedTextField(
                value = state.displayName,
                onValueChange = onNameChange,
                label = { Text("Display name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.startingSize,
                onValueChange = onSizeChange,
                label = { Text("Starting size") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.unitType.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit") },
                    modifier = Modifier.weight(1f)
                )
                ActionButton(text = "Change", onClick = { expanded.value = true })
                DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                    UnitType.options.forEach { unit ->
                        DropdownMenuItem(text = { Text(unit.label) }, onClick = {
                            onUnitChange(unit)
                            expanded.value = false
                        })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.roomCode,
                    onValueChange = onRoomChange,
                    label = { Text("Room code") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCopyRoomCode) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy room code")
                }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            ActionButton(text = if (loading) "Joining..." else "Join", onClick = onJoinClick, modifier = Modifier.fillMaxWidth())
        }
    }
}
