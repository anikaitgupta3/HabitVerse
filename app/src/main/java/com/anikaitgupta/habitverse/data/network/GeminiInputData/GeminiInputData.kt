package com.anikaitgupta.habitverse.data.network.GeminiInputData

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
@Keep
@Serializable
data class GeminiInputData(
    val contents:List<Content>
)