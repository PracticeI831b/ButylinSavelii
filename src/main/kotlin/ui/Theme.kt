package ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import model.DarkBackground
import model.ErrorColor
import model.PrimaryColor
import model.SecondaryColor
import model.TextColor

/**
 * Кастомная тема приложения (темная)
 */
@Composable
fun EquationSolverTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = darkColors(
            primary = PrimaryColor,
            secondary = SecondaryColor,
            error = ErrorColor,
            background = DarkBackground,
            onPrimary = TextColor,
            onBackground = TextColor
        ),
        content = content
    )
}