package com.weightflow.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.theme.WFTokens

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val accent = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.Finished    -> onFinished()
                is OnboardingEvent.AgeDeclined ->
                    snackbarHostState.showSnackbar("You must be 13 or older to use WeightFlow.")
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(56.dp))

        // Branding
        Text(
            text = "WEIGHTFLOW",
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            fontSize = 48.sp,
            color = accent,
            letterSpacing = 4.sp,
        )
        Text(
            text = "TRACK · REFLECT · IMPROVE",
            fontSize = 9.sp,
            letterSpacing = 2.5.sp,
            fontWeight = FontWeight.Bold,
            color = WFTokens.Text3,
        )

        Spacer(Modifier.height(40.dp))

        StepDots(currentStep = uiState.currentStep, accent = accent)

        Spacer(Modifier.height(32.dp))

        when (uiState.currentStep) {
            OnboardingStep.AGE_GATE      -> AgeGateStep(
                birthYearInput = uiState.birthYearInput,
                onBirthYearInput = viewModel::onBirthYearInput,
                accent = accent,
            )
            OnboardingStep.UNIT          -> UnitStep(
                selected = uiState.selectedUnit,
                onSelect = viewModel::onUnitSelected,
                accent = accent,
                onPrimary = onPrimary,
            )
            OnboardingStep.CURRENT_WEIGHT -> WeightStep(
                unit = uiState.selectedUnit,
                input = uiState.weightInput,
                onInput = viewModel::onWeightInput,
                accent = accent,
            )
            OnboardingStep.GOAL          -> GoalStep(
                unit = uiState.selectedUnit,
                input = uiState.goalInput,
                onInput = viewModel::onGoalInput,
                accent = accent,
            )
        }

        Spacer(Modifier.weight(1f))

        if (uiState.currentStep == OnboardingStep.AGE_GATE) {
            PrivacyFooter()
        }

        BottomBar(
            currentStep = uiState.currentStep,
            canAdvance = uiState.canAdvance,
            onBack = viewModel::onBack,
            onNext = viewModel::onNextStep,
            onComplete = viewModel::onComplete,
            accent = accent,
            onPrimary = onPrimary,
        )

        Spacer(Modifier.height(32.dp))
    }
    } // end Scaffold content lambda
} // OnboardingScreen

// ── Privacy footer ────────────────────────────────────────────────────────────

@Composable
private fun PrivacyFooter() {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        TextButton(onClick = {
            runCatching { uriHandler.openUri("https://vaibhavaher100.github.io/WeightFlow/privacy-policy") }
        }) { Text("Privacy Policy", fontSize = 11.sp, color = WFTokens.Text3) }
        Text("·", fontSize = 11.sp, color = WFTokens.Text3,
            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
        TextButton(onClick = {
            runCatching { uriHandler.openUri("https://vaibhavaher100.github.io/WeightFlow/terms-of-service") }
        }) { Text("Terms of Service", fontSize = 11.sp, color = WFTokens.Text3) }
    }
}

// ── Step indicator dots ───────────────────────────────────────────────────────

@Composable
private fun StepDots(currentStep: OnboardingStep, accent: Color) {
    val steps = OnboardingStep.entries
    val currentIdx = steps.indexOf(currentStep)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { idx, _ ->
            val isActive = idx == currentIdx
            val isPast = idx < currentIdx
            Box(
                modifier = Modifier
                    .size(width = if (isActive) 24.dp else 8.dp, height = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when {
                            isActive -> accent
                            isPast   -> accent.copy(alpha = 0.4f)
                            else     -> WFTokens.Text3.copy(alpha = 0.3f)
                        },
                    ),
            )
        }
    }
}

// ── Step 1: Age Gate (COPPA / DPDP — 18+ year-of-birth picker) ───────────────

@Composable
private fun AgeGateStep(
    birthYearInput: String,
    onBirthYearInput: (String) -> Unit,
    accent: Color,
) {
    val currentYear = java.time.LocalDate.now().year
    val age = birthYearInput.toIntOrNull()?.let { currentYear - it }
    val isUnderage = age != null && age < 18

    Column {
        Text(
            text = "WELCOME.",
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            fontSize = 52.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 52.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Your data stays on your device.\nNo subscriptions. No cloud required.",
            style = MaterialTheme.typography.bodyMedium,
            color = WFTokens.Text2,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = birthYearInput,
            onValueChange = { input ->
                if (input.length <= 4 && input.all { it.isDigit() }) onBirthYearInput(input)
            },
            label = { Text("Year of birth") },
            placeholder = { Text("e.g. 1995") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = isUnderage,
            supportingText = if (isUnderage) {
                { Text("You must be 18 or older to use WeightFlow.", color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = WFTokens.Border,
            ),
        )
    }
}

// ── Step 2: Unit Selection ────────────────────────────────────────────────────

@Composable
private fun UnitStep(
    selected: WeightUnit,
    onSelect: (WeightUnit) -> Unit,
    accent: Color,
    onPrimary: Color,
) {
    Column {
        Text(
            text = "YOUR UNIT",
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            fontSize = 52.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 52.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Can be changed in Settings anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = WFTokens.Text2,
        )
        Spacer(Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            WeightUnit.entries.forEach { unit ->
                val isSelected = unit == selected
                val (fullName, shortName) = when (unit) {
                    WeightUnit.KG  -> "Kilograms" to "kg"
                    WeightUnit.LBS -> "Pounds" to "lbs"
                    WeightUnit.ST  -> "Stone" to "st"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) WFTokens.accentDim(accent) else WFTokens.Card)
                        .border(
                            1.dp,
                            if (isSelected) WFTokens.accentBorder(accent) else WFTokens.Border,
                            RoundedCornerShape(16.dp),
                        )
                        .clickable { onSelect(unit) }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else WFTokens.Text2,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) accent else WFTokens.Elevated)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = shortName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) onPrimary else WFTokens.Text3,
                        )
                    }
                }
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
    accent: Color,
) {
    val unitLabel = when (unit) {
        WeightUnit.KG  -> "kg"
        WeightUnit.LBS -> "lbs"
        WeightUnit.ST  -> "st"
    }
    Column {
        Text(
            text = "CURRENT\nWEIGHT",
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            fontSize = 52.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 50.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Stored in kg internally, displayed in $unitLabel.",
            style = MaterialTheme.typography.bodyMedium,
            color = WFTokens.Text2,
        )
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = input,
            onValueChange = onInput,
            label = { Text("Weight in $unitLabel") },
            suffix = { Text(unitLabel, color = WFTokens.Text2) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = WFTokens.Border,
            ),
        )
    }
}

// ── Step 4: Goal Weight ───────────────────────────────────────────────────────

@Composable
private fun GoalStep(
    unit: WeightUnit,
    input: String,
    onInput: (String) -> Unit,
    accent: Color,
) {
    val unitLabel = when (unit) {
        WeightUnit.KG  -> "kg"
        WeightUnit.LBS -> "lbs"
        WeightUnit.ST  -> "st"
    }
    Column {
        Text(
            text = "YOUR GOAL",
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            fontSize = 52.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 52.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Optional — skip this and set it later in Profile.",
            style = MaterialTheme.typography.bodyMedium,
            color = WFTokens.Text2,
        )
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = input,
            onValueChange = onInput,
            label = { Text("Goal weight in $unitLabel (optional)") },
            suffix = { Text(unitLabel, color = WFTokens.Text2) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = WFTokens.Border,
            ),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Set realistic goals. Rapid weight loss can be harmful. If you're struggling with disordered eating, please seek professional support.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
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
    accent: Color,
    onPrimary: Color,
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
                Text("← Back", color = WFTokens.Text2)
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        Button(
            onClick = if (isLast) onComplete else onNext,
            enabled = canAdvance,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = onPrimary,
                disabledContainerColor = WFTokens.Elevated,
                disabledContentColor = WFTokens.Text3,
            ),
            modifier = Modifier.height(48.dp),
        ) {
            Text(
                text = if (isLast) "GET STARTED →" else "Next →",
                fontWeight = FontWeight.Bold,
                letterSpacing = if (isLast) 1.5.sp else 0.sp,
            )
        }
    }
}
