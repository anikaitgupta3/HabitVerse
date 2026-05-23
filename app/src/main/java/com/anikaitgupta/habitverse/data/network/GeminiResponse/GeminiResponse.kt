package com.anikaitgupta.habitverse.data.network.GeminiResponse

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
@Keep
@Serializable
data class GeminiResponse(val candidates:List<Candidate>)
@Keep
@Serializable
data class Candidate(val content:ContentResponse)
@Keep
@Serializable
data class ContentResponse(val parts:List<PartResponse>)
@Keep
@Serializable
data class PartResponse(val text: String)


