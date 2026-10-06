package com.muhazri.jejak.core.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.muhazri.jejak.R
import com.muhazri.jejak.core.designsystem.JejakFont
import com.muhazri.jejak.core.designsystem.JejakTheme

/**
 * Credit for the tiles. The OpenStreetMap tile usage policy requires it wherever the map is shown,
 * so every full-size map carries this; the small route thumbnails are covered by the same line in
 * Settings - About.
 */
@Composable
fun OsmAttribution(modifier: Modifier = Modifier) {
    val colors = JejakTheme.colors
    Text(
        text = stringResource(R.string.map_attribution),
        style = JejakFont.p3,
        color = colors.textSecondary,
        modifier = modifier
            .background(colors.surface.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
