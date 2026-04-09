package com.example.habitverse.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTimePicker(timePickerState: TimePickerState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp), // Adjust height to fit your layout
        contentAlignment = Alignment.Center
    ) {
        TimePicker(
            state = timePickerState,
            modifier = Modifier.graphicsLayer(
                scaleX = 0.7f, // Zoom out to 70%
                scaleY = 0.7f,
                transformOrigin = TransformOrigin.Center
            )
        )
    }
}