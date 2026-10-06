package com.muhazri.jejak.features.session.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.AutoSizeText
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme
import androidx.compose.ui.res.stringResource
import com.muhazri.jejak.features.home.presentation.components.SessionFormat
import com.muhazri.jejak.features.settings.domain.entities.DistanceUnit

/** Big distance readout: label, number, unit. */
@Composable
fun DistanceMetric(
    meters: Double,
    unit: DistanceUnit,
    size: TextUnit,
    modifier: Modifier = Modifier,
    isDimmed: Boolean = false,
) {
    val colors = JejakTheme.colors
    Column(modifier) {
        Text(
            text = stringResource(R.string.metric_distance),
            style = JejakFont.p3Semibold,
            color = colors.textSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AutoSizeText(
                text = SessionFormat.distance(meters, unit),
                style = JejakFont.display(size),
                color = if (isDimmed) colors.textSecondary else colors.textPrimary,
                minScale = 0.5f,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                text = unit.symbol,
                style = JejakFont.h2,
                color = colors.textSecondary,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

/** Small labelled value in the three-column metric row. */
@Composable
fun MetricTile(
    title: String,
    value: String,
    size: TextUnit,
    modifier: Modifier = Modifier,
    isDimmed: Boolean = false,
) {
    val colors = JejakTheme.colors
    Column(modifier.fillMaxWidth()) {
        AutoSizeText(title, JejakFont.p3Semibold, colors.textSecondary, minScale = 0.8f)
        AutoSizeText(
            text = value,
            style = JejakFont.display(size),
            color = if (isDimmed) colors.textSecondary else colors.textPrimary,
            minScale = 0.6f,
        )
    }
}
