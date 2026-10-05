package com.muhazri.jejak.features.counter.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CounterValue(
    value: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = value.toString(),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}
