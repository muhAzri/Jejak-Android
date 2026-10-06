package com.muhazri.jejak.features.onboarding.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.HeroIcon
import com.muhazri.jejak.core.designsystem.HeroIconImage
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.PrimaryButton
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.core.designsystem.TextActionButton
import com.muhazri.jejak.core.designsystem.safeArea
import com.muhazri.jejak.features.onboarding.presentation.components.OnboardingHeading
import com.muhazri.jejak.features.onboarding.presentation.components.OnboardingPage

enum class OnboardingStep { Intro, Privacy, Location }

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    isRequestingPermission: Boolean = false,
    /** "Allow Location" shows the system dialog; onboarding finishes whatever it answers. */
    onAllowLocation: () -> Unit = onFinish,
    /** "Not Now" finishes without asking; Home offers the dialog again from its own notice. */
    onLater: () -> Unit = onFinish,
) {
    val colors = JejakTheme.colors
    val insets = safeArea()
    var step by rememberSaveable { mutableStateOf(OnboardingStep.Intro) }

    // SwiftUI keeps these screens inside the safe area; Compose draws edge to edge, so pad it back.
    Box(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .padding(
                top = insets.top,
                bottom = insets.bottom,
                start = insets.leading,
                end = insets.trailing,
            ),
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(280)) },
            label = "onboardingStep",
        ) { current ->
            when (current) {
                OnboardingStep.Intro -> IntroPage(
                    onContinue = { step = OnboardingStep.Privacy },
                    // Skip jumps to the permission step so location is still explained before the dialog.
                    onSkip = { step = OnboardingStep.Location },
                )

                OnboardingStep.Privacy -> PrivacyPage(
                    onContinue = { step = OnboardingStep.Location },
                    onSkip = { step = OnboardingStep.Location },
                )

                OnboardingStep.Location -> LocationPage(
                    isRequesting = isRequestingPermission,
                    onAllow = onAllowLocation,
                    onLater = onLater,
                )
            }
        }
    }
}

@Composable
private fun IntroPage(onContinue: () -> Unit, onSkip: () -> Unit) {
    OnboardingPage(
        stepIndex = OnboardingStep.Intro.ordinal,
        onSkip = onSkip,
        visual = { layout ->
            SampleSessionCard(
                layout = layout,
                modifier = Modifier
                    .padding(bottom = if (layout == ScreenLayout.Compact) 12.dp else 24.dp)
                    .padding(horizontal = if (layout == ScreenLayout.Split) 40.dp else 0.dp),
            )
        },
        content = { layout ->
            OnboardingHeading(
                title = stringResource(R.string.onboarding_intro_title),
                message = stringResource(R.string.onboarding_intro_message),
                layout = layout,
            )
        },
        actions = {
            PrimaryButton(stringResource(R.string.onboarding_continue), onContinue)
        },
    )
}

/** The design's hero card: a sample session's distance, time and pace on the accent. */
@Composable
private fun SampleSessionCard(layout: ScreenLayout, modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    val padding = when (layout) {
        ScreenLayout.Regular -> 24.dp
        ScreenLayout.Compact -> 20.dp
        ScreenLayout.Split -> 28.dp
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.accent, RoundedCornerShape(12.dp))
            .padding(padding),
    ) {
        Text(stringResource(R.string.metric_distance), style = JejakFont.p3Semibold, color = colors.accentInk)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "5.24",
                style = JejakFont.display(if (layout == ScreenLayout.Compact) 56.sp else 88.sp),
                color = colors.accentInk,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                text = "km",
                style = JejakFont.h2,
                color = colors.accentInk,
                modifier = Modifier.alignByBaseline(),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("28:41", style = JejakFont.p1Semibold, color = colors.accentInk)
            Text("5:28 /km", style = JejakFont.p1Semibold, color = colors.accentInk)
        }
    }
}

@Composable
private fun PrivacyPage(onContinue: () -> Unit, onSkip: () -> Unit) {
    val colors = JejakTheme.colors
    OnboardingPage(
        stepIndex = OnboardingStep.Privacy.ordinal,
        onSkip = onSkip,
        visual = { layout ->
            // The closed fold's screen is too short for the badge; the list carries the message.
            if (layout != ScreenLayout.Compact) {
                val size = if (layout == ScreenLayout.Split) 160.dp else 96.dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(bottom = if (layout == ScreenLayout.Split) 0.dp else 24.dp)
                        .size(size)
                        .background(colors.accent, CircleShape),
                ) {
                    HeroIconImage(HeroIcon.ShieldCheck, size / 2, colors.accentInk)
                }
            }
        },
        content = { layout ->
            OnboardingHeading(
                title = stringResource(R.string.onboarding_privacy_title),
                message = stringResource(R.string.onboarding_privacy_message),
                layout = layout,
            )

            Column(
                Modifier
                    .padding(top = layout.detailSpacing)
                    .fillMaxWidth()
                    .background(colors.surfaceTint, RoundedCornerShape(12.dp)),
            ) {
                PrivacyRow(HeroIcon.User, stringResource(R.string.onboarding_privacy_no_account), layout)
                RowDivider()
                PrivacyRow(HeroIcon.SignalSlash, stringResource(R.string.onboarding_privacy_offline), layout)
                RowDivider()
                PrivacyRow(HeroIcon.LockClosed, stringResource(R.string.onboarding_privacy_on_device), layout)
            }
        },
        actions = {
            PrimaryButton(stringResource(R.string.onboarding_continue), onContinue)
        },
    )
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .padding(start = 50.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 1.dp)
            .background(JejakTheme.colors.border),
    )
}

@Composable
private fun PrivacyRow(icon: HeroIcon, text: String, layout: ScreenLayout) {
    val colors = JejakTheme.colors
    val height = when (layout) {
        ScreenLayout.Regular -> 56.dp
        ScreenLayout.Compact -> 44.dp
        ScreenLayout.Split -> 48.dp
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = height)
            .padding(horizontal = 16.dp),
    ) {
        HeroIconImage(icon, 22.dp, colors.textPrimary)
        Text(text, style = JejakFont.p1Semibold, color = colors.textPrimary)
    }
}

@Composable
private fun LocationPage(isRequesting: Boolean, onAllow: () -> Unit, onLater: () -> Unit) {
    val colors = JejakTheme.colors
    OnboardingPage(
        stepIndex = OnboardingStep.Location.ordinal,
        onSkip = null,
        visual = { layout ->
            val pin = when (layout) {
                ScreenLayout.Regular -> 120.dp
                ScreenLayout.Compact -> 80.dp
                ScreenLayout.Split -> 220.dp
            }
            val bottom = when (layout) {
                ScreenLayout.Split -> 0.dp
                ScreenLayout.Compact -> 4.dp
                ScreenLayout.Regular -> 16.dp
            }
            HeroIconImage(HeroIcon.MapPin, pin, colors.accent, Modifier.padding(bottom = bottom))
        },
        content = { layout ->
            OnboardingHeading(
                title = stringResource(R.string.onboarding_location_title),
                message = stringResource(R.string.onboarding_location_message),
                layout = layout,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(top = layout.detailSpacing)
                    .fillMaxWidth()
                    .background(colors.accentBackground, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                HeroIconImage(
                    HeroIcon.InformationCircle,
                    20.dp,
                    colors.textPrimary,
                    Modifier.padding(top = 2.dp),
                )
                Text(
                    text = stringResource(R.string.onboarding_location_hint),
                    style = JejakFont.p2Semibold,
                    color = colors.textPrimary,
                )
            }
        },
        actions = {
            PrimaryButton(
                text = stringResource(R.string.onboarding_location_allow),
                onClick = onAllow,
                enabled = !isRequesting,
            )
            TextActionButton(stringResource(R.string.onboarding_location_later), onLater)
        },
    )
}

/** Gap above the card that follows the heading. */
private val ScreenLayout.detailSpacing: Dp
    get() = when (this) {
        ScreenLayout.Regular -> 16.dp
        ScreenLayout.Compact -> 8.dp
        ScreenLayout.Split -> 12.dp
    }
