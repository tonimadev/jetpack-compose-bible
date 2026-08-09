package digital.tonima.bibliadigital.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import digital.tonima.bibliadigital.R

@Composable
fun ErrorScreen(function: () -> Unit) {
    Surface(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(id = R.string.loading_error))
            Button(onClick = function, enabled = true) {
                Text(text = stringResource(id = R.string.try_again))
            }
        }
    }
}
