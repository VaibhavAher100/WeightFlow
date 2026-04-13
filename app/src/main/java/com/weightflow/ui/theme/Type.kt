package com.weightflow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.weightflow.R

val GoogleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

val BebasNeue: FontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Bebas Neue"),
        fontProvider = GoogleFontProvider,
        weight = FontWeight.Normal,
    )
)

val Outfit: FontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Outfit"),
        fontProvider = GoogleFontProvider,
        weight = FontWeight.Normal,
    ),
    Font(
        googleFont = GoogleFont("Outfit"),
        fontProvider = GoogleFontProvider,
        weight = FontWeight.Medium,
    ),
    Font(
        googleFont = GoogleFont("Outfit"),
        fontProvider = GoogleFontProvider,
        weight = FontWeight.SemiBold,
    ),
    Font(
        googleFont = GoogleFont("Outfit"),
        fontProvider = GoogleFontProvider,
        weight = FontWeight.Bold,
    ),
)

private val baseline = Typography()

val WeightFlowTypography = Typography(
    // Display — Bebas Neue for large numbers and hero text
    displayLarge  = baseline.displayLarge.copy(fontFamily = BebasNeue),
    displayMedium = baseline.displayMedium.copy(fontFamily = BebasNeue),
    displaySmall  = baseline.displaySmall.copy(fontFamily = BebasNeue),
    // Headline — Bebas Neue for section headers
    headlineLarge  = baseline.headlineLarge.copy(fontFamily = BebasNeue),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = BebasNeue),
    headlineSmall  = baseline.headlineSmall.copy(fontFamily = BebasNeue),
    // Title — Outfit SemiBold
    titleLarge  = baseline.titleLarge.copy(fontFamily = Outfit, fontWeight = FontWeight.SemiBold),
    titleMedium = baseline.titleMedium.copy(fontFamily = Outfit, fontWeight = FontWeight.SemiBold),
    titleSmall  = baseline.titleSmall.copy(fontFamily = Outfit, fontWeight = FontWeight.Medium),
    // Body — Outfit Regular
    bodyLarge  = baseline.bodyLarge.copy(fontFamily = Outfit),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = Outfit),
    bodySmall  = baseline.bodySmall.copy(fontFamily = Outfit),
    // Label — Outfit Medium
    labelLarge  = baseline.labelLarge.copy(fontFamily = Outfit, fontWeight = FontWeight.Medium),
    labelMedium = baseline.labelMedium.copy(fontFamily = Outfit),
    labelSmall  = baseline.labelSmall.copy(fontFamily = Outfit),
)
