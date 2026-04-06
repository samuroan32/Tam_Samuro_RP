package com.example.twosize.data.model

import com.example.twosize.util.UnitType

data class JoinRequest(
    val displayName: String,
    val startingSizeInput: String,
    val unitType: UnitType,
    val roomCode: String
)
