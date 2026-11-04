
package com.example.gazoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresClaros = lightColorScheme(
    primary = VerdePrincipal,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeOscuro,
    secondary = VerdeOscuro,
    onSecondary = Color.White,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro
)

private val ColoresOscuros = darkColorScheme(
    primary = Color(0xFF8BD7AA),
    onPrimary = VerdeOscuro,
    primaryContainer = VerdeOscuro,
    onPrimaryContainer = VerdeClaro,
    secondary = Color(0xFFB3DCC1),
    onSecondary = FondoOscuro,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro
)

@Composable
fun GazoappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) {
            ColoresOscuros
        } else {
            ColoresClaros
        },
        typography = Typography,
        content = content
    )
}
