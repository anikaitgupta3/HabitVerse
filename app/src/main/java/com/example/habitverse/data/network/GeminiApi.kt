package com.example.habitverse.data.network

import com.example.habitverse.data.network.GeminiInputData.GeminiInputData
import com.example.habitverse.data.network.GeminiResponse.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GeminiApi {
    @POST("v1beta/models/gemini-3-flash-preview:generateContent")
    suspend fun getTipsForHabit(@Header("x-goog-api-key") apiKey: String,@Body geminiInputData: GeminiInputData): Response<GeminiResponse>
}