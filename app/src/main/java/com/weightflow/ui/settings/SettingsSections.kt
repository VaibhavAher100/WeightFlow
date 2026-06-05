package com.weightflow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weightflow.R
import com.weightflow.ui.theme.WFTokens

// ── Export format selector data ───────────────────────────────────────────────

internal data class FormatOption(
    val format: ExportFormat,
    @param:androidx.annotation.StringRes val labelRes: Int,
    @param:androidx.annotation.StringRes val descriptionRes: Int,
)

internal val FORMAT_OPTIONS = listOf(
    FormatOption(
        ExportFormat.PLAINTEXT,
        R.string.settings_export_format_plaintext_label,
        R.string.settings_export_format_plaintext_desc,
    ),
    FormatOption(
        ExportFormat.ENCRYPTED_ZIP,
        R.string.settings_export_format_encrypted_label,
        R.string.settings_export_format_encrypted_desc,
    ),
    FormatOption(
        ExportFormat.MINIMAL_CSV,
        R.string.settings_export_format_minimal_label,
        R.string.settings_export_format_minimal_desc,
    ),
)

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
internal fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = WFTokens.Text3,
        modifier = Modifier.padding(top = 10.dp, start = 2.dp),
    )
}

// ── Unit chip ─────────────────────────────────────────────────────────────────

@Composable
internal fun UnitChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val bg     = if (selected) accent else WFTokens.Card
    val fg     = if (selected) MaterialTheme.colorScheme.onPrimary else WFTokens.Text2
    val border = if (selected) accent else WFTokens.Border
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

// ── Theme grid ────────────────────────────────────────────────────────────────

@Composable
internal fun ThemeGrid(
    selectedPalette: String,
    onThemeSelected: (String) -> Unit,
    accent: Color,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .heightIn(max = 320.dp),
    ) {
        items(THEME_OPTIONS) { option ->
            val key = option.key
            val dotColor = option.dotColor
            val isSelected = key == selectedPalette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected) WFTokens.accentSoft(accent) else WFTokens.Card,
                        RoundedCornerShape(12.dp),
                    )
                    .border(
                        1.dp,
                        if (isSelected) WFTokens.accentBorder(accent) else WFTokens.Border,
                        RoundedCornerShape(12.dp),
                    )
                    .clickable { onThemeSelected(key) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(14.dp)
                        .background(dotColor, RoundedCornerShape(999.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(option.labelRes),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else WFTokens.Text2,
                )
            }
        }
    }
}

// ── Toggle row ────────────────────────────────────────────────────────────────

@Composable
internal fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, fontSize = 12.sp, color = WFTokens.Text2)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = accent,
                uncheckedTrackColor = WFTokens.Elevated,
                uncheckedBorderColor = WFTokens.Border,
            ),
        )
    }
}

// ── Permission-denied banner ──────────────────────────────────────────────────

@Composable
internal fun PermissionDeniedBanner(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.settings_permission_denied_banner),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.settings_permission_banner_dismiss),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                )
                .padding(4.dp),
        )
    }
}

// ── Action row ────────────────────────────────────────────────────────────────

@Composable
internal fun SettingsActionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, fontSize = 12.sp, color = WFTokens.Text2)
        }
        Text("›", fontSize = 20.sp, color = WFTokens.Text3)
    }
}

// ── Info/warning banners ──────────────────────────────────────────────────────

@Composable
internal fun InfoBanner(text: String, containerColor: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(text = text, fontSize = 12.sp, color = textColor)
    }
}

@Composable
internal fun PlaintextWarningBanner() {
    InfoBanner(
        text = stringResource(R.string.settings_plaintext_warning_banner),
        containerColor = MaterialTheme.colorScheme.errorContainer,
        textColor = MaterialTheme.colorScheme.onErrorContainer,
    )
}

@Composable
internal fun EncryptedExportInfoBanner() {
    InfoBanner(
        text = stringResource(R.string.settings_encrypted_info_banner),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
}

@Composable
internal fun MinimalCsvWarningBanner() {
    InfoBanner(
        text = stringResource(R.string.settings_minimal_warning_banner),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        textColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
}

// ── Export format selector ────────────────────────────────────────────────────

@Composable
internal fun ExportFormatSelector(
    selected: ExportFormat,
    accent: Color,
    onFormatChanged: (ExportFormat) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
            .padding(vertical = 4.dp),
    ) {
        FORMAT_OPTIONS.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onFormatChanged(option.format) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected == option.format,
                    onClick = { onFormatChanged(option.format) },
                    colors = RadioButtonDefaults.colors(selectedColor = accent),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        stringResource(option.labelRes),
                        fontSize = 14.sp,
                        fontWeight = if (selected == option.format) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(stringResource(option.descriptionRes), fontSize = 11.sp, color = WFTokens.Text2)
                }
            }
        }
    }
}

