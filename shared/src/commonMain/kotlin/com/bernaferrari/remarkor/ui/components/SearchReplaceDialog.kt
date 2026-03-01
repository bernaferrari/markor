package com.bernaferrari.remarkor.ui.components
import markor.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import com.bernaferrari.remarkor.ui.theme.MarkorTheme
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchReplaceDialog(
    onDismiss: () -> Unit,
    onFindNext: (String) -> Unit,
    onReplace: (String, String) -> Unit,
    onReplaceAll: (String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var isReplaceVisible by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier.width(320.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.find),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Toggle Replace Mode
                     TextButton(onClick = { isReplaceVisible = !isReplaceVisible }) {
                        Icon(
                            imageVector = if (isReplaceVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore, 
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isReplaceVisible) "Hide Replace" else "Replace")
                    }
                }
                
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Find text...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                AnimatedVisibility(visible = isReplaceVisible) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TextField(
                            value = replaceQuery,
                            onValueChange = { replaceQuery = it },
                            placeholder = { Text("Replace with...") },
                            leadingIcon = { Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { onReplace(searchQuery, replaceQuery) },
                                enabled = searchQuery.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Replace", maxLines = 1)
                            }
                            FilledTonalButton(
                                onClick = { onReplaceAll(searchQuery, replaceQuery) },
                                enabled = searchQuery.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("All", maxLines = 1)
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = { onFindNext(searchQuery) },
                        enabled = searchQuery.isNotEmpty(),
                        modifier = Modifier.weight(1.5f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Find Next", maxLines = 1)
                    }
                }
            }
        }
    }
}
