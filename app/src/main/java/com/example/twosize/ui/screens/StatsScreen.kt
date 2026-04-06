package com.example.twosize.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.twosize.data.model.Participant
import com.example.twosize.ui.components.GlowCard
import com.example.twosize.ui.components.LabeledValue
import com.example.twosize.util.SizeFormatter

@Composable
fun StatsScreen(me: Participant?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Your Stats")
        GlowCard(title = "Metrics") {
            if (me == null) {
                Text("Join a room first")
            } else {
                LabeledValue("Starting", SizeFormatter.format(me.startingSizeBigInt))
                LabeledValue("Current", SizeFormatter.format(me.currentSizeBigInt))
                LabeledValue("Total growth", SizeFormatter.format(me.totalGrowthMm.toBigInteger()))
                LabeledValue("Total shrink", SizeFormatter.format(me.totalShrinkMm.toBigInteger()))
                LabeledValue("Biggest increase", SizeFormatter.format(me.biggestIncreaseMm.toBigInteger()))
                LabeledValue("Biggest decrease", SizeFormatter.format(me.biggestDecreaseMm.toBigInteger()))
                LabeledValue("Actions", me.actionCount.toString())
            }
        }
    }
}
