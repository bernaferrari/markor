package net.gsantner.markor.ui.screens.intro

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import markor.shared.generated.resources.*
import net.gsantner.markor.ui.components.supportsSharedStorageMode
import net.gsantner.markor.ui.theme.MarkorTheme
import net.gsantner.markor.ui.viewmodel.IntroViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun IntroScreen(
    onIntroFinished: () -> Unit,
    systemInternalFilesDir: String,
    viewModel: IntroViewModel = koinViewModel()
) {
    val supportsSharedStorage = remember { supportsSharedStorageMode() }
    val pageCount = if (supportsSharedStorage) 4 else 3
    val pagerState = rememberPagerState { pageCount }
    val scope = rememberCoroutineScope()
    var requestPermissions by remember { mutableStateOf(false) }

    // Permission handling only happens if user opted for external storage
    net.gsantner.markor.ui.components.HandleStoragePermissions(
        onRequest = requestPermissions,
        onGranted = {
            scope.launch {
                viewModel.setStorageMode(isExternal = true, internalPath = systemInternalFilesDir)
                onIntroFinished()
                requestPermissions = false
            }
        },
        onDenied = {
             requestPermissions = false
             // Effectively falls back or stays on screen?
             // For now, reset. User must explicitly choose Private to proceed without perms.
        }
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    if (supportsSharedStorage && page == pageCount - 1) {
                        StorageSelectionPage(
                            onPrivateSelected = {
                                scope.launch {
                                    viewModel.setStorageMode(isExternal = false, internalPath = systemInternalFilesDir)
                                    onIntroFinished()
                                }
                            },
                            onSharedSelected = {
                                requestPermissions = true
                            }
                        )
                    } else {
                        IntroPage(page)
                    }
                }

                // Bottom controls:
                // - shared-capable platforms: hide on final page to force explicit storage selection
                // - iOS/private-only: show and finish setup on the last page
                val showBottomBar = if (supportsSharedStorage) {
                    pagerState.currentPage < pageCount - 1
                } else {
                    true
                }
                if (showBottomBar) {
                    IntroBottomBar(
                        pagerState = pagerState,
                        onBack = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        },
                        onNext = {
                            scope.launch {
                                if (!supportsSharedStorage && pagerState.currentPage == pageCount - 1) {
                                    viewModel.setStorageMode(isExternal = false, internalPath = systemInternalFilesDir)
                                    onIntroFinished()
                                } else {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageSelectionPage(
    onPrivateSelected: () -> Unit,
    onSharedSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Save,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(Res.string.choose_storage_location),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(Res.string.select_storage_location),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        StorageOptionCard(
            title = stringResource(Res.string.private_storage),
            description = stringResource(Res.string.storage_internal_description),
            icon = Icons.Default.Lock,
            onClick = onPrivateSelected,
            recommended = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        StorageOptionCard(
            title = stringResource(Res.string.shared_storage),
            description = stringResource(Res.string.storage_external_description),
            icon = Icons.Default.FolderShared,
            onClick = onSharedSelected,
            recommended = false
        )
    }
}

@Composable
private fun StorageOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    recommended: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (recommended) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (recommended) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (recommended) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (recommended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (recommended) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (recommended) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stringResource(Res.string.recommended),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (recommended) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (recommended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun IntroPage(page: Int) {
    val (data, color) = when (page) {
        0 -> Triple(
            stringResource(Res.string.welcome_to_markor),
            stringResource(Res.string.intro_description),
            Icons.Filled.Edit
        ) to MaterialTheme.colorScheme.primary
        1 -> Triple(
            stringResource(Res.string.write_quickly),
            stringResource(Res.string.intro_description),
            Icons.AutoMirrored.Filled.NoteAdd
        ) to MaterialTheme.colorScheme.secondary
        2 -> Triple(
            stringResource(Res.string.stay_organized),
            stringResource(Res.string.stay_organized_description),
            Icons.Filled.Search
        ) to MaterialTheme.colorScheme.tertiary
        else -> Triple("", "", Icons.Filled.Close) to Color.Gray
    }
    val (title, description, icon) = data

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(48.dp),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = color
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun IntroBottomBar(
    pagerState: PagerState,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Box(modifier = Modifier.width(80.dp)) {
            if (pagerState.currentPage > 0) {
                TextButton(onClick = onBack) {
                    Text(
                        stringResource(Res.string.back),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pagerState.pageCount) { index ->
                val active = pagerState.currentPage == index
                val width by animateDpAsState(if (active) 24.dp else 8.dp, label = "indicatorWidth")
                val alpha by animateFloatAsState(if (active) 1f else 0.3f, label = "indicatorAlpha")
                
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                )
            }
        }

        // Next Button
        Box(modifier = Modifier.width(120.dp), contentAlignment = Alignment.CenterEnd) {
             FilledTonalButton(
                onClick = onNext,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(Res.string.next), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
