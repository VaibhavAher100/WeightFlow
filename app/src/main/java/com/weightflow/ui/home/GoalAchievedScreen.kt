package com.weightflow.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weightflow.R
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.components.rememberWFHaptics
import com.weightflow.ui.theme.WFTokens

@Composable
fun GoalAchievedScreen(
    state: HomeUiState.HasData,
    onSetNewGoal: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val haptics = rememberWFHaptics()

    LaunchedEffect(Unit) { haptics.celebrate() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width / 2, 0f),
                    radius = size.width * 0.8f,
                ),
                center = Offset(size.width / 2, 0f),
                radius = size.width * 0.8f,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(72.dp))
            Text(text = "🏆", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.goal_achieved_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                letterSpacing = (-1).sp,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            )
            Text(
                text = stringResource(R.string.goal_achieved_you_did_it),
                fontSize = 14.sp,
                color = WFTokens.Text2,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(24.dp))
            val numberText = state.latestWeightDisplay
                .replace(" kg", "").replace(" lbs", "").replace(" st", "")
            Text(
                text = numberText,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                letterSpacing = (-3).sp,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            )
            Text(
                text = when (state.weightUnit) {
                    WeightUnit.KG  -> stringResource(R.string.unit_kg_full)
                    WeightUnit.LBS -> stringResource(R.string.unit_lbs_full)
                    WeightUnit.ST  -> stringResource(R.string.unit_st_full)
                }.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = accent.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                JourneyCell(value = state.startDisplay ?: "—", label = stringResource(R.string.goal_achieved_stat_started).uppercase())
                JourneyCell(value = state.lostDisplay ?: "—", label = stringResource(R.string.goal_achieved_stat_lost).uppercase(), valueColor = accent)
                JourneyCell(value = "${state.streakDays}d", label = stringResource(R.string.goal_achieved_stat_streak).uppercase())
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.goal_achieved_keep_going),
                fontSize = 13.sp,
                color = WFTokens.Text2,
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accent, RoundedCornerShape(999.dp))
                    .clickable { onSetNewGoal() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${stringResource(R.string.goal_achieved_set_new_goal).uppercase()}  →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun JourneyCell(
    value: String,
    label: String,
    valueColor: Color = Color.Unspecified,
) {
    val color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onBackground else valueColor
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = color,
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
