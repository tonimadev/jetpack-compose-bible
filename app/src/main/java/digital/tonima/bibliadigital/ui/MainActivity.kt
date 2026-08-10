package digital.tonima.bibliadigital.ui

import android.Manifest.permission.INTERNET
import android.Manifest.permission.POST_NOTIFICATIONS
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.bibliadigital.R
import digital.tonima.bibliadigital.domain.common.constants.ARG_BOOK_ABBREV
import digital.tonima.bibliadigital.domain.common.constants.ARG_BOOK_NAME
import digital.tonima.bibliadigital.domain.common.constants.ARG_CHAPTER_ID
import digital.tonima.bibliadigital.domain.common.constants.ARG_CHAPTER_QUANTITY
import digital.tonima.bibliadigital.domain.core.exception.Failure.NetworkConnection
import digital.tonima.bibliadigital.domain.core.exception.Failure.ServerError
import digital.tonima.bibliadigital.ui.bible.BibleIntent
import digital.tonima.bibliadigital.ui.bible.BibleViewModel
import digital.tonima.bibliadigital.ui.bible.books.ListBooks
import digital.tonima.bibliadigital.ui.bible.chapters.ListChapters
import digital.tonima.bibliadigital.ui.bible.reading.BibleReading
import digital.tonima.bibliadigital.ui.components.MiniPlayer
import digital.tonima.bibliadigital.ui.theme.BibliaSagradaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val viewModel: BibleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                viewModel.sendIntent(BibleIntent.LoadBooks)
            }
        callRequestPermissions()
        viewModel.sendIntent(BibleIntent.BindTTS(this))

        setContent {
            BibliaSagradaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    BibleApplication(viewModel)
                }
            }
        }
    }

    private fun callRequestPermissions() {
        val permissions = mutableListOf(INTERNET)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }
}

@Composable
fun BibleApplication(viewModel: BibleViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    state.failure?.let {
        when (it) {
            is NetworkConnection -> {
                makeText(
                    LocalContext.current,
                    stringResource(R.string.no_network),
                    Toast.LENGTH_LONG,
                ).show()
            }
            is ServerError -> {
                makeText(
                    LocalContext.current,
                    stringResource(R.string.server_error),
                    Toast.LENGTH_LONG,
                ).show()
            }
            else -> {
                makeText(
                    LocalContext.current,
                    stringResource(R.string.unknown_error),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = ListBooksScreen.route,
                modifier = Modifier.weight(1f),
            ) {
                composable(route = ListBooksScreen.route) {
                    ListBooks(viewModel, navController)
                }
                composable(
                    route = ListChaptersScreen.route,
                    arguments =
                        listOf(
                            navArgument(ARG_BOOK_NAME) {
                                type = NavType.StringType
                            },
                            navArgument(ARG_BOOK_ABBREV) {
                                type = NavType.StringType
                            },
                            navArgument(ARG_CHAPTER_QUANTITY) {
                                type = NavType.IntType
                            },
                        ),
                ) { navBackStackEntry ->
                    ListChapters(
                        navBackStackEntry.arguments?.getString(ARG_BOOK_NAME)!!,
                        navBackStackEntry.arguments?.getString(ARG_BOOK_ABBREV)!!,
                        navBackStackEntry.arguments?.getInt(ARG_CHAPTER_QUANTITY)!!,
                        navController,
                        viewModel,
                    )
                }
                composable(
                    route = BibleReadingScreen.route,
                    arguments =
                        listOf(
                            navArgument(ARG_BOOK_NAME) {
                                type = NavType.StringType
                            },
                            navArgument(ARG_BOOK_ABBREV) {
                                type = NavType.StringType
                            },
                            navArgument(ARG_CHAPTER_ID) {
                                type = NavType.IntType
                            },
                            navArgument(ARG_CHAPTER_QUANTITY) {
                                type = NavType.IntType
                            },
                        ),
                ) { navBackStackEntry ->
                    BibleReading(
                        navBackStackEntry.arguments?.getString(ARG_BOOK_NAME)!!,
                        navBackStackEntry.arguments?.getString(ARG_BOOK_ABBREV)!!,
                        navBackStackEntry.arguments!!.getInt(ARG_CHAPTER_ID),
                        navBackStackEntry.arguments!!.getInt(ARG_CHAPTER_QUANTITY),
                        navController,
                        viewModel,
                    )
                }
            }

            // Global Mini Player
            if (state.isSpeechEnabled && currentRoute != BibleReadingScreen.route) {
                MiniPlayer(
                    bookName = state.playingBookName ?: "",
                    chapterId = state.playingChapterId ?: 1,
                    isPaused = state.isSpeechPaused,
                    onPlayPause = {
                        if (state.isSpeechPaused) {
                            viewModel.sendIntent(BibleIntent.ResumeSpeech)
                        } else {
                            viewModel.sendIntent(BibleIntent.PauseSpeech)
                        }
                    },
                    onStop = {
                        viewModel.sendIntent(BibleIntent.StopSpeech)
                    },
                    onClick = {
                        navController.navigate(
                            BibleReadingScreen.route
                                .replace("{$ARG_BOOK_NAME}", state.playingBookName ?: "")
                                .replace("{$ARG_BOOK_ABBREV}", state.playingBookAbbrev ?: "")
                                .replace("{$ARG_CHAPTER_ID}", (state.playingChapterId ?: 1).toString())
                                .replace("{$ARG_CHAPTER_QUANTITY}", (state.playingChapterQuantity ?: 50).toString()),
                        ) {
                            popUpTo(ListBooksScreen.route)
                        }
                    },
                )
            }
        }
    }
}
