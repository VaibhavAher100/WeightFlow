package com.weightflow.ui.logentry

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.components.WheelPicker
import com.weightflow.ui.components.rememberWFHaptics
import com.weightflow.ui.theme.WFTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val WHOLE_PARTS_KG  = (20..300).toList()
private val WHOLE_PARTS_LBS = (44..660).toList()
private val DECIMAL_PARTS   = (0..9).toList()

@Composable
fun LogEntrySheet(
    viewModel: LogEntryViewModel,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = rememberWFHaptics()
    val accent = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LogEntryEvent.Saved, is LogEntryEvent.Dismissed -> onDismiss()
            }
        }
    }

    val lastKg = uiState.lastLoggedWeightKg ?: 80.0
    val initialDisplayVal = when (uiState.weightUnit) {
        WeightUnit.KG  -> lastKg
        WeightUnit.LBS -> WeightConverter.kgToLbs(lastKg)
        WeightUnit.ST  -> lastKg
    }
    val wholeParts = if (uiState.weightUnit == WeightUnit.LBS) WHOLE_PARTS_LBS else WHOLE_PARTS_KG
    val initialWhole = initialDisplayVal.toInt().coerceIn(wholeParts.first(), wholeParts.last())
    val initialDecimal = ((initialDisplayVal - initialDisplayVal.toInt()) * 10).toInt().coerceIn(0, 9)

    var selectedWhole   by remember { mutableIntStateOf(initialWhole) }
    var selectedDecimal by remember { mutableIntStateOf(initialDecimal) }

    LaunchedEffect(selectedWhole, selectedDecimal, uiState.weightUnit) {
        viewModel.onWeightInput("$selectedWhole.$selectedDecimal")
    }

    val drumAlpha by animateFloatAsState(
        targetValue = if (uiState.isSaved) 0.12f else 1f,
        animationSpec = tween(250),
        label = "drumAlpha",
    )
    val numColor by animateColorAsState(
        targetValue = if (uiState.isNewPersonalLow) accent else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(250),
        label = "numColor",
    )

    LaunchedEffect(uiState.isNewPersonalLow) {
        if (uiState.isNewPersonalLow) haptics.celebrate()
    }

    val wholeInitialIndex = wholeParts.indexOf(initialWhole).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Handle
        Box(
            Modifier
                .padding(top = 14.dp)
                .width(36.dp)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
        )

        // ── Hero zone ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.13f), Color.Transparent),
                        center = Offset(size.width / 2, size.height),
                        radius = size.width * 0.5f,
                    ),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$selectedWhole.$selectedDecimal",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Black,
                    color = numColor,
                    letterSpacing = (-3).sp,
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                )
                val unitLabel = when (uiState.weightUnit) {
                    WeightUnit.KG  -> if (uiState.isNewPersonalLow) "New Low · Kilograms" else "Kilograms"
                    WeightUnit.LBS -> if (uiState.isNewPersonalLow) "New Low · Pounds" else "Pounds"
                    WeightUnit.ST  -> if (uiState.isNewPersonalLow) "New Low · Stone" else "Stone"
                }
                Box(
                    modifier = Modifier
                        .background(WFTokens.accentSoft(accent), RoundedCornerShape(999.dp))
                        .border(1.dp, WFTokens.accentBorder(accent), RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = unitLabel.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = accent,
                    )
                }
                uiState.lastLoggedWeightKg?.let { lastKgVal ->
                    val displayLast = when (uiState.weightUnit) {
                        WeightUnit.KG  -> "%.1f kg".format(lastKgVal)
                        WeightUnit.LBS -> "%.1f lbs".format(WeightConverter.kgToLbs(lastKgVal))
                        WeightUnit.ST  -> "%.1f kg".format(lastKgVal)
                    }
                    Text(
                        text = "Last logged $displayLast",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = WFTokens.Text3,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        // ── Drum zone ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(drumAlpha)
                .background(Color.White.copy(alpha = 0.02f))
                .padding(horizontal = 24.dp, vertical = 4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WheelPicker(
                    items = wholeParts,
                    initialIndex = wholeInitialIndex,
                    onItemSelected = { selectedWhole = it },
                    onScrollTick = { haptics.tick() },
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = ".",
                    fontSize = 20.sp,
                    color = WFTokens.Text3,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                WheelPicker(
                    items = DECIMAL_PARTS,
                    initialIndex = initialDecimal,
                    onItemSelected = { selectedDecimal = it },
                    onScrollTick = { haptics.tick() },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    listOf(WeightUnit.KG to "KG", WeightUnit.LBS to "LBS", WeightUnit.ST to "ST")
                        .forEach { (unit, label) ->
                            val isActive = unit == uiState.weightUnit
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = if (isActive) accent else WFTokens.Text3,
                                modifier = Modifier.padding(end = 10.dp),
                            )
                        }
                }
                Text(
                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d")),
                    fontSize = 9.sp,
                    color = WFTokens.Text3,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Save button ──
        val btnLabel = when {
            uiState.isSaved && uiState.isNewPersonalLow -> "New Low 🏆"
            uiState.isSaved                             -> "✓  Logged"
            uiState.isSaving                            -> "Saving…"
            else                                        -> "Save Entry"
        }
        val btnBg = if (uiState.isSaved) Color.Transparent else accent
        val btnTextColor = if (uiState.isSaved) accent else MaterialTheme.colorScheme.onPrimary
        val btnBorderMod = if (uiState.isSaved)
            Modifier.border(1.dp, WFTokens.accentBorder(accent), RoundedCornerShape(999.dp))
        else
            Modifier

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .then(btnBorderMod)
                .background(btnBg, RoundedCornerShape(999.dp))
                .clickable(enabled = uiState.isInputValid && !uiState.isSaving && !uiState.isSaved) {
                    haptics.confirm()
                    viewModel.onSave()
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = btnLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp,
                color = btnTextColor,
            )
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                fontSize = 11.sp,
                color = WFTokens.Danger,
                modifier = Modifier.padding(top = 8.dp, start = 20.dp, end = 20.dp),
            )
        }
    }
}
