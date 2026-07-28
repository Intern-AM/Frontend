package com.speehive.speehiveaihub.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.ui.theme.*

@Composable
fun SlidingStatusFilter(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("All", "Pending", "Generated", "Rejected", "Completed")
    
    val icons = listOf(
        Icons.Default.Layers,
        Icons.Default.Schedule,
        Icons.Default.AutoAwesome,
        Icons.Default.Cancel,
        Icons.Default.CheckCircle
    )

    val index = when (selectedStatus) {
        "All" -> 0
        "Pending" -> 1
        "Generated" -> 2
        "Rejected" -> 3
        "Completed" -> 4
        else -> 0
    }

    val activeColor = when (selectedStatus) {
        "All" -> Color(0xFFE2E8F0)
        "Pending" -> PulseAmber
        "Generated" -> PulseBlue
        "Rejected" -> PulseRed
        "Completed" -> PulseGreen
        else -> Color(0xFFE2E8F0)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(CardSurface.copy(alpha = 0.5f))
                .border(1.dp, CardBorder, RoundedCornerShape(28.dp))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val tabWidth = maxWidth / 5

                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * index,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "offset"
                )

                val indicatorColor by animateColorAsState(
                    targetValue = indicatorColorSelected(selectedStatus, activeColor),
                    animationSpec = tween(durationMillis = 250),
                    label = "color"
                )

                // Sliding indicator background pill
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(indicatorColor)
                )

                // Clickable tabs
                Row(modifier = Modifier.fillMaxSize()) {
                    options.forEachIndexed { i, option ->
                        val isSelected = index == i
                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                if (option == "All") Color(0xFF1C1B1F) else Color.White
                            } else {
                                TextSecondary
                            },
                            animationSpec = tween(durationMillis = 200),
                            label = "iconColor"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onStatusSelected(option)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icons[i],
                                contentDescription = option,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(16.dp)) {
            val tabWidth = maxWidth / 5

            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * index,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "labelOffset"
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedStatus.uppercase(),
                    color = if (selectedStatus == "All") TextSecondary else activeColor,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

private fun indicatorColorSelected(status: String, activeColor: Color): Color {
    return activeColor
}
