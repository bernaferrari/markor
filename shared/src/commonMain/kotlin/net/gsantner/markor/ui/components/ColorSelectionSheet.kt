package net.gsantner.markor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSelectionSheet(
    currentColor: Int?,
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

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Color",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(colors) { colorInt ->
                    val color = if (colorInt != null) Color(colorInt) else MaterialTheme.colorScheme.surfaceVariant
                    val isSelected = currentColor == colorInt
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(colorInt) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (colorInt == null) {
                            Icon(
                                imageVector = Icons.Default.Check, // Placeholder icon for "None"
                                contentDescription = "Default",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp).alpha(if (currentColor == null) 1f else 0f)
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
