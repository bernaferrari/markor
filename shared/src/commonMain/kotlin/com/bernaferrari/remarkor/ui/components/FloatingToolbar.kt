package com.bernaferrari.remarkor.ui.components

import markor.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.bernaferrari.remarkor.ui.theme.MarkorTheme

/**
 * A compact floating toolbar that appears above text selection.
 * Shows common formatting actions (Bold, Italic, Code, Link).
 */
@Composable
fun FloatingSelectionToolbar(
    visible: Boolean,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onCode: () -> Unit,
    onLink: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticHelper = rememberHapticHelper()
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + scaleIn(
            initialScale = 0.9f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.95f)
    ) {
        Popup(
            alignment = Alignment.BottomCenter,
            offset = androidx.compose.ui.unit.IntOffset(0, -150),
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = false)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.inverseSurface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    FloatingToolbarButton(
                        icon = Icons.Default.FormatBold,
                        contentDescription = stringResource(Res.string.bold),
                        onClick = {
                            hapticHelper.performLightClick()
                            onBold()
                        }
                    )
                    FloatingToolbarButton(
                        icon = Icons.Default.FormatItalic,
                        contentDescription = stringResource(Res.string.italic),
                        onClick = {
                            hapticHelper.performLightClick()
                            onItalic()
                        }
                    )
                    FloatingToolbarButton(
                        icon = Icons.Default.Code,
                        contentDescription = "Code",
                        onClick = {
                            hapticHelper.performLightClick()
                            onCode()
                        }
                    )
                    
                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.2f))
                    )
                    
                    FloatingToolbarButton(
                        icon = Icons.Default.Link,
                        contentDescription = "Link",
                        onClick = {
                            hapticHelper.performLightClick()
                            onLink()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingToolbarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.inverseOnSurface
        ),
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * A focus mode overlay that dims non-current paragraphs.
 * Creates a typewriter-like focus experience.
 */
@Composable
fun FocusModeOverlay(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
        
        // The actual focus mode effect is applied within the editor
        // This overlay can add additional ambient effects
        AnimatedVisibility(
            visible = enabled,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            // Optional: Add ambient glow or vignette effect
            // For now, this is a placeholder for future enhancements
        }
    }
}

/**
 * Focus mode toolbar toggle button with animation.
 */
@Composable
fun FocusModeToggle(
    isFocusMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticHelper = rememberHapticHelper()
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocusMode) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            Color.Transparent,
        label = "focusBg"
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (isFocusMode) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        label = "focusIcon"
    )
    
    Surface(
        onClick = {
            hapticHelper.performLightClick()
            onToggle()
        },
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = if (isFocusMode) Icons.Default.CenterFocusStrong else Icons.Default.CenterFocusWeak,
                contentDescription = if (isFocusMode) "Exit Focus Mode" else "Enter Focus Mode",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Apply focus mode styling to paragraphs.
 * Returns alpha value for each paragraph based on whether it's the active one.
 * 
 * @param paragraphIndex The index of the paragraph being styled
 * @param currentParagraphIndex The index of the currently focused paragraph
 * @param isFocusMode Whether focus mode is enabled
 * @return Alpha value (0.0 to 1.0) for the paragraph
 */
fun calculateParagraphAlpha(
    paragraphIndex: Int,
    currentParagraphIndex: Int,
    isFocusMode: Boolean
): Float {
    if (!isFocusMode) return 1f
    
    val distance = kotlin.math.abs(paragraphIndex - currentParagraphIndex)
    return when (distance) {
        0 -> 1f // Current paragraph
        1 -> 0.6f // Adjacent paragraphs
        2 -> 0.35f // Two away
        else -> 0.2f // Further paragraphs
    }
}

/**
 * Determines the current paragraph index based on cursor position.
 */
fun getCurrentParagraphIndex(text: String, cursorPosition: Int): Int {
    if (text.isEmpty() || cursorPosition == 0) return 0
    
    val beforeCursor = text.substring(0, minOf(cursorPosition, text.length))
    return beforeCursor.count { it == '\n' }
}
