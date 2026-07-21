package com.speehive.speehiveaihub.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.ui.theme.*

import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush

enum class DashboardView {
    ADMIN,
    REVIEWER
}

@Composable
fun ViewModeSwitcher(
    currentView: DashboardView,
    onViewSelected: (DashboardView) -> Unit,
    modifier: Modifier = Modifier
) {
    val views = DashboardView.values()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x280F172A)
            ),
        shape = RoundedCornerShape(26.dp),
        color = ElevatedSurface,
        border = BorderStroke(1.5.dp, Color(0xFFCBD5E1))
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            val segmentWidth = maxWidth / views.size

            val indicatorOffset by animateDpAsState(
                targetValue = segmentWidth * currentView.ordinal,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "indicatorOffset"
            )

            // Floating 3D green pill with light green gradient and drop shadow
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(21.dp),
                        spotColor = PulseGreenBright.copy(alpha = 0.45f),
                        ambientColor = PulseGreenBright.copy(alpha = 0.2f)
                    )
                    .clip(RoundedCornerShape(21.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF34D399), // Top highlight
                                PulseGreenBright   // Normal light green
                            )
                        )
                    )
            )

            // Segment touch targets & labels
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                views.forEach { view ->
                    val isSelected = view == currentView
                    val itemInteractionSource = remember { MutableInteractionSource() }

                    val (label, icon) = when (view) {
                        DashboardView.ADMIN -> "Admin" to Icons.Default.Person
                        DashboardView.REVIEWER -> "Reviewer" to Icons.Default.Visibility
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .deep3DPress(
                                interactionSource = itemInteractionSource,
                                onClick = {
                                    if (!isSelected) {
                                        onViewSelected(view)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "$label View",
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) Color.White else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
