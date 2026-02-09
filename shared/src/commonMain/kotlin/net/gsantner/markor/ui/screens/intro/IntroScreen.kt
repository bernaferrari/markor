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
import net.gsantner.markor.ui.theme.MarkorTheme
import net.gsantner.markor.ui.viewmodel.IntroViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun IntroScreen(
    onIntroFinished: () -> Unit,
    systemInternalFilesDir: String,
    viewModel: IntroViewModel = koinViewModel()
) {
    val pagerState = rememberPagerState { 5 }
    val scope = rememberCoroutineScope()
    var requestPermissions by remember { mutableStateOf(false) }

    // Permission handling only happens if user opted for external storage
    net.gsantner.markor.ui.components.HandleStoragePermissions(
        onRequest = requestPermissions,
        onGranted = {
            viewModel.setStorageMode(isExternal = true, internalPath = systemInternalFilesDir) 
            viewModel.markIntroSeen()
            onIntroFinished()
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
                    if (page == 4) {
                        StorageSelectionPage(
                            onPrivateSelected = {
                                viewModel.setStorageMode(isExternal = false, internalPath = systemInternalFilesDir)
                                viewModel.markIntroSeen()
                                onIntroFinished()
                            },
                            onSharedSelected = {
                                requestPermissions = true
                            }
                        )
                    } else {
                        IntroPage(page)
                    }
                }

                // Bottom Controls (Hidden on last page to force selection)
                if (pagerState.currentPage < 4) {
                    IntroBottomBar(
                        pagerState = pagerState,
                        onBack = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        },
                        onNext = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
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
            text = "Choose Storage Location",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Select where you want to keep your notes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        StorageOptionCard(
            title = "Private Storage",
            description = "Notes are kept inside the app. No permissions required. Secure and simple.",
            icon = Icons.Default.Lock,
            onClick = onPrivateSelected,
            recommended = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        StorageOptionCard(
            title = "Shared Storage",
            description = "Notes are stored in your Documents folder. Accessible by other apps and sync tools.",
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (recommended) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    if (recommended) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "RECOMMENDED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
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
        0 -> Triple("Welcome to Markor", "The best markdown editor for Android. Powerful, open-source, and offline-first.", Icons.Filled.Edit) to MaterialTheme.colorScheme.primary
        1 -> Triple("Notebook", "Notebook is the home of your files. Markor loads this folder by default when the app is started.", Icons.Filled.Folder) to MaterialTheme.colorScheme.secondary
        2 -> Triple("QuickNote", "The fastest way to write down notes. Automatically saved in Markdown format.", Icons.Filled.FlashOn) to MaterialTheme.colorScheme.tertiary
        3 -> Triple("To-Do List", "Manage your tasks with todo.txt format. Simple, flexible, and portable.", Icons.Filled.CheckCircle) to MaterialTheme.colorScheme.primary
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
                        "Back",
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
            repeat(5) { index ->
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
                Text("Next", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
