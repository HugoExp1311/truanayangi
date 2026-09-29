package com.example.foodgacha.ui.roll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodgacha.data.db.model.ItemWithTags
import com.example.foodgacha.domain.reel.CaseMechanics
import com.example.foodgacha.domain.reel.RarityTier
import com.example.foodgacha.domain.reel.SpinProfile
import java.io.File
import kotlin.math.roundToInt
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RollScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: RollViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Gacha Roll") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !uiState.isSpinning) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // CS:GO Horizontal Reel Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFF0F0E13))
            ) {
                if (uiState.reelItems.isNotEmpty() && uiState.spinProfile != null) {
                    CsgoReelTrack(
                        items = uiState.reelItems,
                        targetIndex = uiState.winnerTargetIndex,
                        spinProfile = uiState.spinProfile!!,
                        isSpinning = uiState.isSpinning,
                        onTick = { viewModel.playItemScrollSound() },
                        onSpinFinished = { viewModel.onSpinComplete() }
                    )
                } else {
                    // Placeholder before first spin
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Press Roll to Spin the Reel",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Vertical Winning Center Line
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(280.dp)
                        .align(Alignment.Center)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFFFC837),
                                    Color(0xFFFF8008),
                                    Color(0xFFFFC837),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Spin CTA Button
            Button(
                onClick = { viewModel.prepareAndStartSpin() },
                enabled = !uiState.isSpinning && uiState.filteredItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Casino, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (uiState.isSpinning) "Spinning..." else "Roll Gacha",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pool Size: ${uiState.filteredItems.size} / ${uiState.items.size} item(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter by Tags
            if (uiState.allTags.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Filter by Tags (Optional):",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.allTags.forEach { tag ->
                            val isSelected = uiState.selectedTagIds.contains(tag.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleTagFilter(tag.id) },
                                label = { Text(tag.name) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // Winner Reveal Dialog
        if (uiState.showWinnerDialog && uiState.winnerItem != null) {
            val winner = uiState.winnerItem!!
            val context = LocalContext.current
            val coverFile = if (!winner.item.coverImageFileName.isNullOrBlank()) {
                File(winner.item.folderPath, winner.item.coverImageFileName)
            } else null

            AlertDialog(
                onDismissRequest = { viewModel.dismissWinnerDialog() },
                properties = DialogProperties(dismissOnClickOutside = true),
                title = {
                    Text(
                        text = "🎉 You Got!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val rarityTier = RarityTier.fromTier(winner.item.rarity)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .border(3.dp, rarityTier.color, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(coverFile)
                                    .size(500)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = winner.item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = rarityTier.color.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, rarityTier.color)
                        ) {
                            Text(
                                text = "${rarityTier.nameEn} • ${rarityTier.nameVi}",
                                color = rarityTier.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = winner.item.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        if (winner.item.author.isNotBlank()) {
                            Text(
                                text = "By ${winner.item.author}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (winner.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                winner.tags.forEach { tag ->
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(tag.name, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissWinnerDialog()
                            onNavigateToDetail(winner.item.id)
                        }
                    ) {
                        Text("View Details")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissWinnerDialog() }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun CsgoReelTrack(
    items: List<ItemWithTags>,
    targetIndex: Int,
    spinProfile: com.example.foodgacha.domain.reel.SpinProfile,
    isSpinning: Boolean,
    onTick: () -> Unit,
    onSpinFinished: () -> Unit
) {
    val density = LocalDensity.current
    val tileWidth = 200.dp
    val tileGap = 12.dp
    val stepDp = tileWidth + tileGap

    val tileWidthPx = with(density) { tileWidth.toPx() }
    val stepPx = with(density) { stepDp.toPx() }

    val animProgress = remember { Animatable(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewportWidthPx = constraints.maxWidth.toFloat()
        val centerOffsetPx = viewportWidthPx / 2f

        // Center index 2 is initial start
        val startSlot = 2
        val startStopFraction = 0.5
        val startOffsetPx = centerOffsetPx - (tileWidthPx * startStopFraction.toFloat()) - (startSlot * stepPx)

        val targetStopFraction = spinProfile.stopFraction
        val endOffsetPx = centerOffsetPx - (tileWidthPx * targetStopFraction.toFloat()) - (targetIndex * stepPx)

        LaunchedEffect(isSpinning) {
            if (isSpinning) {
                animProgress.snapTo(0f)
                val durationMs = spinProfile.durationMs
                val startTimeNanos = java.lang.System.nanoTime()
                var lastCell = kotlin.math.floor((startOffsetPx - centerOffsetPx) / stepPx).toInt()

                while (true) {
                    withFrameNanos { nowNanos ->
                        val elapsedMs = (nowNanos - startTimeNanos) / 1_000_000.0
                        val progress = (elapsedMs / durationMs).coerceIn(0.0, 1.0)
                        val eased = CaseMechanics.spinProgress(progress, spinProfile.friction).toFloat()
                        val currentPos = startOffsetPx + (endOffsetPx - startOffsetPx) * eased

                        // Web tick calculation: cell = floor((next - width / 2) / step)
                        val cell = kotlin.math.floor((currentPos - centerOffsetPx) / stepPx).toInt()
                        if (cell != lastCell) {
                            onTick()
                            lastCell = cell
                        }
                    }

                    val elapsedMs = (java.lang.System.nanoTime() - startTimeNanos) / 1_000_000.0
                    val p = (elapsedMs / durationMs).coerceIn(0.0, 1.0).toFloat()
                    animProgress.snapTo(p)

                    if (p >= 1f) {
                        break
                    }
                }
                onSpinFinished()
            }
        }

        val easedProgress = CaseMechanics.spinProgress(animProgress.value.toDouble(), spinProfile.friction).toFloat()
        val currentTrackOffsetPx = startOffsetPx + (endOffsetPx - startOffsetPx) * easedProgress

        // Render visible cards relative to currentTrackOffsetPx
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = currentTrackOffsetPx.roundToInt(), y = 0) }
        ) {
            items.forEachIndexed { index, itemWithTags ->
                val cardX = with(density) { (index * stepPx).toDp() }
                Box(
                    modifier = Modifier
                        .offset(x = cardX)
                        .width(tileWidth)
                        .height(260.dp)
                        .padding(vertical = 12.dp)
                ) {
                    ReelItemCard(itemWithTags = itemWithTags)
                }
            }
        }
    }
}


@Composable
fun ReelItemCard(itemWithTags: ItemWithTags) {
    val context = LocalContext.current
    val item = itemWithTags.item
    val coverFile = if (!item.coverImageFileName.isNullOrBlank()) {
        File(item.folderPath, item.coverImageFileName)
    } else null
    val rarityTier = RarityTier.fromTier(item.rarity)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .border(2.dp, rarityTier.color, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1D24))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(coverFile)
                        .size(400)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            if (item.author.isNotBlank()) {
                Text(
                    text = item.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom rarity accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(rarityTier.color, RoundedCornerShape(2.dp))
            )
        }
    }
}
