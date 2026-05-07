package com.weightflow.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
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
import com.weightflow.domain.isValidWeightKg
import com.weightflow.ui.theme.WFTokens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingEntry by rememberSaveable { mutableStateOf<Long?>(null) }
    var editInput by rememberSaveable { mutableStateOf("") }

    val currentState = uiState as? HistoryUiState.HasData

    editingEntry?.let { editId ->
        val entry = currentState?.entries?.firstOrNull { it.id == editId }
        val unitLabel = when (currentState?.weightUnit) {
            com.weightflow.domain.WeightUnit.LBS -> "lbs"
            com.weightflow.domain.WeightUnit.ST  -> "st"
            else                                 -> "kg"
        }
        AlertDialog(
            onDismissRequest = { editingEntry = null; editInput = "" },
            title = { Text("Edit entry") },
            text = {
                OutlinedTextField(
                    value = editInput,
                    onValueChange = { editInput = it },
                    label = { Text("Weight ($unitLabel)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val raw = editInput.toDoubleOrNull()
                        if (raw != null && entry != null) {
                            val kg = when (currentState?.weightUnit) {
                                com.weightflow.domain.WeightUnit.LBS ->
                                    com.weightflow.domain.WeightConverter.lbsToKg(raw)
                                else -> raw
                            }
                            if (kg.isValidWeightKg()) viewModel.onEditEntry(entry.id, kg)
                        }
                        editingEntry = null
                        editInput = ""
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null; editInput = "" }) {
                    Text("Cancel")
                }
            },
        )
    }

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
                onEdit = { entry ->
                    editInput = "%.1f".format(
                        when ((uiState as HistoryUiState.HasData).weightUnit) {
                            com.weightflow.domain.WeightUnit.LBS ->
                                com.weightflow.domain.WeightConverter.kgToLbs(entry.weightKg)
                            else -> entry.weightKg
                        }
                    )
                    editingEntry = entry.id
                },
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
private fun DataView(
    state: HistoryUiState.HasData,
    onDelete: (Long) -> Unit,
    onEdit: (HistoryEntryDisplay) -> Unit,
) {
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
                    onDelete = { onDelete(entry.id) },
                    onEdit = { onEdit(entry) },
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
    onDelete: () -> Unit,
    onEdit: () -> Unit,
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

    val accent = MaterialTheme.colorScheme.primary
    val rowBg = if (isToday) accent.copy(alpha = 0.04f) else Color.Transparent

    // Split "70.5 kg" → numText="70.5", unitText="kg"
    val numText = remember(entry.weightDisplay) {
        entry.weightDisplay
            .replace(" kg", "").replace(" lbs", "").replace(" st", "")
            .trim()
    }
    val unitText = remember(entry.weightDisplay) {
        when {
            entry.weightDisplay.contains("kg")  -> "kg"
            entry.weightDisplay.contains("lbs") -> "lbs"
            entry.weightDisplay.contains("st")  -> "st"
            else -> ""
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBg)
                .clickable(onClick = onEdit)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .semantics {
                    contentDescription = "${entry.weightDisplay} on ${entry.dateDisplay}"
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Day number block — 48dp wide
            Column(modifier = Modifier.width(48.dp)) {
                Text(
                    text = dayNum,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isToday) accent else MaterialTheme.colorScheme.onBackground,
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                    lineHeight = 22.sp,
                )
                Text(
                    text = dayName,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = WFTokens.Text3,
                )
            }

            // Weight number + unit sub-label
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = numText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                    lineHeight = 22.sp,
                )
                if (unitText.isNotEmpty()) {
                    Text(
                        text = unitText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = WFTokens.Text3,
                    )
                }
            }

            // Delta chip + delete button
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
}

// ── Delta Chip ────────────────────────────────────────────────────────────────

@Composable
private fun DeltaChip(display: String?, isDown: Boolean?) {
    val (bg, borderColor, chipColor, label) = when {
        display == null -> return
        isDown == null  -> Quad(
            WFTokens.Elevated, WFTokens.Text3.copy(alpha = 0.2f), WFTokens.Text3, "— $display",
        )
        display == "0.0 kg" || display == "0.0 lbs" || display == "0.0 st" -> Quad(
            WFTokens.Elevated, WFTokens.Text3.copy(alpha = 0.2f), WFTokens.Text3, "— same",
        )
        isDown -> Quad(
            WFTokens.Success.copy(alpha = 0.10f),
            WFTokens.Success.copy(alpha = 0.20f),
            WFTokens.Success,
            "▼ $display",
        )
        else -> Quad(
            WFTokens.Danger.copy(alpha = 0.10f),
            WFTokens.Danger.copy(alpha = 0.20f),
            WFTokens.Danger,
            "▲ $display",
        )
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = chipColor,
        )
    }
}

/** Tiny data holder to make destructuring four values readable in [DeltaChip]. */
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private operator fun <A, B, C, D> Quad<A, B, C, D>.component1() = first
private operator fun <A, B, C, D> Quad<A, B, C, D>.component2() = second
private operator fun <A, B, C, D> Quad<A, B, C, D>.component3() = third
private operator fun <A, B, C, D> Quad<A, B, C, D>.component4() = fourth
