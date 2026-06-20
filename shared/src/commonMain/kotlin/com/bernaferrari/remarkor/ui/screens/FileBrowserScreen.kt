package com.bernaferrari.remarkor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.remarkor.domain.repository.FileInfo
import com.bernaferrari.remarkor.domain.repository.IAssetRepository
import com.bernaferrari.remarkor.ui.components.AssetManagerSheet
import com.bernaferrari.remarkor.ui.components.BackHandler
import com.bernaferrari.remarkor.ui.components.CreateFolderDialog
import com.bernaferrari.remarkor.ui.components.DeleteDialog
import com.bernaferrari.remarkor.ui.components.EmptyState
import com.bernaferrari.remarkor.ui.components.FavoriteIndicator
import com.bernaferrari.remarkor.ui.components.FileActionSheet
import com.bernaferrari.remarkor.ui.components.FileGridItem
import com.bernaferrari.remarkor.ui.components.HapticHelper
import com.bernaferrari.remarkor.ui.components.LabelsDialog
import com.bernaferrari.remarkor.ui.components.RenameDialog
import com.bernaferrari.remarkor.ui.components.ShareDialog
import com.bernaferrari.remarkor.ui.components.SharedElementContainer
import com.bernaferrari.remarkor.ui.components.SharedTransitionKeys
import com.bernaferrari.remarkor.ui.components.SwipeableFileCard
import com.bernaferrari.remarkor.ui.components.rememberHapticHelper
import com.bernaferrari.remarkor.ui.components.renderGridMarkdown
import com.bernaferrari.remarkor.ui.components.resolveMarkdownColorPalette
import com.bernaferrari.remarkor.ui.components.resolveNoteSurfaceColor
import com.bernaferrari.remarkor.ui.screens.filebrowser.FileBrowserContent
import com.bernaferrari.remarkor.ui.viewmodel.FileBrowserViewModel
import com.bernaferrari.remarkor.ui.viewmodel.FileFilterMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import markor.shared.generated.resources.Res
import markor.shared.generated.resources.back_to_with_arg
import markor.shared.generated.resources.create_new
import markor.shared.generated.resources.delete_permanently
import markor.shared.generated.resources.favorite
import markor.shared.generated.resources.folder
import markor.shared.generated.resources.more
import markor.shared.generated.resources.more_create_options
import markor.shared.generated.resources.no_favorites_yet
import markor.shared.generated.resources.no_favorites_yet_description
import markor.shared.generated.resources.notebook_is_empty
import markor.shared.generated.resources.notebook_is_empty_description
import markor.shared.generated.resources.restore
import markor.shared.generated.resources.trash_is_empty
import markor.shared.generated.resources.trash_is_empty_description
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FileBrowserScreen(
    initialPath: String?,
    onNavigateToEditor: (String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: FileBrowserViewModel = koinViewModel(),
    isGridView: Boolean = true
) {
    FileBrowserContent(
        initialPath = initialPath,
        onNavigateToEditor = onNavigateToEditor,
        onNavigateBack = onNavigateBack,
        viewModel = viewModel,
        isGridView = isGridView
    )
}
