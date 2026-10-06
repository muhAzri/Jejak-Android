package com.muhazri.jejak.features.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.HeroIcon
import com.muhazri.jejak.core.designsystem.HeroIconImage
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.core.designsystem.SecondaryButton
import com.muhazri.jejak.core.designsystem.pressable

/**
 * Location is off: explains why the start cards are locked and links to system settings.
 * [ScreenLayout.Split] fills the open fold's right panel; other layouts show a banner above the cards.
 */
@Composable
fun LocationDeniedNotice(
    layout: ScreenLayout,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    val isPanel = layout == ScreenLayout.Split
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (isPanel) Modifier.fillMaxHeight() else Modifier)
            .background(colors.dangerBackground, RoundedCornerShape(12.dp))
            .padding(if (isPanel) 24.dp else 16.dp),
    ) {
        if (isPanel) {
            HeroIconImage(HeroIcon.MapPin, 96.dp, colors.danger)
            Text(
                text = stringResource(R.string.notice_denied_title),
                style = JejakFont.h1,
                color = colors.textPrimary,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HeroIconImage(HeroIcon.ExclamationTriangle, 22.dp, colors.danger)
                Text(
                    text = stringResource(R.string.notice_denied_title),
                    style = JejakFont.p1Bold,
                    color = colors.textPrimary,
                )
            }
        }
        Text(
            text = stringResource(R.string.notice_denied_message),
            style = JejakFont.p2,
            color = colors.textPrimary,
        )
        SecondaryButton(stringResource(R.string.notice_open_settings), onOpenSettings)
    }
}

/** Location was granted with "Allow Once": suggests switching to "While using the app". */
@Composable
fun LocationAllowedOnceNotice(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    val dismissLabel = stringResource(R.string.notice_dismiss)
    val interaction = remember { MutableInteractionSource() }
    val linkInteraction = remember { MutableInteractionSource() }
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.accentBackground, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        HeroIconImage(HeroIcon.InformationCircle, 22.dp, colors.textPrimary)

        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.notice_once_title),
                style = JejakFont.p1Bold,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.notice_once_message),
                style = JejakFont.p2,
                color = colors.textPrimary,
            )
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .pressable(linkInteraction, onClick = onOpenSettings)
                    .defaultMinSize(minHeight = 32.dp),
            ) {
                Text(
                    text = stringResource(R.string.notice_once_change),
                    style = JejakFont.p2Bold.copy(textDecoration = TextDecoration.Underline),
                    color = colors.textPrimary,
                )
            }
        }

        // Keeps the glyph where the design puts it while giving it a 44dp target.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding((-12).dp)
                .size(44.dp)
                .pressable(interaction, onClick = onDismiss)
                .semantics { contentDescription = dismissLabel },
        ) {
            HeroIconImage(HeroIcon.XMark, 20.dp, colors.textSecondary)
        }
    }
}

/** Location was never asked (onboarding's "Not Now"): offers the system dialog from Home. */
@Composable
fun LocationNotRequestedNotice(
    isRequesting: Boolean,
    onAllow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = JejakTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.accentBackground, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HeroIconImage(HeroIcon.MapPin, 22.dp, colors.textPrimary)
            Text(
                text = stringResource(R.string.notice_not_requested_title),
                style = JejakFont.p1Bold,
                color = colors.textPrimary,
            )
        }
        Text(
            text = stringResource(R.string.notice_not_requested_message),
            style = JejakFont.p2,
            color = colors.textPrimary,
        )
        SecondaryButton(
            text = stringResource(R.string.notice_allow_location),
            onClick = onAllow,
            enabled = !isRequesting,
        )
    }
}
