package net.gsantner.markor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import net.gsantner.markor.ui.theme.MarkorTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Wraps a file card with swipe-to-action functionality.
 * - Swipe right: Toggle favorite (star)
 * - Swipe left: Delete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableFileCard(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hapticHelper = rememberHapticHelper()
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe right = favorite
                    hapticHelper.performHeavyClick()
                    onToggleFavorite()
                    false // Don't dismiss, just toggle
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe left = delete (confirm first)
                    hapticHelper.performHeavyClick()
                    onDelete()
                    false // Don't auto-dismiss, let parent handle
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { it * 0.3f } // 30% threshold
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            
            // Background colors based on swipe direction
            val color by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.tertiaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                },
                label = "bgColor"
            )
            
            val iconColor by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onTertiaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                    else -> Color.Transparent
                },
                label = "iconColor"
            )
            
            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                animationSpec = spring(),
                label = "iconScale"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Icon(
                            imageVector = if (isFavorite) Icons.Outlined.StarOutline else Icons.Filled.Star,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = iconColor,
                            modifier = Modifier.scale(scale).size(28.dp)
                        )
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = iconColor,
                            modifier = Modifier.scale(scale).size(28.dp)
                        )
                    }
                    else -> {}
                }
            }
        },
        content = { content() },
        modifier = modifier,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    )
}

/**
 * Inline star indicator for displaying favorite status on cards.
 */
@Composable
fun FavoriteIndicator(
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    if (isFavorite) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f),
            modifier = modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
