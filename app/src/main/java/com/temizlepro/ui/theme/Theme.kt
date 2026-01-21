package com.temizlepro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Mint,
    onPrimary = Color.White,
    primaryContainer = Sand,
    onPrimaryContainer = MintDark,
    secondary = Orange,
    tertiary = Red
)

private val DarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = Color.Black,
    primaryContainer = MintDark,
    onPrimaryContainer = Color.White,
    secondary = Orange,
    tertiary = Red
)

@Composable
fun TemizleProTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
