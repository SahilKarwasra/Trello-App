package com.laarasoft.frontend.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary                = L_Primary,
    onPrimary              = L_OnPrimary,
    primaryContainer       = L_PrimaryContainer,
    onPrimaryContainer     = L_OnPrimaryContainer,

    secondary              = L_Secondary,
    onSecondary            = L_OnSecondary,
    secondaryContainer     = L_SecondaryContainer,
    onSecondaryContainer   = L_OnSecondaryContainer,

    background             = L_Background,
    onBackground           = L_OnBackground,

    surface                = L_Surface,
    onSurface              = L_OnSurface,
    surfaceVariant         = L_SurfaceVariant,
    onSurfaceVariant       = L_OnSurfaceVariant,
    surfaceContainer       = L_SurfaceContainer,

    outline                = L_Outline,
    outlineVariant         = L_OutlineVariant,

    error                  = L_Error,
    onError                = L_OnError,
    errorContainer         = L_ErrorContainer,
    onErrorContainer       = L_OnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary                = D_Primary,
    onPrimary              = D_OnPrimary,
    primaryContainer       = D_PrimaryContainer,
    onPrimaryContainer     = D_OnPrimaryContainer,

    secondary              = D_Secondary,
    onSecondary            = D_OnSecondary,
    secondaryContainer     = D_SecondaryContainer,
    onSecondaryContainer   = D_OnSecondaryContainer,

    background             = D_Background,
    onBackground           = D_OnBackground,

    surface                = D_Surface,
    onSurface              = D_OnSurface,
    surfaceVariant         = D_SurfaceVariant,
    onSurfaceVariant       = D_OnSurfaceVariant,
    surfaceContainer       = D_SurfaceContainer,

    outline                = D_Outline,
    outlineVariant         = D_OutlineVariant,

    error                  = D_Error,
    onError                = D_OnError,
    errorContainer         = D_ErrorContainer,
    onErrorContainer       = D_OnErrorContainer,
)

@Composable
fun TrelloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:   @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = AppTypography,
        content     = content
    )
}