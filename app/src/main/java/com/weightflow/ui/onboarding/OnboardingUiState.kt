package com.weightflow.ui.onboarding

import com.weightflow.domain.WeightUnit

enum class OnboardingStep {
    AGE_GATE,
    UNIT,
    CURRENT_WEIGHT,
    GOAL,
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.AGE_GATE,
    val ageConfirmed: Boolean = false,
    val selectedUnit: WeightUnit = WeightUnit.KG,
    val weightInput: String = "",
    val goalInput: String = "",
    val canAdvance: Boolean = false,
)

sealed class OnboardingEvent {
    data object Finished : OnboardingEvent()
    data object AgeDeclined : OnboardingEvent()
}
