package com.anikaitgupta.habitverse.data.network

sealed interface GeminiResult {
    data class Success(val output: String): GeminiResult
    data class Error(val message: String): GeminiResult
    object Loading: GeminiResult
}