package com.hmagr.budgetapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF176B58),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8F3E7),
    onPrimaryContainer = Color(0xFF103C32),
    background = Color(0xFFF6F8F5),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BDDBB),
    onPrimary = Color(0xFF003829),
    primaryContainer = Color(0xFF185441),
    onPrimaryContainer = Color(0xFFD8F3E7),
)

@Composable
fun BudgetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
