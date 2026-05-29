package com.weightflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged

private val ITEM_HEIGHT = 24.dp
private const val PADDING_ITEMS = 2

@Composable
fun WheelPicker(
    items: List<Int>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onScrollTick: () -> Unit = {},
) {
    val clampedInitial = initialIndex.coerceIn(0, items.lastIndex)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = clampedInitial)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val accent = MaterialTheme.colorScheme.primary
    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground
    val centerIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex + PADDING_ITEMS }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { firstIndex ->
                val selectedIndex = firstIndex + PADDING_ITEMS
                if (selectedIndex in items.indices) {
                    onItemSelected(items[selectedIndex])
                }
                onScrollTick()
            }
    }

    Box(modifier = modifier.height(ITEM_HEIGHT * (PADDING_ITEMS * 2 + 1))) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(PADDING_ITEMS) { Spacer(Modifier.height(ITEM_HEIGHT)) }
            itemsIndexed(items) { index, value ->
                val distance = kotlin.math.abs(index - centerIndex)
                val textSize = when (distance) { 0 -> 24.sp; 1 -> 15.sp; else -> 11.sp }
                val alpha = when (distance) { 0 -> 1f; 1 -> 0.35f; 2 -> 0.15f; else -> 0.07f }
                Box(Modifier.height(ITEM_HEIGHT), contentAlignment = Alignment.Center) {
                    Text(
                        text = "$value",
                        fontSize = textSize,
                        fontWeight = FontWeight.Bold,
                        color = onBg.copy(alpha = alpha),
                    )
                }
            }
            items(PADDING_ITEMS) { Spacer(Modifier.height(ITEM_HEIGHT)) }
        }

        // Top fade
        Canvas(Modifier.fillMaxWidth().height(36.dp).align(Alignment.TopCenter)) {
            drawRect(brush = Brush.verticalGradient(listOf(bg, Color.Transparent)))
        }
        // Bottom fade
        Canvas(Modifier.fillMaxWidth().height(36.dp).align(Alignment.BottomCenter)) {
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, bg)))
        }
        // Selection hairlines
        Canvas(Modifier.fillMaxSize()) {
            val halfItem = ITEM_HEIGHT.toPx() / 2
            val cy = size.height / 2
            listOf(cy - halfItem, cy + halfItem).forEach { y ->
                drawLine(
                    color = accent.copy(alpha = 0.22f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }
    }
}
