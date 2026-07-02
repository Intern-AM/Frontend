package com.speehive.speehiveaihub.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speehive.speehiveaihub.ui.theme.PulseGreen
import com.speehive.speehiveaihub.ui.theme.PulseAmber
import com.speehive.speehiveaihub.ui.theme.TextPrimary
import com.speehive.speehiveaihub.ui.theme.TextMuted
import com.speehive.speehiveaihub.ui.theme.CardBorder

@Composable
fun CircularScoreIndicator(
    score: Int,
    modifier: Modifier = Modifier,
    size: Int = 54
) {
    val color = if (score >= 80) PulseGreen else PulseAmber
    
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            // Background track
            drawArc(
                color = CardBorder.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (score / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontSize = (size / 3).sp,
                color = TextPrimary
            )
            Text(
                text = "AI",
                style = MaterialTheme.typography.labelSmall,
                fontSize = (size / 6).sp,
                color = color
            )
        }
    }
}

@Composable
fun QualityScoreBar(
    label: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = score.toString(), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            // Track
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CardBorder.copy(alpha = 0.3f))
            )
            // Progress
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(score / 100f)
                    .background(TextMuted)
            )
        }
    }
}
