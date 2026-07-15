package com.bernaferrari.remarkor.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * SharedTransitionScope from the app-level [androidx.compose.animation.SharedTransitionLayout].
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * AnimatedVisibilityScope for sharedBounds matching.
 * Grid cards: per-item AnimatedVisibility (Keep pattern).
 * Overlay: parent AnimatedVisibility when the note is open.
 * Full-screen editor: NavDisplay scope.
 */
@OptIn(ExperimentalAnimationApi::class)
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

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

@OptIn(ExperimentalSharedTransitionApi::class)
private val KeepBoundsTransform = BoundsTransform { _, _ ->
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
}

/** Matches Keep overlay Surface corner radius for clean container morph. */
private val KeepSharedShape = RoundedCornerShape(28.dp)

/**
 * Container-transform helper matching
 * [Android shared element docs](https://developer.android.com/develop/ui/compose/animation/shared-elements):
 * - [sharedBounds] for visually different content (list card ↔ expanded note)
 * - both ends under the same SharedTransitionLayout
 * - each end in its own AnimatedVisibility (source exits when destination enters)
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedElementContainer(
    key: String,
    isSource: Boolean,
    useSharedBounds: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalAnimatedVisibilityScope.current

    if (enabled && sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            val sharedContentState = rememberSharedContentState(key = key)
            Box(
                modifier = if (useSharedBounds) {
                    modifier.sharedBounds(
                        sharedContentState = sharedContentState,
                        animatedVisibilityScope = animatedScope,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        boundsTransform = KeepBoundsTransform,
                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                            contentScale = ContentScale.FillBounds,
                            alignment = Alignment.Center,
                        ),
                        // Match official snack sample: clip morph to rounded container.
                        clipInOverlayDuringTransition = OverlayClip(KeepSharedShape),
                        renderInOverlayDuringTransition = true,
                    )
                } else {
                    modifier.sharedElement(
                        sharedContentState = sharedContentState,
                        animatedVisibilityScope = animatedScope,
                        boundsTransform = KeepBoundsTransform,
                        renderInOverlayDuringTransition = true,
                    )
                }
            ) {
                content()
            }
        }
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}

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
