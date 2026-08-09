package digital.tonima.bibliadigital.ui.bible.reading

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import digital.tonima.bibliadigital.R
import digital.tonima.bibliadigital.domain.common.constants.PLAY_STORE_URL
import digital.tonima.bibliadigital.domain.model.Verse
import digital.tonima.bibliadigital.ui.bible.BibleIntent
import digital.tonima.bibliadigital.ui.bible.BibleViewModel
import digital.tonima.bibliadigital.ui.components.AppBar
import digital.tonima.bibliadigital.ui.components.ErrorScreen
import digital.tonima.bibliadigital.ui.components.Loading

@Composable
fun BibleReading(
    bookName: String,
    bookAbbrev: String,
    chapterId: Int,
    chapterQuantity: Int,
    navController: NavHostController,
    viewModel: BibleViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val showBottomBar = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.sendIntent(BibleIntent.LoadChapter(bookName, bookAbbrev, chapterId))
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = showBottomBar.value,
                enter = slideInVertically(initialOffsetY = { -40 }),
                exit = slideOutVertically(targetOffsetY = { -40 }),
            ) {
                AppBar(
                    title = "$bookName - ${stringResource(id = R.string.chapter)} ${state.currentChapter}",
                    icon = Icons.Default.ArrowBack,
                ) {
                    viewModel.sendIntent(BibleIntent.StopSpeech)
                    navController.navigateUp()
                }
            }
        },
    ) { paddingValues ->
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                toggleNavigationMenusVisibility(showBottomBar)
                            },
                        )
                    },
        ) {
            DropdownMenu(
                expanded = state.showTutorial,
                onDismissRequest = { viewModel.sendIntent(BibleIntent.DisableTutorial) },
            ) {
                DropdownMenuItem(onClick = { }) {
                    Row {
                        Text(
                            text = stringResource(id = R.string.verse_long_press_tutorial),
                        )
                    }
                }
            }
            if (state.isLoading) Loading()
            if (state.chapter == null && !state.isLoading) {
                ErrorScreen {
                    viewModel.sendIntent(BibleIntent.LoadChapter(bookName, bookAbbrev, state.currentChapter))
                }
            }
            val configuration = LocalConfiguration.current
            val isWideScreen = configuration.screenWidthDp > 600
            val verses = state.chapter?.verses ?: emptyList()

            if (isWideScreen) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    items(verses) { verse ->
                        VerseItem(
                            verse,
                            state.fontSize,
                            { toggleNavigationMenusVisibility(showBottomBar) },
                        ) {
                            viewModel.sendIntent(BibleIntent.SetSelectedVerse(verse))
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        if (showBottomBar.value) {
                            Spacer(modifier = Modifier.height(64.dp))
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            } else {
                LazyColumn {
                    items(verses) { verse ->
                        VerseItem(
                            verse,
                            state.fontSize,
                            { toggleNavigationMenusVisibility(showBottomBar) },
                        ) {
                            viewModel.sendIntent(BibleIntent.SetSelectedVerse(verse))
                        }
                    }
                    if (showBottomBar.value) {
                        item { Spacer(modifier = Modifier.height(64.dp)) }
                    } else {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
            state.selectedVerse?.let {
                ShareVerseMenu(verse = it, viewModel, bookName, state.currentChapter)
            }
            BottomMenu(
                viewModel,
                state.isSpeechEnabled,
                state.currentText,
                chapterQuantity,
                state.currentChapter,
                showBottomBar,
            )
        }
    }
}

fun toggleNavigationMenusVisibility(showBottomBar: MutableState<Boolean>) {
    showBottomBar.value = !showBottomBar.value
}

@Composable
fun BottomMenu(
    viewModel: BibleViewModel,
    isSpeechEnable: Boolean,
    currentText: String,
    chapterQuantity: Int,
    currentChapter: Int,
    showBottomBar: MutableState<Boolean>,
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        DropdownMenuItem(onClick = { /* Handle refresh! */ }) {
            Row(
                modifier =
                    Modifier.clickable {
                        viewModel.sendIntent(BibleIntent.DecreaseFontSize)
                    },
            ) {
                Text(
                    text = stringResource(id = R.string.decrease),
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_minus),
                    contentDescription = null,
                )
            }
        }
        Divider()
        DropdownMenuItem(onClick = { /* Handle send feedback! */ }) {
            Row(
                modifier =
                    Modifier.clickable {
                        viewModel.sendIntent(BibleIntent.IncreaseFontSize)
                    },
            ) {
                Text(
                    text = stringResource(id = R.string.increase),
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                )
            }
        }
    }
    AnimatedVisibility(
        visible = showBottomBar.value,
        enter = slideInVertically(initialOffsetY = { 40 }),
        exit = slideOutVertically(targetOffsetY = { 40 }),
    ) {
        Card(
            elevation = 8.dp,
            modifier =
                Modifier
                    .wrapContentSize(align = Alignment.BottomCenter)
                    .fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier =
                        Modifier.clickable {
                            if (currentChapter > 1) {
                                viewModel.sendIntent(BibleIntent.PreviousChapter)
                            }
                        },
                )
                Row(
                    modifier =
                        Modifier.clickable {
                            if (!isSpeechEnable) {
                                viewModel.sendIntent(BibleIntent.TextToSpeech(context, currentText))
                            } else {
                                viewModel.sendIntent(BibleIntent.StopSpeech)
                            }
                        },
                ) {
                    Icon(
                        imageVector = if (isSpeechEnable) Icons.Filled.Clear else Icons.Filled.PlayArrow,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text =
                            if (isSpeechEnable) {
                                stringResource(id = R.string.stop_speech)
                            } else {
                                stringResource(
                                    id = R.string.speech,
                                )
                            },
                    )
                }
                Row(
                    modifier =
                        Modifier.clickable {
                            expanded = !expanded
                        },
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_font_size),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = stringResource(id = R.string.font_size),
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier =
                        Modifier.clickable {
                            if (chapterQuantity > currentChapter) {
                                viewModel.sendIntent(BibleIntent.NextChapter)
                            }
                        },
                )
            }
        }
    }
}

@Composable
fun VerseItem(
    verse: Verse,
    fontSize: TextUnit,
    onTap: () -> Unit,
    onLongClick: ((verse: Verse) -> Unit)? = null,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        if (onLongClick != null) {
                            onLongClick(verse)
                        }
                    }, onTap = { onTap() })
                },
    ) {
        Text(text = "${verse.number}. ${verse.text}", fontSize = fontSize)
    }
}

@Composable
fun ShareVerseMenu(
    verse: Verse,
    viewModel: BibleViewModel,
    bookName: String,
    chapter: Int,
) {
    val context = LocalContext.current

    var expandedShareVerseMenu by remember { mutableStateOf(true) }

    DropdownMenu(
        expanded = expandedShareVerseMenu,
        onDismissRequest = {
            expandedShareVerseMenu = false
            viewModel.sendIntent(BibleIntent.ClearSelectedVerse)
        },
    ) {
        DropdownMenuItem(onClick = {
            shareVerseIntent(verse, context, bookName = bookName, chapter = chapter)
        }) {
            Row {
                Text(
                    text = stringResource(id = R.string.share),
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = null,
                )
            }
        }
    }
}

private fun shareVerseIntent(
    verse: Verse,
    context: Context,
    bookName: String,
    chapter: Int,
) {
    val shareIntent =
        Intent().apply {
            action = Intent.ACTION_SEND
            val line1 = "${verse.text} - $bookName $chapter:${verse.number}"
            val line2 = "\n\n${context.getString(R.string.download_now_at_play_store)} $PLAY_STORE_URL"
            putExtra(
                Intent.EXTRA_TEXT,
                line1 + line2,
            )

            type = "text/plain"
        }

    val shareIntentChooser =
        Intent.createChooser(shareIntent, context.getString(R.string.share_verse))
    context.startActivity(shareIntentChooser)
}
