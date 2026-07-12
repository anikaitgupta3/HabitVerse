package com.anikaitgupta.habitverse.data.network

import com.anikaitgupta.habitverse.domain.ChatMessage
import com.anikaitgupta.habitverse.toGeminiInputData
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class GeminiApiTest {
    lateinit var mockWebServer: MockWebServer
    lateinit var geminiApi: GeminiApi

    @Before
    fun setup(){
        mockWebServer = MockWebServer()
        val retroJson = Json { ignoreUnknownKeys = true }
        geminiApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(retroJson.asConverterFactory("application/json".toMediaType()))
            .build().create(GeminiApi::class.java)
    }
    @Test
    fun testGetResponse() = runTest{
        val mockResponse = MockResponse()
        mockResponse.setBody("""{"candidates": []}""")
        mockWebServer.enqueue(mockResponse)

        val response = geminiApi.getTipsForHabit("abc", listOf<ChatMessage>(ChatMessage("user","How to improve sleeping")).toGeminiInputData())
        mockWebServer.takeRequest()
        Assert.assertEquals(true, response.body()!!.candidates.isEmpty())
    }

    @Test
    fun `testGetResponse returns success and correct data`() = runTest {
        val jsonResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [{ "text": "Try to maintain a consistent sleep schedule." }]
                  }
                }
              ]
            }
        """.trimIndent()
        val mockResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)
        mockWebServer.enqueue(mockResponse)

        val response = geminiApi.getTipsForHabit("key", listOf(ChatMessage("user", "sleep")).toGeminiInputData())

        Assert.assertTrue(response.isSuccessful)
        Assert.assertEquals("Try to maintain a consistent sleep schedule.", response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text)
    }
    @Test
    fun testGetResponse_returnError() = runTest{
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(404)
        mockResponse.setBody("Something went wrong")
        mockWebServer.enqueue(mockResponse)

        val response = geminiApi.getTipsForHabit("key", listOf(ChatMessage("user", "sleep")).toGeminiInputData())
        mockWebServer.takeRequest()

        Assert.assertEquals(false, response.isSuccessful)
        Assert.assertEquals(404, response.code())
    }

    @After
    fun tearDown(){
        mockWebServer.shutdown()
    }


}