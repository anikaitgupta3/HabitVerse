package com.example.habitverse.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.habitverse.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    analyticsUiState: HabitAnalyticsUiState,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompletionRateCard(analyticsUiState)
            RecoveryRateCard(analyticsUiState)
            StreakCard(analyticsUiState)
        }
    }
}

@Composable
private fun CompletionRateCard(state: HabitAnalyticsUiState) {
    val rate = state.completionRateThisWeek
    val trend = state.trend
    val trendPositive = trend >= 0

    AnalyticsCard {
        CardHeader(
            iconRes = R.drawable.bar_chart_24px, // replace with your icon
            iconBg = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
            subtitle = "This week",
            title = "Completion rate"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            BigNumber(value = "${(rate)}%")
            //CompletionRing(progress = rate, size = 72.dp)
        }
        CardDivider()
        TrendRow(
            trend = "${if (trendPositive) "+" else ""}${(trend)}%",
            isPositive = trendPositive,
            label = "vs last week"
        )
    }
}

@Composable
private fun RecoveryRateCard(state: HabitAnalyticsUiState) {
    AnalyticsCard {
        CardHeader(
            iconRes = R.drawable.restart_alt_24px, // replace with your icon
            iconBg = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
            subtitle = "Resilience",
            title = "Recovery rate"
        )
        BigNumber(value = "${state.recoveryRate}")
        Text(
            text = "Your average recovery rate",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StreakCard(state: HabitAnalyticsUiState) {
    AnalyticsCard {
        CardHeader(
            iconRes = R.drawable.local_fire_department_24px, // replace with your icon
            iconBg = Color(0xFFFAEEDA),
            iconTint = Color(0xFF854F0B),
            subtitle = "Consistency",
            title = "Current streak"
        )
        BigNumber(value = "${state.streakCount}")
        Text(
            text = "Habits in a perfect 3-day streak",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- Shared components ---

@Composable
private fun AnalyticsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

@Composable
private fun CardHeader(
    iconRes: Int,
    iconBg: Color,
    iconTint: Color,
    subtitle: String,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(MaterialTheme.shapes.small)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun BigNumber(value: String) {
    Text(
        text = value,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun TrendRow(trend: String, isPositive: Boolean, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TrendBadge(text = trend, isPositive = isPositive)
    }
}

@Composable
private fun TrendBadge(text: String, isPositive: Boolean) {
    val bg = if (isPositive) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.errorContainer
    val fg = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onErrorContainer
    val icon = if (isPositive) R.drawable.arrow_upward_24px else R.drawable.arrow_downward_24px

    Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = fg, modifier = Modifier.size(12.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = fg)
        }
    }
}

//@Composable
//private fun CompletionRing(progress: Double, size: Dp) {
//    val color = MaterialTheme.colorScheme.primary
//    val trackColor = MaterialTheme.colorScheme.surfaceVariant
//    Canvas(modifier = Modifier.size(size)) {
//        val stroke = 6.dp.toPx()
//        val diameter = this.size.minDimension - stroke
//        val topLeft = Offset(stroke / 2, stroke / 2)
//        val arcSize = Size(diameter, diameter)
//        drawArc(
//            color = trackColor,
//            startAngle = 0f, sweepAngle = 360f, useCenter = false,
//            topLeft = topLeft, size = arcSize,
//            style = Stroke(width = stroke, cap = StrokeCap.Round)
//        )
//        drawArc(
//            color = color,
//            startAngle = -90f, sweepAngle = (progress * 360f).toFloat(), useCenter = false,
//            topLeft = topLeft, size = arcSize,
//            style = Stroke(width = stroke, cap = StrokeCap.Round)
//        )
//    }
//}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AnalyticsScreen(analyticsUiState: HabitAnalyticsUiState,onBackClick:()-> Unit){
//    Scaffold(topBar = {
//        CenterAlignedTopAppBar(title = {Text(text = "Analytics")},navigationIcon = {
//            IconButton(onClick = { onBackClick() }) {
//                Icon(
//                    painter = painterResource(R.drawable.baseline_arrow_back_24),
//                    contentDescription = "Back Button"
//                )
//            }
//        })
//
//    }) { innerPadding->
//        Surface(modifier = Modifier.padding(innerPadding).fillMaxSize()){
//            Column{
//                Card(modifier = Modifier.padding(10.dp).height(75.dp).fillMaxWidth(),colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
//                )) {
//                    //var currentPercentage by rememberSaveable{ analyticsUiState.logCountThisWeek/analyticsUiState.habitCountThisWeek }
//                    Column(modifier = Modifier.padding(10.dp)){
//                        Text(text = "${analyticsUiState.completionRateThisWeek} % habits completed this week", modifier = Modifier.weight(0.6f))
//                        Spacer(modifier = Modifier.weight(0.1f))
//                        Text(text="${analyticsUiState.trend} % from last week", modifier = Modifier.weight(0.3f))
//                    }
//                }
//                Card(modifier = Modifier.padding(10.dp).height(75.dp).fillMaxWidth(),colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
//                )) {
//                    //var currentPercentage by rememberSaveable{ analyticsUiState.logCountThisWeek/analyticsUiState.habitCountThisWeek }
//                    Column(modifier = Modifier.padding(10.dp)){
//                        Text(text = "${analyticsUiState.recoveryRate} is your average recovery rate", modifier = Modifier.weight(0.6f))
//                        Spacer(modifier = Modifier.weight(0.1f))
//                        Text(text="The average time taken to restart a habit after a break", modifier = Modifier.weight(0.3f))
//                    }
//                }
//                Card(modifier = Modifier.padding(10.dp).height(75.dp).fillMaxWidth(),colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
//                )) {
//                    //var currentPercentage by rememberSaveable{ analyticsUiState.logCountThisWeek/analyticsUiState.habitCountThisWeek }
//                    Text(text = "${analyticsUiState.streakCount} habits in a perfect 3 day streak", modifier = Modifier.padding(10.dp))
//                }
//            }
//        }
//    }
//}
@Preview
@Composable
fun AnalyticsPreview(){
    AnalyticsScreen(HabitAnalyticsUiState(),{})
}