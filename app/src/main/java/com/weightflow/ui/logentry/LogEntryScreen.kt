package com.weightflow.ui.logentry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.theme.WFTokens
import java.time.format.DateTimeFormatter

@Composable
fun LogEntrySheet(
    viewModel: LogEntryViewModel,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val accent = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LogEntryEvent.Saved, is LogEntryEvent.Dismissed -> onDismiss()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "LOG WEIGHT",
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                color = WFTokens.Text2,
            )
            TextButton(onClick = viewModel::onDismiss) {
                Text("Cancel", color = WFTokens.Text2, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Hero card — weight input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(WFTokens.Elevated)
                .border(1.dp, WFTokens.Border, RoundedCornerShape(26.dp))
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ENTER WEIGHT",
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = WFTokens.Text3,
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    StepButton("−") {
                        val cur = uiState.weightInput.toDoubleOrNull() ?: 0.0
                        viewModel.onWeightInput("%.1f".format(maxOf(0.0, cur - 0.1)))
                    }

                    BasicTextField(
                        value = uiState.weightInput,
                        onValueChange = viewModel::onWeightInput,
                        textStyle = TextStyle(
                            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                            fontSize = 72.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(accent),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (uiState.isInputValid) viewModel.onSave() },
                        ),
                        modifier = Modifier.width(160.dp),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.Center) {
                                if (uiState.weightInput.isEmpty()) {
                                    Text(
                                        text = "0.0",
                                        style = TextStyle(
                                            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                                            fontSize = 72.sp,
                                            color = WFTokens.Text3,
                                            textAlign = TextAlign.Center,
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                                inner()
                            }
                        },
                    )

                    StepButton("+") {
                        val cur = uiState.weightInput.toDoubleOrNull() ?: 0.0
                        viewModel.onWeightInput("%.1f".format(cur + 0.1))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Unit indicator (display-only — change via Settings)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(WFTokens.Card)
                        .border(1.dp, WFTokens.Border, RoundedCornerShape(8.dp)),
                ) {
                    listOf(WeightUnit.KG, WeightUnit.LBS, WeightUnit.ST).forEach { unit ->
                        val isSelected = unit == uiState.weightUnit
                        Box(
                            modifier = Modifier
                                .background(if (isSelected) accent else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = when (unit) {
                                    WeightUnit.KG  -> "kg"
                                    WeightUnit.LBS -> "lbs"
                                    WeightUnit.ST  -> "st"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) onPrimary else WFTokens.Text3,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Date row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(WFTokens.Card)
                .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "DATE",
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    color = WFTokens.Text3,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = uiState.selectedDate.format(
                        DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"),
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null,
                tint = WFTokens.Text3,
                modifier = Modifier.size(18.dp),
            )
        }

        if (uiState.errorMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = uiState.errorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = viewModel::onSave,
            enabled = uiState.isInputValid && !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = onPrimary,
                disabledContainerColor = WFTokens.Elevated,
                disabledContentColor = WFTokens.Text3,
            ),
        ) {
            Text(
                text = if (uiState.isSaving) "SAVING…" else "SAVE ENTRY",
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            color = WFTokens.Text2,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
        )
    }
}
