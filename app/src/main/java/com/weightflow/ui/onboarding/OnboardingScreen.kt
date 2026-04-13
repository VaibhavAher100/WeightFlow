package com.weightflow.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.domain.WeightUnit

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.Finished -> onFinished()
                is OnboardingEvent.AgeDeclined -> { /* stay on screen */ }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            StepIndicator(currentStep = uiState.currentStep)
            Spacer(modifier = Modifier.height(32.dp))
            when (uiState.currentStep) {
                OnboardingStep.AGE_GATE -> AgeGateStep(
                    ageConfirmed = uiState.ageConfirmed,
                    onAgeConfirmed = viewModel::onAgeConfirmed,
                )
                OnboardingStep.UNIT -> UnitStep(
                    selected = uiState.selectedUnit,
                    onSelect = viewModel::onUnitSelected,
                )
                OnboardingStep.CURRENT_WEIGHT -> WeightStep(
                    unit = uiState.selectedUnit,
                    input = uiState.weightInput,
                    onInput = viewModel::onWeightInput,
                )
                OnboardingStep.GOAL -> GoalStep(
                    unit = uiState.selectedUnit,
                    input = uiState.goalInput,
                    onInput = viewModel::onGoalInput,
                )
            }
        }

        BottomBar(
            currentStep = uiState.currentStep,
            canAdvance = uiState.canAdvance,
            onBack = viewModel::onBack,
            onNext = viewModel::onNextStep,
            onComplete = viewModel::onComplete,
        )
    }
}

@Composable
private fun StepIndicator(currentStep: OnboardingStep) {
    val steps = OnboardingStep.entries
    val current = steps.indexOf(currentStep) + 1
    Text(
        text = "Step $current of ${steps.size}",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

// ── Step 1: Age Gate (COPPA) ──────────────────────────────────────────────────

@Composable
private fun AgeGateStep(
    ageConfirmed: Boolean,
    onAgeConfirmed: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Welcome to WeightFlow",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Your data stays on your device. No subscriptions. No cloud required.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = ageConfirmed,
                onCheckedChange = onAgeConfirmed,
            )
            Text(
                text = "I confirm I am 13 years of age or older",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ── Step 2: Unit Selection ────────────────────────────────────────────────────

@Composable
private fun UnitStep(
    selected: WeightUnit,
    onSelect: (WeightUnit) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Choose your unit",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "You can change this later in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeightUnit.entries.forEach { unit ->
                FilterChip(
                    selected = unit == selected,
                    onClick = { onSelect(unit) },
                    label = {
                        Text(
                            text = when (unit) {
                                WeightUnit.KG -> "kg"
                                WeightUnit.LBS -> "lbs"
                                WeightUnit.ST -> "st"
                            },
                        )
                    },
                )
            }
        }
    }
}

// ── Step 3: Current Weight ────────────────────────────────────────────────────

@Composable
private fun WeightStep(
    unit: WeightUnit,
    input: String,
    onInput: (String) -> Unit,
) {
    val unitLabel = when (unit) {
        WeightUnit.KG -> "kg"
        WeightUnit.LBS -> "lbs"
        WeightUnit.ST -> "st"
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "What's your current weight?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
            value = input,
            onValueChange = onInput,
            label = { Text("Weight in $unitLabel") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── Step 4: Goal Weight ───────────────────────────────────────────────────────

@Composable
private fun GoalStep(
    unit: WeightUnit,
    input: String,
    onInput: (String) -> Unit,
) {
    val unitLabel = when (unit) {
        WeightUnit.KG -> "kg"
        WeightUnit.LBS -> "lbs"
        WeightUnit.ST -> "st"
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Set a goal weight",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Optional — you can skip this and set it later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = input,
            onValueChange = onInput,
            label = { Text("Goal weight in $unitLabel (optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── Bottom navigation bar ─────────────────────────────────────────────────────

@Composable
private fun BottomBar(
    currentStep: OnboardingStep,
    canAdvance: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    val isFirst = currentStep == OnboardingStep.AGE_GATE
    val isLast = currentStep == OnboardingStep.GOAL

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isFirst) {
            TextButton(onClick = onBack) {
                Text("Back")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Button(
            onClick = if (isLast) onComplete else onNext,
            enabled = canAdvance,
        ) {
            Text(if (isLast) "Get started" else "Next")
        }
    }
}
