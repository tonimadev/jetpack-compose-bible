package digital.tonima.bibliadigital.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme =
    darkColorScheme(
        primary = Color.White,
        secondary = Color.White,
        tertiary = Color.White,
        background = Color.Black,
        surface = Color.Black,
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onTertiary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Color.Black,
        secondary = Color.Black,
        tertiary = Color.Black,
        background = Color.White,
        surface = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
    )

@Composable
fun BibliaSagradaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val systemUiController = rememberSystemUiController()

    val colorScheme =
        if (darkTheme) {
            systemUiController.setSystemBarsColor(
                color = Color.Black,
            )
            DarkColorScheme
        } else {
            systemUiController.setSystemBarsColor(
                color = Color.White,
            )
            LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
