package net.gsantner.markor.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch

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
 * A shared element container that simulates shared element transitions using
 * position and size animation when native SharedTransitionScope is not available.
 *
 * Navigation 3 doesn't provide LocalSharedTransitionScope/LocalAnimatedVisibilityScope
 * like Navigation 2 does, so this fallback creates a convincing shared element effect
 * using bounds animation with proper easing.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedElementContainer(
    key: String,
    isSource: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalAnimatedVisibilityScope.current
    val coroutineScope = rememberCoroutineScope()

    var targetOffset by remember(key) { mutableStateOf(IntOffset.Zero) }
    var targetSize by remember(key) { mutableStateOf(IntSize.Zero) }
    var sourceOffset by remember(key) { mutableStateOf(IntOffset.Zero) }
    var sourceSize by remember(key) { mutableStateOf(IntSize.Zero) }
    var startPosition by remember(key) { mutableStateOf(false) }

    val offsetAnim = animateIntOffsetAsState(
        targetValue = if (startPosition && !isSource) targetOffset else sourceOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "shared_offset"
    )

    val sizeAnim = animateIntSizeAsState(
        targetValue = if (startPosition && !isSource) targetSize else sourceSize,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "shared_size"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            !startPosition -> 1f
            isSource -> 0.95f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "shared_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = when {
            !startPosition -> 1f
            isSource -> 0.8f
            else -> 1f
        },
        animationSpec = tween(200),
        label = "shared_alpha"
    )

    LaunchedEffect(key, isSource) {
        if (!isSource) {
            kotlinx.coroutines.delay(50)
            startPosition = true
        } else {
            startPosition = false
        }
    }

    if (sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            Box(
                modifier = modifier.sharedElement(
                    rememberSharedContentState(key = key),
                    animatedScope
                )
            ) {
                content()
            }
        }
    } else {
        Box(
            modifier = modifier
                .onGloballyPositioned { coordinates ->
                    if (isSource) {
                        sourceOffset = coordinates.positionInRoot().let { IntOffset(it.x.toInt(), it.y.toInt()) }
                        sourceSize = coordinates.size.let { IntSize(it.width, it.height) }
                    } else {
                        targetOffset = coordinates.positionInRoot().let { IntOffset(it.x.toInt(), it.y.toInt()) }
                        targetSize = coordinates.size.let { IntSize(it.width, it.height) }
                    }
                }
                .graphicsLayer {
                    translationX = (offsetAnim.value.x - sourceOffset.x).toFloat()
                    translationY = (offsetAnim.value.y - sourceOffset.y).toFloat()
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = if (isSource && startPosition) 1f / scale else 1f
                    scaleY = if (isSource && startPosition) 1f / scale else 1f
                }
            ) {
                content()
            }
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

