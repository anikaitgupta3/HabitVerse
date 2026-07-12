package com.anikaitgupta.habitverse.data.db

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun fromLocalTime() {
        val timeToConvert = LocalTime.of(14, 30)
        val result = converters.fromLocalTime(timeToConvert)
        assertEquals("14:30",result)
    }

    @Test
    fun toLocalTime() {
        val timeToConvert = "14:30"
        val result = converters.toLocalTime(timeToConvert)
        assertEquals(LocalTime.of(14,30),result)
    }

}