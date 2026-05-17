package com.example.habitverse.data.network.GeminiInputData

import kotlinx.serialization.Serializable

@Serializable
data class GeminiInputData(
    val contents:List<Content>
)