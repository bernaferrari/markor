package net.gsantner.markor.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.gsantner.markor.ui.viewmodel.SettingsViewModel
import net.gsantner.markor.ui.components.RenameDialog
import net.gsantner.markor.ui.theme.MarkorTheme
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val showLineNumbers by viewModel.showLineNumbers.collectAsState()
    val wordWrap by viewModel.wordWrap.collectAsState()
    val autoFormat by viewModel.autoFormat.collectAsState()
    val editorFontSize by viewModel.editorFontSize.collectAsState()
    
    val fileBrowserShowHidden by viewModel.fileBrowserShowHidden.collectAsState()
    val fileBrowserShowExt by viewModel.fileBrowserShowExt.collectAsState()
    val fileBrowserSortOrder by viewModel.fileBrowserSortOrder.collectAsState()
    val fileBrowserFolderFirst by viewModel.fileBrowserFolderFirst.collectAsState()
    
    val notebookDirectory by viewModel.notebookDirectory.collectAsState()
    val quickNotePath by viewModel.quickNotePath.collectAsState()
    val todoFilePath by viewModel.todoFilePath.collectAsState()

    // Simplified content for embedding in MainScreen tab
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MarkorTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(MarkorTheme.spacing.extraLarge)
    ) {
            // File Browser Section
            SettingsSection(title = "File Browser", icon = Icons.Default.FolderOpen) {
                SwitchSettingItem(
                    title = "Show Hidden Files",
                    subtitle = "Files starting with .",
                    checked = fileBrowserShowHidden,
                    onCheckedChange = { viewModel.setFileBrowserShowHidden(it) }
                )
                SwitchSettingItem(
                    title = "Folders First",
                    subtitle = "Show folders before files",
                    checked = fileBrowserFolderFirst,
                    onCheckedChange = { viewModel.setFileBrowserFolderFirst(it) }
                )
                
                // Sort Order - Segmented Button
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Sort By",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = fileBrowserSortOrder == "name",
                            onClick = { viewModel.setFileBrowserSortOrder("name") },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Name")
                        }
                        SegmentedButton(
                            selected = fileBrowserSortOrder == "date",
                            onClick = { viewModel.setFileBrowserSortOrder("date") },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Date")
                        }
                    }
                }
            }
            
            // Editor Section
            SettingsSection(title = "Editor", icon = Icons.Default.Edit) {
                SwitchSettingItem(
                    title = "Line Numbers",
                    subtitle = "Display line numbers",
                    checked = showLineNumbers,
                    onCheckedChange = { viewModel.setShowLineNumbers(it) }
                )
                SwitchSettingItem(
                    title = "Word Wrap",
                    subtitle = "Wrap text to fit screen",
                    checked = wordWrap,
                    onCheckedChange = { viewModel.setWordWrap(it) }
                )
                SwitchSettingItem(
                    title = "Auto Format",
                    subtitle = "Format while typing",
                    checked = autoFormat,
                    onCheckedChange = { viewModel.setAutoFormat(it) }
                )
                
                // Font Size - Slider
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Font Size",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${editorFontSize}sp",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = editorFontSize.toFloat(),
                        onValueChange = { viewModel.setEditorFontSize(it.toInt()) },
                        valueRange = 12f..24f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Storage Section
            var editingStorageKey by remember { mutableStateOf<String?>(null) }
            var currentStorageValue by remember { mutableStateOf("") }

            if (editingStorageKey != null) {
                RenameDialog(
                    currentName = currentStorageValue,
                    onDismiss = { editingStorageKey = null },
                    onConfirm = { newValue ->
                        when(editingStorageKey) {
                            "notebook" -> viewModel.setNotebookDirectory(newValue)
                            "quicknote" -> viewModel.setQuickNotePath(newValue)
                            "todo" -> viewModel.setTodoFilePath(newValue)
                        }
                        editingStorageKey = null
                    }
                )
            }

            SettingsSection(title = "Storage", icon = Icons.Default.SdStorage) {
                ClickableSettingItem(
                    title = "Notebook Directory",
                    subtitle = notebookDirectory.ifEmpty { "Default (Documents/Markor)" },
                    onClick = { 
                        editingStorageKey = "notebook"
                        currentStorageValue = notebookDirectory
                    }
                )
                ClickableSettingItem(
                    title = "QuickNote Path",
                    subtitle = quickNotePath.ifEmpty { "Default (QuickNote.md)" },
                    onClick = { 
                        editingStorageKey = "quicknote"
                        currentStorageValue = quickNotePath
                    }
                )
                ClickableSettingItem(
                    title = "Todo Path",
                    subtitle = todoFilePath.ifEmpty { "Default (todo.txt)" },
                    onClick = { 
                        editingStorageKey = "todo"
                        currentStorageValue = todoFilePath
                    }
                )
            }
             
            // About Section
            SettingsSection(title = "About", icon = Icons.Default.Info) {
                InfoSettingItem(
                    title = "Version",
                    value = "2.15.2-Expressive"
                )
            }
            
            // Add extra spacer to ensure last item is visible above bottom bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = MarkorTheme.spacing.medium, bottom = MarkorTheme.spacing.medium)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(MarkorTheme.spacing.medium))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        ElevatedCard(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = MarkorTheme.elevation.level0),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = MarkorTheme.spacing.medium)) {
                content()
            }
        }
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)) 
        },
        supportingContent = { 
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) 
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun InfoSettingItem(
    title: String,
    value: String
) {
    ListItem(
        headlineContent = { 
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)) 
        },
        supportingContent = { 
            Text(
                value, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            ) 
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun ClickableSettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)) 
        },
        supportingContent = { 
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) 
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
