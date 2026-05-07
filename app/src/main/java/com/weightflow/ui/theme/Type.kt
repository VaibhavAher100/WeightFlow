package com.weightflow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
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
    // Display — Bebas Neue. Negative tracking tightens large numerals (open-design: -0.02em to -0.03em).
    displayLarge  = baseline.displayLarge.copy(fontFamily = BebasNeue,  letterSpacing = (-1.5).sp),
    displayMedium = baseline.displayMedium.copy(fontFamily = BebasNeue, letterSpacing = (-1.0).sp),
    displaySmall  = baseline.displaySmall.copy(fontFamily = BebasNeue,  letterSpacing = (-0.5).sp),

    // Headline — Bebas Neue for section headers. Slight negative tracking.
    headlineLarge  = baseline.headlineLarge.copy(fontFamily = BebasNeue,  letterSpacing = (-0.5).sp),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = BebasNeue, letterSpacing = (-0.25).sp),
    headlineSmall  = baseline.headlineSmall.copy(fontFamily = BebasNeue,  letterSpacing = (-0.25).sp),

    // Title — Outfit SemiBold. Zero tracking — Outfit reads cleanly at these sizes.
    titleLarge  = baseline.titleLarge.copy(fontFamily = Outfit,  fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    titleMedium = baseline.titleMedium.copy(fontFamily = Outfit, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    titleSmall  = baseline.titleSmall.copy(fontFamily = Outfit,  fontWeight = FontWeight.Medium,   letterSpacing = 0.sp),

    // Body — Outfit Regular. Zero tracking, generous line height.
    bodyLarge  = baseline.bodyLarge.copy(fontFamily = Outfit,  letterSpacing = 0.sp),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = Outfit, letterSpacing = 0.sp),
    bodySmall  = baseline.bodySmall.copy(fontFamily = Outfit,  letterSpacing = 0.sp),

    // Label — Outfit Medium. Positive tracking for small ALL-CAPS UI text (open-design: +0.06em–0.1em).
    labelLarge  = baseline.labelLarge.copy(fontFamily = Outfit,  fontWeight = FontWeight.Medium,   letterSpacing = 0.5.sp),
    labelMedium = baseline.labelMedium.copy(fontFamily = Outfit, letterSpacing = 0.5.sp),
    labelSmall  = baseline.labelSmall.copy(fontFamily = Outfit,  letterSpacing = 0.8.sp),
)
