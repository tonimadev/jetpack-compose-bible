package digital.tonima.bibliadigital.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import digital.tonima.bibliadigital.R

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.app_name),
    icon: ImageVector? = null,
    onBackClick: () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = "",
                    Modifier
                        .padding(horizontal = 12.dp)
                        .clickable { onBackClick.invoke() },
                )
            }
        },
        title = { Text(text = title, textAlign = TextAlign.Center) },
        backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
    )
}
