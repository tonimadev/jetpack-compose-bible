package digital.tonima.bibliadigital.feature.bible.impl.reading

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import digital.tonima.bibliadigital.core.common.constants.PLAY_STORE_URL
import digital.tonima.bibliadigital.core.common.model.Verse
import digital.tonima.bibliadigital.core.ui.R
import digital.tonima.bibliadigital.core.ui.components.AppBar
import digital.tonima.bibliadigital.core.ui.components.ErrorScreen
import digital.tonima.bibliadigital.core.ui.components.Loading
import digital.tonima.bibliadigital.feature.bible.bridge.BibleIntent.ClearSelectedVerse
import digital.tonima.bibliadigital.feature.bible.bridge.BibleIntent.DisableTutorial
import digital.tonima.bibliadigital.feature.bible.bridge.BibleIntent.LoadChapter
import digital.tonima.bibliadigital.feature.bible.bridge.BibleIntent.PauseSpeech
import digital.tonima.bibliadigital.feature.bible.bridge.BibleIntent.ResumeSpeech
import digital.tonima.bibliadigital.feature.bible.bridge.BibleIntent.SetSelectedVerse
import digital.tonima.bibliadigital.feature.bible.bridge.BibleIntent.StopSpeech
import digital.tonima.bibliadigital.feature.bible.bridge.BibleIntent.TextToSpeech
import digital.tonima.bibliadigital.feature.bible.bridge.BibleIntent.ToggleFavorite
import digital.tonima.bibliadigital.feature.bible.impl.BibleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val isWideScreen = with(density) { windowInfo.containerSize.width.toDp() > 600.dp }

    val nestedScrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (available.y < -10f) {
                        showBottomBar.value = false
                    } else if (available.y > 10f) {
                        showBottomBar.value = true
                    }
                    return Offset.Zero
                }
            }
        }

    // Load initial chapter and react to page changes
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onIntent(LoadChapter(bookName, bookAbbrev, pagerState.currentPage + 1))
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            ReadingSettingsSheet(
                viewModel = viewModel,
                selectedVersion = state.selectedVersion,
                versions = state.versions,
            )
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
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
                        viewModel.onIntent(LoadChapter(bookName, bookAbbrev, state.currentChapter))
                    }
                } else if (isCurrentPage) {
                    val verses = state.chapter?.verses ?: emptyList()

                    if (isWideScreen) {
                        LazyVerticalGrid(
                            // Grid Adaptativo para melhor responsividade
                            columns = GridCells.Adaptive(minSize = 350.dp),
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
                                    viewModel.onIntent(SetSelectedVerse(verse))
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
                                    viewModel.onIntent(SetSelectedVerse(verse))
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
                onDismissRequest = { viewModel.onIntent(DisableTutorial) },
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.verse_long_press_tutorial),
                        )
                    },
                    onClick = { },
                )
            }
            state.selectedVerse?.let {
                ShareVerseMenu(verse = it, viewModel, bookName, state.currentChapter)
            }
            BottomMenu(
                viewModel = viewModel,
                isSpeechEnable = state.isSpeechEnabled,
                isSpeechPaused = state.isSpeechPaused,
                currentText = state.currentText,
                bookName = bookName,
                currentChapter = state.currentChapter,
                chapterQuantity = chapterQuantity,
                showBottomBar = showBottomBar,
                selectedVersion = state.selectedVersion,
                pagerState = pagerState,
                scope = scope,
                onSettingsClick = {
                    showSheet = true
                },
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
    isSpeechPaused: Boolean,
    currentText: String,
    bookName: String,
    currentChapter: Int,
    chapterQuantity: Int,
    showBottomBar: MutableState<Boolean>,
    selectedVersion: String,
    pagerState: PagerState,
    scope: CoroutineScope,
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = showBottomBar.value,
        enter = slideInVertically(initialOffsetY = { 40 }),
        exit = slideOutVertically(targetOffsetY = { 40 }),
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                when {
                                    !isSpeechEnable -> {
                                        viewModel.onIntent(
                                            TextToSpeech(
                                                context,
                                                currentText,
                                                bookName,
                                                currentChapter,
                                            ),
                                        )
                                    }
                                    isSpeechPaused -> viewModel.onIntent(ResumeSpeech)
                                    else -> viewModel.onIntent(PauseSpeech)
                                }
                            }
                            .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector =
                            if (isSpeechEnable && !isSpeechPaused) {
                                Icons.Filled.Clear // Using Clear as Stop icon, maybe change to Pause icon
                            } else {
                                Icons.Filled.PlayArrow
                            },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text =
                            when {
                                !isSpeechEnable -> stringResource(id = R.string.speech)
                                isSpeechPaused -> stringResource(id = R.string.play)
                                else -> stringResource(id = R.string.pause)
                            },
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Stop button only when speech is enabled
                if (isSpeechEnable) {
                    IconButton(onClick = { viewModel.onIntent(StopSpeech) }) {
                        Icon(Icons.Filled.Clear, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
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
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
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
            viewModel.onIntent(ClearSelectedVerse)
        },
    ) {
        DropdownMenuItem(
            text = { Text("Favoritar") },
            leadingIcon = {
                val isFavorite = false // Need to check from state
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Clear else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = {
                toggleFavorite(verse, viewModel, bookName, chapter)
                expandedShareVerseMenu = false
            },
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.share)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = {
                shareVerseIntent(verse, context, bookName = bookName, chapter = chapter)
                expandedShareVerseMenu = false
            },
        )
    }
}

private fun toggleFavorite(
    verse: Verse,
    viewModel: BibleViewModel,
    bookName: String,
    chapter: Int,
) {
    viewModel.onIntent(ToggleFavorite(verse, bookName, chapter))
    viewModel.onIntent(ClearSelectedVerse)
}

private fun shareVerseIntent(
    verse: Verse,
    context: Context,
    bookName: String,
    chapter: Int,
) {
    val shareIntent =
        Intent().apply {
            action = ACTION_SEND
            val line1 = "${verse.text} - $bookName $chapter:${verse.number}"
            val line2 = "\n\n${context.getString(R.string.download_now_at_play_store)} $PLAY_STORE_URL"
            putExtra(
                EXTRA_TEXT,
                line1 + line2,
            )

            type = "text/plain"
        }

    val shareIntentChooser =
        Intent.createChooser(shareIntent, context.getString(R.string.share_verse))
    context.startActivity(shareIntentChooser)
}
