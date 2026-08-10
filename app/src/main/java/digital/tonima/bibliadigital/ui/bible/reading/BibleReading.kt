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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
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
    val pagerState = rememberPagerState(initialPage = chapterId - 1) { chapterQuantity }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    // Load initial chapter and react to page changes
    LaunchedEffect(pagerState.currentPage) {
        viewModel.sendIntent(BibleIntent.LoadChapter(bookName, bookAbbrev, pagerState.currentPage + 1))
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.sendIntent(BibleIntent.StopSpeech)
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            ReadingSettingsSheet(
                viewModel = viewModel,
                selectedVersion = state.selectedVersion,
                versions = state.versions,
            )
        },
    ) {
        Scaffold(
            topBar = {
                AnimatedVisibility(
                    visible = showBottomBar.value,
                    enter = slideInVertically(initialOffsetY = { -40 }),
                    exit = slideOutVertically(targetOffsetY = { -40 }),
                ) {
                    AppBar(
                        title = "$bookName - ${stringResource(id = R.string.chapter)} ${state.currentChapter}",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                    ) {
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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                ) { page ->
                    val isCurrentPage = state.currentChapter == page + 1

                    if (state.isLoading && isCurrentPage) {
                        Loading()
                    } else if (state.chapter == null && isCurrentPage) {
                        ErrorScreen {
                            viewModel.sendIntent(BibleIntent.LoadChapter(bookName, bookAbbrev, state.currentChapter))
                        }
                    } else if (isCurrentPage) {
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
                                        Spacer(modifier = Modifier.height(80.dp))
                                    } else {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                                    item { Spacer(modifier = Modifier.height(80.dp)) }
                                } else {
                                    item { Spacer(modifier = Modifier.height(16.dp)) }
                                }
                            }
                        }
                    } else {
                        Loading()
                    }
                }

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
                state.selectedVerse?.let {
                    ShareVerseMenu(verse = it, viewModel, bookName, state.currentChapter)
                }
                BottomMenu(
                    viewModel,
                    state.isSpeechEnabled,
                    state.currentText,
                    chapterQuantity,
                    showBottomBar,
                    state.selectedVersion,
                    pagerState = pagerState,
                    scope = scope,
                    onSettingsClick = {
                        scope.launch { sheetState.show() }
                    },
                )
            }
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
    showBottomBar: MutableState<Boolean>,
    selectedVersion: String,
    pagerState: androidx.compose.foundation.pager.PagerState,
    scope: kotlinx.coroutines.CoroutineScope,
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current

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
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp),
            ) {
                IconButton(onClick = {
                    if (pagerState.currentPage > 0) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }

                Row(
                    modifier =
                        Modifier
                            .clickable {
                                if (!isSpeechEnable) {
                                    viewModel.sendIntent(BibleIntent.TextToSpeech(context, currentText))
                                } else {
                                    viewModel.sendIntent(BibleIntent.StopSpeech)
                                }
                            }
                            .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (isSpeechEnable) Icons.Filled.Clear else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text =
                            if (isSpeechEnable) {
                                stringResource(id = R.string.stop_speech)
                            } else {
                                stringResource(id = R.string.speech)
                            },
                        fontWeight = FontWeight.Medium,
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .clickable { onSettingsClick() }
                            .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedVersion.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }

                IconButton(onClick = {
                    if (pagerState.currentPage < chapterQuantity - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
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
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongClick?.invoke(verse) },
                        onTap = { onTap() },
                    )
                },
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "${verse.number}",
                fontSize = (fontSize.value * 0.8f).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary.copy(alpha = 0.5f),
                modifier = Modifier.width(24.dp),
            )
            Text(
                text = verse.text,
                fontSize = fontSize,
                lineHeight = (fontSize.value * 1.5f).sp,
            )
        }
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
            toggleFavorite(verse, viewModel, bookName, chapter)
            expandedShareVerseMenu = false
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isFavorite = false // Need to check from state
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Clear else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Favoritar")
            }
        }
        Divider()
        DropdownMenuItem(onClick = {
            shareVerseIntent(verse, context, bookName = bookName, chapter = chapter)
            expandedShareVerseMenu = false
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.share))
            }
        }
    }
}

private fun toggleFavorite(
    verse: Verse,
    viewModel: BibleViewModel,
    bookName: String,
    chapter: Int,
) {
    viewModel.sendIntent(BibleIntent.ToggleFavorite(verse, bookName, chapter))
    viewModel.sendIntent(BibleIntent.ClearSelectedVerse)
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
