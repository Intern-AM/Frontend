package com.speehive.speehiveaihub.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ZoomableImageDialog(
    imageUrl: String?,
    onDismiss: () -> Unit
) {
    if (imageUrl.isNullOrBlank()) return

    val coroutineScope = rememberCoroutineScope()

    // Smooth pinch and double-tap zoom
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    // Dialog Entrance Animation state
    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateTrigger = true
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialogScale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "dialogAlpha"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp)
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                    alpha = animatedAlpha
                )
                .clickable { onDismiss() }, // Dismiss when clicking Card surface outside the image
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    coroutineScope.launch {
                                        if (scale.value > 1f) {
                                            launch { scale.animateTo(1f, spring()) }
                                            launch { offsetX.animateTo(0f, spring()) }
                                            launch { offsetY.animateTo(0f, spring()) }
                                        } else {
                                            launch { scale.animateTo(2.5f, spring()) }
                                            launch { offsetX.animateTo(0f, spring()) }
                                            launch { offsetY.animateTo(0f, spring()) }
                                        }
                                    }
                                },
                                onTap = {
                                    // Consume single tap on the image to prevent dismissing
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                coroutineScope.launch {
                                    val newScale = (scale.value * zoom).coerceIn(1f, 5f)
                                    scale.snapTo(newScale)
                                    if (newScale > 1f) {
                                        offsetX.snapTo(offsetX.value + pan.x)
                                        offsetY.snapTo(offsetY.value + pan.y)
                                    } else {
                                        offsetX.snapTo(0f)
                                        offsetY.snapTo(0f)
                                    }
                                }
                            }
                        }
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Zoomable Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale.value,
                                scaleY = scale.value,
                                translationX = offsetX.value,
                                translationY = offsetY.value
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
