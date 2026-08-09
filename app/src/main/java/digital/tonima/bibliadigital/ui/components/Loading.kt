package digital.tonima.bibliadigital.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Loading() {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .fillMaxSize()
                .background(if (isSystemInDarkTheme()) Color.Black else Color.White),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = if (isSystemInDarkTheme()) Color.White else Color.Black,
        )
    }
}
