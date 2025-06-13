package com.example.inbetween.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme

// הצבעים שלכם מ–Color.kt
private val DarkColorScheme = darkColorScheme(
    primary     = Primary,
    onPrimary   = White,
    secondary   = Secondary,
    onSecondary = Black,
    background  = StatusBar
)
private val LightColorScheme = lightColorScheme(
    primary     = Primary,
    onPrimary   = White,
    secondary   = Secondary,
    onSecondary = Black,
    background  = StatusBar
)

@SuppressLint("NewApi")
@Composable
fun InBetweenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // בדיקה כפולה: גרסת API + דינמי + מצב לילה
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dynamicColor && darkTheme ->
            dynamicDarkColorScheme(LocalContext.current)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dynamicColor && !darkTheme ->
            dynamicLightColorScheme(LocalContext.current)
        darkTheme ->
            DarkColorScheme
        else ->
            LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = Shapes,
        content     = content
    )
}
