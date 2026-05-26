package com.anikaitgupta.habitverse.data.network.GeminiInputData

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Keep
@Serializable
data class GeminiInputData(
    @SerialName("system_instruction")
    val systemInstruction: SystemInstruction,
    val contents:List<Content>
)
@Serializable
data class SystemInstruction(
    val parts: List<Part>
)