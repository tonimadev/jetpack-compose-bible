package digital.tonima.bibliadigital.ui.bible.chapters

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import digital.tonima.bibliadigital.ui.bible.BibleViewModel
import digital.tonima.bibliadigital.ui.components.AppBar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListChapters(
    bookName: String,
    bookAbbrev: String,
    chapterQuantity: Int,
    navController: NavHostController,
    viewModel: BibleViewModel,
) {
    val fontSizeState: State<TextUnit> = viewModel.fontSize.observeAsState(initial = 16.sp)

    Scaffold(topBar = {
        AppBar(bookName, icon = Icons.Default.ArrowBack) {
            navController.navigateUp()
        }
    }) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 70.dp)) {
                items(chapterQuantity) { index ->
                    val currentChapter = index + 1
                    ChapterItem(currentChapter, fontSizeState) {
                        navController.navigate("reading/$bookName/$bookAbbrev/$currentChapter/$chapterQuantity")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChapterItem(
    chapter: Int,
    fontSizeState: State<TextUnit>,
    onBookClick: () -> Unit,
) {
    Card(
        elevation = 5.dp,
        modifier =
            Modifier
                .wrapContentSize()
                .padding(12.dp),
        onClick = onBookClick,
    ) {
        Text(
            text = chapter.toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(12.dp),
            fontSize = fontSizeState.value,
        )
    }
}
