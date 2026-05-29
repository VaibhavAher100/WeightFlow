package com.weightflow.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weightflow.domain.Badge
import com.weightflow.domain.WeightConverter
import com.weightflow.ui.theme.WFTokens

// ── Journey card ──────────────────────────────────────────────────────────────

@Composable
internal fun JourneyCard(state: ProfileUiState.Loaded, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(WFTokens.Card, RoundedCornerShape(16.dp))
            .border(1.dp, WFTokens.Border, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            JourneyValue(label = "START", value = state.startWeightDisplay ?: "—")
            JourneyValue(label = "NOW", value = state.currentWeightDisplay ?: "—", color = accent)
            JourneyValue(label = "GOAL", value = state.goalWeightDisplay ?: "—")
        }
        if (state.goalProgressPercent != null) {
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(WFTokens.Elevated, RoundedCornerShape(999.dp)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(state.goalProgressPercent.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(accent, RoundedCornerShape(999.dp)),
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${(state.goalProgressPercent * 100).toInt()}% complete",
                    fontSize = 8.sp,
                    color = WFTokens.Text3,
                    fontWeight = FontWeight.Bold,
                )
                if (state.etaDays != null) {
                    Text(
                        text = "~${state.etaDays}d to go",
                        fontSize = 8.sp,
                        color = accent.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
internal fun JourneyValue(label: String, value: String, color: Color = Color.Unspecified) {
    val textColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.onBackground else color
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
        )
        Text(
            text = label,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = WFTokens.Text3,
        )
    }
}

// ── Body stats grid ───────────────────────────────────────────────────────────

@Composable
internal fun BodyStatsGrid(state: ProfileUiState.Loaded) {
    val accent = MaterialTheme.colorScheme.primary
    val cells = buildList {
        add(Triple("Height", state.heightCm?.let { "${it.toInt()} cm" } ?: "—", false))
        add(Triple("BMI", state.bmiDisplay ?: "—", state.bmiDisplay != null))
        add(Triple("Logged", "${state.totalEntriesCount} days", false))
        add(Triple("Streak", if (state.streakDays > 0) "${state.streakDays} days" else "—", false))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        cells.chunked(2).forEach { pair ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pair.forEach { (label, value, isAccent) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(WFTokens.Card)
                            .border(
                                1.dp,
                                if (isAccent) WFTokens.accentBorder(accent) else WFTokens.Border,
                                RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                    ) {
                        Column {
                            Text(
                                text = label.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = WFTokens.Text3,
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = value,
                                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                                fontSize = 28.sp,
                                color = if (isAccent) accent else MaterialTheme.colorScheme.onBackground,
                                lineHeight = 28.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── BMI context ───────────────────────────────────────────────────────────────

@Composable
internal fun BmiContextCard(state: ProfileUiState.Loaded) {
    val category = state.bmiCategory ?: return
    val categoryColor = when (category) {
        "Normal"      -> WFTokens.Success
        "Underweight" -> MaterialTheme.colorScheme.primary
        else          -> WFTokens.Danger
    }
    val normalRangeText = if (state.bmiNormalRangeLow != null && state.bmiNormalRangeHigh != null) {
        val low = WeightConverter.format(state.bmiNormalRangeLow, state.weightUnit)
        val high = WeightConverter.format(state.bmiNormalRangeHigh, state.weightUnit)
        "Normal: $low – $high"
    } else null
    val diffText = state.bmiDifferenceFromNormal?.let { diff ->
        if (diff == 0.0) null
        else if (diff > 0) "+${WeightConverter.format(diff, state.weightUnit)} above range"
        else "−${WeightConverter.format(kotlin.math.abs(diff), state.weightUnit)} below range"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
            .background(WFTokens.Card)
            .border(1.dp, WFTokens.Border, androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (normalRangeText != null) {
                Text(text = normalRangeText, fontSize = 11.sp, color = WFTokens.Text2)
            }
            if (diffText != null) {
                Text(text = diffText, fontSize = 11.sp, color = WFTokens.Text3)
            }
        }
        Text(
            text = category,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = categoryColor,
        )
    }
}

// ── Achievements ──────────────────────────────────────────────────────────────

@Composable
internal fun AchievementsHeader(earned: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "ACHIEVEMENTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = WFTokens.Text3,
        )
        Text(
            text = "$earned / $total",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = WFTokens.Text3,
        )
    }
}

@Composable
internal fun BadgeRow(earnedBadges: Set<Badge>) {
    val accent = MaterialTheme.colorScheme.primary
    LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(Badge.entries) { badge ->
            val isEarned = badge in earnedBadges
            BadgeItem(badge = badge, isEarned = isEarned, accent = accent)
        }
    }
}

@Composable
internal fun BadgeItem(badge: Badge, isEarned: Boolean, accent: Color) {
    Column(
        modifier = Modifier.width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (isEarned) WFTokens.accentDim(accent) else WFTokens.Card)
                .border(
                    1.dp,
                    if (isEarned) WFTokens.accentBorder(accent) else WFTokens.Border,
                    RoundedCornerShape(15.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = badge.emoji(),
                fontSize = 20.sp,
                modifier = if (!isEarned) Modifier.then(
                    Modifier.background(Color.Transparent)
                ) else Modifier,
                color = if (isEarned) Color.Unspecified else Color.Unspecified.copy(alpha = 0.4f),
            )
        }
        Text(
            text = badge.shortName(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = WFTokens.Text3,
            textAlign = TextAlign.Center,
            letterSpacing = 0.3.sp,
            maxLines = 2,
            lineHeight = 11.sp,
        )
    }
}

// ── Support section ───────────────────────────────────────────────────────────

@Composable
internal fun SupportSection() {
    val uriHandler = LocalUriHandler.current
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "SUPPORT DEVELOPMENT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = WFTokens.Text3,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )

        val links = listOf(
            Triple("Ko-fi", "☕", "https://ko-fi.com/vaibhavaher"),
            Triple("Liberapay", "💛", "https://liberapay.com/vaibhavaher"),
            Triple("GitHub Sponsors", "⭐", "https://github.com/sponsors/VaibhavAher100"),
        )

        links.forEach { (label, emoji, url) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(WFTokens.Card)
                    .border(1.dp, WFTokens.Border, RoundedCornerShape(14.dp))
                    .clickable { uriHandler.openUri(url) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = "→",
                    fontSize = 16.sp,
                    color = WFTokens.Text3,
                )
            }
        }
    }
}

// ── Badge extension functions ─────────────────────────────────────────────────

internal fun Badge.emoji(): String = when (this) {
    Badge.FIRST_WEIGH_IN      -> "🏆"
    Badge.GOAL_SET            -> "🎯"
    Badge.SEVEN_DAY_STREAK    -> "🔥"
    Badge.THIRTY_DAY_STREAK   -> "📅"
    Badge.HUNDRED_DAY_STREAK  -> "🌟"
    Badge.TEN_LOGS            -> "⚡"
    Badge.FIFTY_LOGS          -> "💪"
    Badge.THREE_SIXTY_FIVE_LOGS -> "🏅"
    Badge.HALFWAY_THERE       -> "🎯"
    Badge.GOAL_CRUSHER        -> "🏆"
    Badge.COMEBACK            -> "💪"
    Badge.STEADY_STATE        -> "⭐"
}

internal fun Badge.shortName(): String = when (this) {
    Badge.FIRST_WEIGH_IN      -> "First Log"
    Badge.GOAL_SET            -> "Goal Set"
    Badge.SEVEN_DAY_STREAK    -> "7-Day"
    Badge.THIRTY_DAY_STREAK   -> "30-Day"
    Badge.HUNDRED_DAY_STREAK  -> "100 Days"
    Badge.TEN_LOGS            -> "10 Logs"
    Badge.FIFTY_LOGS          -> "50 Logs"
    Badge.THREE_SIXTY_FIVE_LOGS -> "365 Logs"
    Badge.HALFWAY_THERE       -> "Halfway"
    Badge.GOAL_CRUSHER        -> "Goal Hit"
    Badge.COMEBACK            -> "Comeback"
    Badge.STEADY_STATE        -> "Steady"
}
