package digital.tonima.bibliadigital.ui.bible.reading

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.bibliadigital.R
import digital.tonima.bibliadigital.ui.bible.BibleIntent
import digital.tonima.bibliadigital.ui.bible.BibleViewModel

@Composable
fun ReadingSettingsSheet(
    viewModel: BibleViewModel,
    selectedVersion: String,
    versions: List<digital.tonima.bibliadigital.domain.model.Version>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
    ) {
        Text(
            text = "Configurações de Leitura",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tamanho da Fonte",
            style = MaterialTheme.typography.subtitle2,
            color = Color.Gray,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(onClick = { viewModel.sendIntent(BibleIntent.DecreaseFontSize) }) {
                Icon(ImageVector.vectorResource(id = R.drawable.ic_minus), contentDescription = null)
            }

            Text(text = "Aa", fontSize = 20.sp, fontWeight = FontWeight.Medium)

            IconButton(onClick = { viewModel.sendIntent(BibleIntent.IncreaseFontSize) }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Versão da Bíblia",
            style = MaterialTheme.typography.subtitle2,
            color = Color.Gray,
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
        ) {
            items(versions) { version ->
                val isSelected = version.code.lowercase() == selectedVersion.lowercase()
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                    elevation = 2.dp,
                    modifier =
                        Modifier.clickable {
                            viewModel.sendIntent(BibleIntent.ChangeVersion(version.code.lowercase()))
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = version.code,
                            color = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
