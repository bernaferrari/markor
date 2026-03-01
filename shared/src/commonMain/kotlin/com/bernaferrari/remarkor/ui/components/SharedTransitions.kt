package com.bernaferrari.remarkor.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Local provider for shared transition scope.
 * Note: Navigation 3 doesn't provide this by default, so we use a fallback implementation.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * Local provider for AnimatedVisibilityScope.
 * Note: Navigation 3 doesn't provide this by default, so we use a fallback implementation.
 */
@OptIn(ExperimentalAnimationApi::class)
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Shared transition keys for consistent animation between screens.
 */
object SharedTransitionKeys {
    fun fileCard(filePath: String): String = "file_card_$filePath"
    fun fileTitle(filePath: String): String = "file_title_$filePath"
    fun fabNew(): String = "fab_new"
    fun newNoteDialog(): String = "new_note_dialog"
}

data class SharedElementState(
    val offset: IntOffset = IntOffset.Zero,
    val size: IntSize = IntSize.Zero,
    val isAnimating: Boolean = false
)

val LocalSharedElementState = compositionLocalOf { SharedElementState() }

/**
 * A shared element container.
 *
 * If native shared transitions are unavailable in the current navigation host,
 * fallback rendering is static to avoid spring/jump artifacts.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedElementContainer(
    key: String,
    isSource: Boolean,
    useSharedBounds: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalAnimatedVisibilityScope.current

    if (sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            val sharedContentState = rememberSharedContentState(key = key)
            Box(
                modifier = if (useSharedBounds) {
                    modifier.sharedBounds(
                        sharedContentState = sharedContentState,
                        animatedVisibilityScope = animatedScope,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 90,
                                easing = LinearOutSlowInEasing
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = 90,
                                easing = FastOutLinearInEasing
                            )
                        ),
                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                            contentScale = ContentScale.FillBounds,
                            alignment = Alignment.Center
                        )
                    )
                } else {
                    modifier.sharedElement(
                        sharedContentState = sharedContentState,
                        animatedVisibilityScope = animatedScope
                    )
                }
            ) {
                content()
            }
        }
    } else {
        // Navigation 3 currently doesn't provide AnimatedVisibilityScope here,
        // so keep fallback static to avoid bounce/jump artifacts on list/grid relayout.
        Box(
            modifier = modifier
                .onGloballyPositioned { coordinates ->
                    // Keep this so we can reintroduce real fallback transitions later.
                    coordinates.positionInRoot()
                    coordinates.size
                }
        ) {
            content()
        }
    }
}

/**
 * Simpler shared element transition for smaller elements (like icons, text).
 * Uses smooth scale and fade animation.
 */
@Composable
fun SimpleSharedElement(
    key: String,
    isSource: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = when {
            !isSource -> 1f
            else -> 0.95f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "simple_shared_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isSource) 0.9f else 1f,
        animationSpec = tween(150),
        label = "simple_shared_alpha"
    )

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
    ) {
        content()
    }
}
