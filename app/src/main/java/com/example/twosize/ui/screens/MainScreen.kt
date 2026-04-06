package com.example.twosize.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.twosize.data.model.ChatMessage
import com.example.twosize.data.model.Participant
import com.example.twosize.ui.components.ActionButton
import com.example.twosize.ui.components.GlowCard
import com.example.twosize.ui.components.LabeledValue
import com.example.twosize.util.ChangeMode
import com.example.twosize.util.RelationshipSummary
import com.example.twosize.util.SizeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    me: Participant?,
    partner: Participant?,
    messages: List<ChatMessage>,
    mode: ChangeMode,
    relationshipSummary: RelationshipSummary,
    lastAction: String,
    roomCode: String,
    chatDraft: String,
    onModeChange: (ChangeMode) -> Unit,
    onGrow: () -> Unit,
    onShrink: () -> Unit,
    onChatDraftChange: (String) -> Unit,
    onSendChat: () -> Unit,
    onCopyRoom: () -> Unit,
    onOpenStats: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Room $roomCode", style = MaterialTheme.typography.titleLarge)
            Row {
                IconButton(onClick = onCopyRoom) { Icon(Icons.Default.ContentCopy, contentDescription = "copy") }
                IconButton(onClick = onOpenStats) { Icon(Icons.Default.BarChart, contentDescription = "stats") }
            }
        }

        GlowCard(title = "Sizes") {
            LabeledValue("You", me?.let { "${it.displayName} • ${SizeFormatter.format(it.currentSizeBigInt)}" } ?: "Not available")
            LabeledValue("Partner", partner?.let { "${it.displayName} • ${SizeFormatter.format(it.currentSizeBigInt)}" } ?: "Waiting")
            LabeledValue("Online", "You: ${if (me?.online == true) "Online" else "Offline"} / Partner: ${if (partner?.online == true) "Online" else "Offline"}")
        }

        GlowCard(title = "Mode") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ChangeMode.entries.forEachIndexed { idx, item ->
                    SegmentedButton(
                        shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(index = idx, count = ChangeMode.entries.size),
                        onClick = { onModeChange(item) },
                        selected = mode == item,
                        label = { Text(item.name.lowercase().replaceFirstChar(Char::uppercase)) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ActionButton(text = "Grow", onClick = onGrow, modifier = Modifier.weight(1f))
                ActionButton(text = "Shrink", onClick = onShrink, modifier = Modifier.weight(1f))
            }
            if (lastAction.isNotBlank()) Text(lastAction)
        }

        GlowCard(title = "Relationship") {
            Text(relationshipSummary.summaryText)
            Text(relationshipSummary.ratioText, color = MaterialTheme.colorScheme.primary)
        }

        GlowCard(title = "Chat") {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 190.dp)) {
                items(messages) { msg ->
                    val prefix = if (msg.type == "system") "•" else "${msg.senderName}:"
                    Text("$prefix ${msg.message}")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = chatDraft, onValueChange = onChatDraftChange, modifier = Modifier.weight(1f), label = { Text("Message") })
                ActionButton(text = "Send", onClick = onSendChat)
            }
        }
    }
}
