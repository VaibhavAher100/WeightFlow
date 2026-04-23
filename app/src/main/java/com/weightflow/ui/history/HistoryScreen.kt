package com.weightflow.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.ui.theme.WFTokens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (uiState) {
            is HistoryUiState.Loading -> LoadingView()
            is HistoryUiState.Empty   -> EmptyView()
            is HistoryUiState.HasData -> DataView(
                state = uiState as HistoryUiState.HasData,
                onDelete = { id -> viewModel.onDelete(id) },
            )
        }
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

// ── Empty ─────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "0",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No entries yet — log your first weight!",
            style = MaterialTheme.typography.bodyLarge,
            color = WFTokens.Text2,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Has Data ──────────────────────────────────────────────────────────────────

@Composable
private fun DataView(state: HistoryUiState.HasData, onDelete: (Long) -> Unit) {
    val monthFmt = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    val grouped = remember(state.entries) {
        state.entries.groupBy { entry ->
            Instant.ofEpochMilli(entry.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(monthFmt)
                .uppercase()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { SearchBar() }

        grouped.forEach { (month, entries) ->
            stickyHeader(key = month) {
                MonthHeader(label = month)
            }
            items(items = entries, key = { it.id }) { entry ->
                HistoryEntryRow(
                    entry = entry,
                    isLast = entry == entries.last(),
                    onDelete = { onDelete(entry.id) },
                )
            }
        }
    }
}

// ── Search Bar ────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = WFTokens.Text3,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "Search entries…",
            fontSize = 13.sp,
            color = WFTokens.Text3,
        )
    }
}

// ── Month Header ──────────────────────────────────────────────────────────────

@Composable
private fun MonthHeader(label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = WFTokens.Text3,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 6.dp),
    )
}

// ── Entry Row ─────────────────────────────────────────────────────────────────

@Composable
private fun HistoryEntryRow(
    entry: HistoryEntryDisplay,
    isLast: Boolean,
    onDelete: () -> Unit,
) {
    val zone = remember { ZoneId.systemDefault() }
    val dayFmt = remember { DateTimeFormatter.ofPattern("EEE") }
    val localDate = remember(entry.timestamp) {
        Instant.ofEpochMilli(entry.timestamp).atZone(zone).toLocalDate()
    }
    val isToday = remember(localDate) { localDate == LocalDate.now() }
    val dayNum = remember(localDate) { localDate.dayOfMonth.toString() }
    val dayName = remember(localDate, isToday) {
        if (isToday) "TODAY" else localDate.format(dayFmt).uppercase()
    }

    val rowBg = if (isToday)
        WFTokens.accentDim(MaterialTheme.colorScheme.primary)
    else
        Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .then(
                if (!isLast) Modifier.padding(bottom = 0.dp)
                else Modifier
            )
            .padding(horizontal = 18.dp, vertical = 11.dp)
            .semantics {
                contentDescription = "${entry.weightDisplay} on ${entry.dateDisplay}"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Date column — fixed 38dp
        Column(
            modifier = Modifier.width(38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = dayNum,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 24.sp,
            )
            Text(
                text = dayName,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = WFTokens.Text3,
            )
        }

        // Weight + notes
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.weightDisplay,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 22.sp,
            )
        }

        // Delta + delete
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DeltaChip(display = entry.deltaDisplay, isDown = entry.deltaIsDown)
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete entry for ${entry.dateDisplay}",
                    tint = WFTokens.Text3,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }

    // Divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(WFTokens.Border),
    )
}

// ── Delta Chip ────────────────────────────────────────────────────────────────

@Composable
private fun DeltaChip(display: String?, isDown: Boolean?) {
    val (bg, color, label) = when {
        display == null        -> return
        isDown == null         -> Triple(WFTokens.Elevated, WFTokens.Text3, "— $display")
        display == "0.0 kg" || display == "0.0 lbs" -> Triple(WFTokens.Elevated, WFTokens.Text3, "— same")
        isDown                 -> Triple(WFTokens.Success.copy(alpha = 0.12f), WFTokens.Success, "▼ $display")
        else                   -> Triple(WFTokens.Danger.copy(alpha = 0.12f), WFTokens.Danger, "▲ $display")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}
