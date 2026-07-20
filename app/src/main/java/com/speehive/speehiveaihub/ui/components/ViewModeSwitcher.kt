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

enum class DashboardView {
    ADMIN,
    DESIGNER,
    REVIEWER
}

@Composable
fun ViewModeSwitcher(
    currentView: DashboardView,
    onViewSelected: (DashboardView) -> Unit,
    modifier: Modifier = Modifier
) {
    val views = DashboardView.values()

    val indicatorColor = PulseGreenBright

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorder)
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

            // Animated floating green pill
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(21.dp))
                    .background(indicatorColor)
            )

            // Segment touch targets & labels
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                views.forEach { view ->
                    val isSelected = view == currentView

                    val (label, icon) = when (view) {
                        DashboardView.ADMIN -> "Admin" to Icons.Default.Person
                        DashboardView.DESIGNER -> "Designer" to Icons.Default.Brush
                        DashboardView.REVIEWER -> "Reviewer" to Icons.Default.Visibility
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isSelected) {
                                    onViewSelected(view)
                                }
                            },
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
