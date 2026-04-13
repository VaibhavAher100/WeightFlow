package com.weightflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Root theme composable. Wraps [MaterialTheme] with the "Athlete's Journal" palette
 * and typography. The [palette] string matches the keys stored in DataStore
 * ("lime", "forest", "ocean", "sunset", "rose", "violet", "gold", "ice").
 * Defaults to "lime" (#C8FF00 accent on warm dark #0F0E0B).
 */
@Composable
fun WeightFlowTheme(
    palette: String = "lime",
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorSchemeForPalette(palette),
        typography = WeightFlowTypography,
        content = content,
    )
}
