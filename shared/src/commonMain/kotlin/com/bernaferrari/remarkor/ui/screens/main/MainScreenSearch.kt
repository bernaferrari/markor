package com.bernaferrari.remarkor.ui.screens.main

import com.bernaferrari.remarkor.ui.icons.MaterialSymbols

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bernaferrari.remarkor.ui.components.BackHandler
import com.bernaferrari.remarkor.ui.components.CreateFileDialog
import com.bernaferrari.remarkor.ui.components.EmptyState
import com.bernaferrari.remarkor.ui.components.rememberAdaptiveLayoutInfo
import com.bernaferrari.remarkor.ui.components.rememberHapticHelper
import com.bernaferrari.remarkor.ui.components.resolveNoteSurfaceColor
import com.bernaferrari.remarkor.ui.viewmodel.FileBrowserViewModel
import com.bernaferrari.remarkor.ui.viewmodel.FileFilterMode
import com.bernaferrari.remarkor.ui.viewmodel.MainViewModel
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.all
import markor.shared.generated.resources.archive
import markor.shared.generated.resources.back
import markor.shared.generated.resources.choose_file_to_view_edit
import markor.shared.generated.resources.clear_query
import markor.shared.generated.resources.clear_selection
import markor.shared.generated.resources.create_note_to_start_searching
import markor.shared.generated.resources.delete
import markor.shared.generated.resources.favorites
import markor.shared.generated.resources.name
import markor.shared.generated.resources.no_matches
import markor.shared.generated.resources.no_notes_yet
import markor.shared.generated.resources.note_color
import markor.shared.generated.resources.notebook
import markor.shared.generated.resources.oldest_first
import markor.shared.generated.resources.recent_first
import markor.shared.generated.resources.search_notes
import markor.shared.generated.resources.search_notes_label
import markor.shared.generated.resources.select_all
import markor.shared.generated.resources.select_file
import markor.shared.generated.resources.selected
import markor.shared.generated.resources.settings
import markor.shared.generated.resources.sort
import markor.shared.generated.resources.switch_to_grid_view
import markor.shared.generated.resources.switch_to_list_view
import markor.shared.generated.resources.trash
import markor.shared.generated.resources.try_different_search_term
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SearchScreen(
    query: String,
    results: List<com.bernaferrari.remarkor.domain.repository.FileInfo>,
    /** Note colors by path, so each result keeps the color it has in the notebook. */
    noteColors: Map<String, Int?> = emptyMap(),
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onOpenNote: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            MaterialSymbols.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                title = {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .padding(vertical = 4.dp),
                        placeholder = { Text(stringResource(Res.string.search_notes)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                },
                actions = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                MaterialSymbols.Filled.Close,
                                contentDescription = stringResource(Res.string.clear_query)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = if (query.isBlank()) stringResource(Res.string.no_notes_yet) else stringResource(
                        Res.string.no_matches
                    ),
                    subtitle = if (query.isBlank()) stringResource(Res.string.create_note_to_start_searching) else stringResource(
                        Res.string.try_different_search_term
                    ),
                    icon = MaterialSymbols.Filled.SearchOff
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = results,
                    key = { it.path.toString() }
                ) { file ->
                    val highlight = MaterialTheme.colorScheme.tertiaryContainer
                    val onHighlight = MaterialTheme.colorScheme.onTertiaryContainer
                    val title = remember(file.name, query, highlight) {
                        highlightMatches(file.name.substringBeforeLast('.'), query, highlight, onHighlight)
                    }
                    val snippets = remember(file.preview, query, highlight) {
                        searchSnippets(file.preview.orEmpty(), query)
                            .map { highlightMatches(it, query, highlight, onHighlight) }
                    }
                    val noteColor = noteColors[file.path.toString()]
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = resolveNoteSurfaceColor(noteColor, MaterialTheme.colorScheme)
                        ),
                        headlineContent = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            if (snippets.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    snippets.forEach { line ->
                                        Text(
                                            text = line,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenNote(file.path.toString()) }
                    )
                }
            }
        }
    }
}

/** Strips list, task, heading and quote markers so a matching line reads as plain text. */
internal fun plainSearchLine(line: String): String =
    line.trim()
        .replace(Regex("^#{1,6}\\s+"), "")
        .replace(Regex("^>\\s*"), "")
        .replace(Regex("^(?:[-*+]|\\d+\\.)\\s+"), "")
        .replace(Regex("^\\[[ xX]]\\s+"), "")
        .replace("*", "")
        .replace("`", "")
        .trim()

/**
 * Up to [maxLines] lines of [text] that contain [query] (case-insensitive), cleaned of Markdown
 * markers. With no match in the text (a name-only match, or no query), the first lines instead.
 */
internal fun searchSnippets(text: String, query: String, maxLines: Int = 2): List<String> {
    val lines = text.lines().map(::plainSearchLine).filter { it.isNotEmpty() }
    val needle = query.trim()
    val matches = if (needle.isEmpty()) emptyList() else lines.filter { it.contains(needle, ignoreCase = true) }
    if (matches.isEmpty()) return lines.take(maxLines)
    return matches.take(maxLines).map { aroundMatch(it, needle) }
}

/** Starts a long line a few words before the match, so the match stays visible on one line. */
private fun aroundMatch(line: String, needle: String, lead: Int = 24): String {
    val at = line.indexOf(needle, ignoreCase = true)
    if (at <= lead) return line
    val cut = line.lastIndexOf(' ', at - lead) + 1
    return if (cut <= 0) line else "…" + line.substring(cut)
}

/** [text] with every case-insensitive occurrence of [query] marked like a highlighter. */
internal fun highlightMatches(text: String, query: String, background: Color, foreground: Color): AnnotatedString {
    val needle = query.trim()
    return buildAnnotatedString {
        append(text)
        if (needle.isEmpty()) return@buildAnnotatedString
        var start = text.indexOf(needle, ignoreCase = true)
        while (start >= 0) {
            addStyle(
                SpanStyle(background = background, color = foreground, fontWeight = FontWeight.SemiBold),
                start,
                start + needle.length
            )
            start = text.indexOf(needle, start + needle.length, ignoreCase = true)
        }
    }
}

@Composable
internal fun EmptyStateMessage(title: String, subtitle: String) {
    EmptyState(
        title = title,
        subtitle = subtitle,
        icon = MaterialSymbols.Filled.Info
    )
}
