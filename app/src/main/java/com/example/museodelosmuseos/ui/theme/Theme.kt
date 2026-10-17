package com.example.museodelosmuseos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DoradoPatrimonial,
    onPrimary = FondoOscuro,
    primaryContainer = AzulMuseoMedio,
    onPrimaryContainer = TextoPrimario,
    secondary = DoradoBrillante,
    onSecondary = FondoOscuro,
    background = FondoOscuro,
    onBackground = TextoPrimario,
    surface = SuperficieCard,
    onSurface = TextoPrimario,
    surfaceVariant = AzulCard,
    onSurfaceVariant = TextoSecundario,
    error = ErrorRojo,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AzulMuseoProfundo,
    onPrimary = Color.White,
    primaryContainer = DoradoPatrimonial.copy(alpha = 0.2f),
    onPrimaryContainer = AzulMuseoProfundo,
    secondary = Terracota,
    onSecondary = Color.White,
    background = Color(0xFFF7F9FA),
    onBackground = Color(0xFF1A202C),
    surface = Color.White,
    onSurface = Color(0xFF1A202C),
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = Color(0xFF4A5568),
    error = ErrorRojo,
    onError = Color.White
)

@Composable
fun MuseoDeLosMuseosTheme(
    darkTheme: Boolean = true, // Estilo patrimonial nocturno por defecto como en el diseño de referencia
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}