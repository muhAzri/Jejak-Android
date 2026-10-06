package com.muhazri.jejak.features.onboarding.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import com.muhazri.jejak.core.designsystem.ScreenLayout
import com.muhazri.jejak.core.designsystem.TextActionButton
import com.muhazri.jejak.core.designsystem.titleSize

/** Number of onboarding steps, for the page indicator. */
const val ONBOARDING_STEP_COUNT = 3

/**
 * Shared page layout: optional top-trailing action, centered content, bottom actions.
 * In [ScreenLayout.Split] the visual moves to the left panel; everything else stays on the right,
 * clear of the fold.
 */
@Composable
fun OnboardingPage(
    stepIndex: Int,
    onSkip: (() -> Unit)?,
    visual: @Composable (ScreenLayout) -> Unit,
    content: @Composable ColumnScope.(ScreenLayout) -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val layout = ScreenLayout.of(DpSize(maxWidth, maxHeight))
        if (layout == ScreenLayout.Split) {
            Split(stepIndex, onSkip, maxWidth / 2, visual, content, actions)
        } else {
            Stacked(layout, stepIndex, onSkip, visual, content, actions)
        }
    }
}

@Composable
private fun Stacked(
    layout: ScreenLayout,
    stepIndex: Int,
    onSkip: (() -> Unit)?,
    visual: @Composable (ScreenLayout) -> Unit,
    content: @Composable ColumnScope.(ScreenLayout) -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopBar(onSkip, Modifier.padding(horizontal = 16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        ) {
            visual(layout)
            content(layout)
        }

        Footer(stepIndex, Modifier.padding(horizontal = 24.dp), actions)
    }
}

@Composable
private fun Split(
    stepIndex: Int,
    onSkip: (() -> Unit)?,
    panelWidth: Dp,
    visual: @Composable (ScreenLayout) -> Unit,
    content: @Composable ColumnScope.(ScreenLayout) -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(panelWidth)
                .fillMaxHeight()
                .padding(bottom = 24.dp),
        ) {
            visual(ScreenLayout.Split)
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 40.dp, end = 32.dp),
        ) {
            TopBar(onSkip)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                content(ScreenLayout.Split)
            }
            Footer(stepIndex, Modifier, actions)
        }
    }
}

@Composable
private fun TopBar(onSkip: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp),
    ) {
        if (onSkip != null) TextActionButton(stringResource(R.string.onboarding_skip), onSkip)
    }
}

@Composable
private fun Footer(stepIndex: Int, modifier: Modifier, actions: @Composable ColumnScope.() -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
    ) {
        PageIndicator(ONBOARDING_STEP_COUNT, stepIndex, Modifier.padding(bottom = 8.dp))
        actions()
    }
}

/** Title and message of a page, in the display and body faces. */
@Composable
fun ColumnScope.OnboardingHeading(title: String, message: String, layout: ScreenLayout) {
    val colors = JejakTheme.colors
    Text(title, style = JejakFont.display(layout.titleSize.sp), color = colors.textPrimary)
    Text(message, style = JejakFont.p1, color = colors.textSecondary)
}
