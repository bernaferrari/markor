package com.bernaferrari.remarkor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ConfettiShape {
    RECTANGLE, CIRCLE, TRIANGLE
}

/**
 * Confetti celebration animation for delightful micro-interactions.
 * Triggers on task completion, first note creation, etc.
 */
@Composable
fun ConfettiCelebration(
    trigger: Boolean,
    durationMs: Long = 3000,
    particleCount: Int = 50,
    colors: List<Color> = listOf(
        Color(0xFFFF6B6B),  // Red
        Color(0xFF4ECDC4),  // Teal
        Color(0xFFFFE66D),  // Yellow
        Color(0xFF95E1D3),  // Mint
        Color(0xFFF38181),  // Coral
        Color(0xFFAA96DA),  // Purple
        Color(0xFFFCBAD3),  // Pink
    ),
    onFinished: () -> Unit = {}
) {
    var isPlaying by remember(trigger) { mutableStateOf(trigger) }

    // Particles state
    data class ConfettiParticle(
        val color: Color,
        val startX: Float,
        val startY: Float,
        val velocityX: Float,
        val velocityY: Float,
        val rotation: Float,
        val rotationSpeed: Float,
        val size: Float,
        val shape: ConfettiShape
    )

    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                color = colors.random(),
                startX = Random.nextFloat(),
                startY = 1.1f,
                velocityX = Random.nextFloat() * 2f - 1f,
                velocityY = -Random.nextFloat() * 2f - 1f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 360f - 180f,
                size = Random.nextFloat() * 8f + 4f,
                shape = ConfettiShape.values().random()
            )
        }
    }

    // Animation progress
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            isPlaying = true
            runTimedAnimation(durationMs) { progress = it }
            isPlaying = false
            onFinished()
        }
    }

    AnimatedVisibility(
        visible = isPlaying,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            particles.forEach { particle ->
                // Physics: gravity affects Y velocity over time
                val gravity = 2f * progress
                val currentVelocityY = particle.velocityY + gravity

                // Position based on progress
                val x = particle.startX + particle.velocityX * progress * 0.5f
                val y = particle.startY + currentVelocityY * progress * 0.5f

                // Only draw if on screen
                if (y >= -0.1f && y <= 1.1f && x >= -0.1f && x <= 1.1f) {
                    val drawX = x * canvasWidth
                    val drawY = y * canvasHeight
                    val currentRotation = particle.rotation + particle.rotationSpeed * progress

                    rotate(currentRotation, Offset(drawX, drawY)) {
                        when (particle.shape) {
                            ConfettiShape.RECTANGLE -> {
                                drawRect(
                                    color = particle.color,
                                    topLeft = Offset(
                                        drawX - particle.size / 2,
                                        drawY - particle.size / 4
                                    ),
                                    size = Size(particle.size, particle.size / 2),
                                    alpha = 1f - progress * 0.3f
                                )
                            }

                            ConfettiShape.CIRCLE -> {
                                drawCircle(
                                    color = particle.color,
                                    radius = particle.size / 2,
                                    center = Offset(drawX, drawY),
                                    alpha = 1f - progress * 0.3f
                                )
                            }

                            ConfettiShape.TRIANGLE -> {
                                // Simple triangle as a rotated rectangle
                                drawRect(
                                    color = particle.color,
                                    topLeft = Offset(
                                        drawX - particle.size / 4,
                                        drawY - particle.size / 2
                                    ),
                                    size = Size(particle.size / 2, particle.size),
                                    alpha = 1f - progress * 0.3f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sparkle effect for subtle celebrations.
 * Lighter weight than confetti, good for smaller moments.
 */
@Composable
fun SparkleEffect(
    trigger: Boolean,
    centerOffset: Offset = Offset(0.5f, 0.5f),
    sparkleCount: Int = 8,
    color: Color = Color(0xFFFFD700),
    onFinished: () -> Unit = {}
) {
    var isPlaying by remember(trigger) { mutableStateOf(trigger) }
    var progress by remember { mutableStateOf(0f) }

    data class Sparkle(
        val angle: Float,
        val distance: Float,
        val delay: Float,
        val size: Float
    )

    val sparkles = remember(sparkleCount) {
        List(sparkleCount) { i ->
            Sparkle(
                angle = (360f / sparkleCount) * i,
                distance = Random.nextFloat() * 30f + 20f,
                delay = Random.nextFloat() * 0.2f,
                size = Random.nextFloat() * 4f + 2f
            )
        }
    }

    LaunchedEffect(trigger) {
        if (trigger) {
            isPlaying = true
            runTimedAnimation(600L) { progress = it }
            isPlaying = false
            onFinished()
        }
    }

    AnimatedVisibility(
        visible = isPlaying,
        enter = fadeIn(tween(100)),
        exit = fadeOut(tween(100))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = centerOffset.x * size.width
            val centerY = centerOffset.y * size.height

            sparkles.forEach { sparkle ->
                // Only show after delay
                if (progress > sparkle.delay) {
                    val adjustedProgress = (progress - sparkle.delay) / (1f - sparkle.delay)

                    // Ease out curve
                    val easedProgress = 1f - (1f - adjustedProgress) * (1f - adjustedProgress)

                    val distance = sparkle.distance * easedProgress
                    val angleRad = sparkle.angle * (PI.toFloat() / 180f)

                    val x = centerX + cos(angleRad) * distance
                    val y = centerY + sin(angleRad) * distance

                    // Fade out
                    val alpha = 1f - adjustedProgress

                    // Draw star shape
                    drawCircle(
                        color = color,
                        radius = sparkle.size * (1f - adjustedProgress * 0.5f),
                        center = Offset(x, y),
                        alpha = alpha
                    )
                }
            }
        }
    }
}

/**
 * Success checkmark animation for completed actions.
 */
@Composable
fun SuccessCheckmark(
    visible: Boolean,
    size: androidx.compose.ui.unit.Dp = 64.dp,
    color: Color = Color(0xFF34A853),
    onAnimationComplete: () -> Unit = {}
) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            runTimedAnimation(400L) { progress = it }
            delay(200)
            onAnimationComplete()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(tween(200), initialScale = 0.5f) + fadeIn(tween(200)),
        exit = fadeOut(tween(100))
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 4.dp.toPx()
            val canvasSize = size.toPx()
            val center = Offset(canvasSize / 2, canvasSize / 2)
            val radius = canvasSize / 2 - strokeWidth

            // Draw circle background
            drawCircle(
                color = color,
                radius = radius,
                center = center,
                alpha = 0.2f
            )

            // Draw checkmark with progress
            val checkProgress = (progress * 2f).coerceIn(0f, 1f)
            if (checkProgress > 0) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    // Checkmark points (relative to center)
                    val p1 = Offset(center.x - radius * 0.3f, center.y)
                    val p2 = Offset(center.x - radius * 0.05f, center.y + radius * 0.35f)
                    val p3 = Offset(center.x + radius * 0.35f, center.y - radius * 0.25f)

                    moveTo(p1.x, p1.y)

                    if (checkProgress <= 0.5f) {
                        // First segment
                        val segmentProgress = checkProgress * 2f
                        val endX = p1.x + (p2.x - p1.x) * segmentProgress
                        val endY = p1.y + (p2.y - p1.y) * segmentProgress
                        lineTo(endX, endY)
                    } else {
                        // Complete first segment
                        lineTo(p2.x, p2.y)

                        // Second segment
                        val segmentProgress = (checkProgress - 0.5f) * 2f
                        val endX = p2.x + (p3.x - p2.x) * segmentProgress
                        val endY = p2.y + (p3.y - p2.y) * segmentProgress
                        lineTo(endX, endY)
                    }
                }

                drawPath(
                    path = path,
                    color = color,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }
        }
    }
}

/**
 * Bounce animation for interactive elements.
 */
@Composable
fun BounceAnimation(
    trigger: Boolean,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (trigger) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}

private suspend fun runTimedAnimation(durationMs: Long, onProgress: (Float) -> Unit) {
    var elapsed = 0L
    while (elapsed < durationMs) {
        onProgress(elapsed.toFloat() / durationMs)
        delay(FRAME_DELAY_MS)
        elapsed += FRAME_DELAY_MS
    }
    onProgress(1f)
}

private const val FRAME_DELAY_MS = 16L

/**
 * Shake animation for error feedback.
 */
@Composable
fun ShakeAnimation(
    trigger: Boolean,
    intensity: Float = 10f,
    content: @Composable () -> Unit
) {
    var offset by remember { mutableStateOf(0f) }
    var isShaking by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            isShaking = true
            repeat(4) { i ->
                offset = if (i % 2 == 0) intensity else -intensity
                delay(50)
            }
            offset = 0f
            isShaking = false
        }
    }

    Box(
        modifier = Modifier.graphicsLayer {
            translationX = offset
        }
    ) {
        content()
    }
}

/**
 * Task completion celebration - combines effects.
 */
@Composable
fun TaskCompletionCelebration(
    trigger: Boolean,
    onComplete: () -> Unit = {}
) {
    var showConfetti by remember { mutableStateOf(false) }

    // Show checkmark first, then confetti
    var showCheckmark by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            showCheckmark = true
            delay(300)
            showConfetti = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showCheckmark) {
            SuccessCheckmark(
                visible = showCheckmark,
                onAnimationComplete = {
                    showCheckmark = false
                }
            )
        }

        ConfettiCelebration(
            trigger = showConfetti,
            durationMs = 2000,
            particleCount = 30,
            onFinished = {
                showConfetti = false
                onComplete()
            }
        )
    }
}
