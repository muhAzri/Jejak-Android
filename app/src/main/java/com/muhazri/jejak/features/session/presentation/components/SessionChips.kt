package com.muhazri.jejak.features.session.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.HeroIcon
import com.muhazri.jejak.core.designsystem.HeroIconImage
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.features.home.domain.entities.ActivityType
import com.muhazri.jejak.features.home.presentation.components.ActivityChip
import com.muhazri.jejak.features.home.presentation.components.SessionFormat
import com.muhazri.jejak.features.session.domain.entities.GPSSignal
import com.muhazri.jejak.features.session.domain.entities.SessionPhase

/** Chips over the top of the session map: activity on the left, GPS or lock state on the right. */
@Composable
fun SessionChips(
    activity: ActivityType,
    signal: GPSSignal,
    phase: SessionPhase,
    isLocked: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        ActivityChip(activity, colors.surface)
        Spacer(Modifier.weight(1f))
        when {
            isLocked -> StatusChip(HeroIcon.LockClosed, stringResource(R.string.chip_locked), iconSize = 14.dp)
            phase == SessionPhase.Paused -> Unit
            signal == GPSSignal.Searching -> StatusChip(
                icon = HeroIcon.SignalSlash,
                title = stringResource(R.string.chip_gps),
                iconColor = colors.textSecondary,
            )

            signal == GPSSignal.Good -> StatusChip(HeroIcon.Signal, stringResource(R.string.chip_gps))
            else -> StatusChip(
                icon = HeroIcon.ExclamationTriangle,
                title = stringResource(R.string.chip_weak_signal),
                isWarning = true,
            )
        }
    }
}

@Composable
private fun StatusChip(
    icon: HeroIcon,
    title: String,
    iconSize: Dp = 16.dp,
    iconColor: Color? = null,
    isWarning: Boolean = false,
) {
    val colors = JejakTheme.colors
    val ink = if (isWarning) colors.accentInk else colors.textPrimary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(if (isWarning) colors.accent else colors.surface, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        HeroIconImage(icon, iconSize, iconColor ?: ink)
        Text(title, style = JejakFont.p3Bold, color = ink)
    }
}

/** "Paused · 0:42" pill at the bottom of the map. */
@Composable
fun PausedPill(pausedForSeconds: Double, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .background(colors.accent, CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        HeroIconImage(HeroIcon.Pause, 20.dp, colors.accentInk)
        Text(
            text = stringResource(R.string.session_paused, SessionFormat.duration(pausedForSeconds)),
            style = JejakFont.p1Bold,
            color = colors.accentInk,
        )
    }
}
