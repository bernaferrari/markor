package com.bernaferrari.remarkor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.default_
import markor.shared.generated.resources.note_color
import markor.shared.generated.resources.personalize_note_description
import markor.shared.generated.resources.selected
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColorSelectionSheet(
    currentColor: Int?,
    showCurrentSelection: Boolean = true,
    onColorSelected: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        null, // Default
        0xFFFFAFA3.toInt(), // Red
        0xFFF39F76.toInt(), // Orange
        0xFFFFF8B8.toInt(), // Yellow
        0xFFE2F6D3.toInt(), // Green
        0xFFB4DDD3.toInt(), // Teal
        0xFFD4E4ED.toInt(), // Blue
        0xFFAECCDC.toInt(), // Dark Blue
        0xFFD3BFDB.toInt(), // Purple
        0xFFF6E2DD.toInt(), // Pink
        0xFFE9E3D4.toInt(), // Brown
        0xFFEFEFF1.toInt()  // Gray
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                stringResource(Res.string.note_color),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                stringResource(Res.string.personalize_note_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(56.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(colors) { colorInt ->
                    val color =
                        if (colorInt != null) Color(colorInt) else MaterialTheme.colorScheme.surfaceVariant
                    val isSelected = showCurrentSelection && currentColor == colorInt

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(colorInt) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (colorInt == null) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Palette,
                                contentDescription = stringResource(Res.string.default_),
                                tint = if (isSelected) selectionIconTint(color) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(Res.string.selected),
                                tint = selectionIconTint(color),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun selectionIconTint(background: Color): Color {
    return if (background.luminance() > 0.55f) {
        Color(0xFF1A1A1A)
    } else {
        Color.White
    }
}
