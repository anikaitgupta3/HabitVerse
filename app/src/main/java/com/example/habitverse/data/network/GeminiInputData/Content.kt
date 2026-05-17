package com.example.habitverse.data.network.GeminiInputData

import kotlinx.serialization.Serializable

@Serializable
data class Content(val parts:List<Part>)
