package digital.tonima.bibliadigital.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.bibliadigital.R
import digital.tonima.bibliadigital.domain.common.constants.ARG_BOOK_ABBREV
import digital.tonima.bibliadigital.domain.common.constants.ARG_BOOK_NAME
import digital.tonima.bibliadigital.domain.common.constants.ARG_CHAPTER_ID
import digital.tonima.bibliadigital.domain.common.constants.ARG_CHAPTER_QUANTITY
import digital.tonima.bibliadigital.domain.core.exception.Failure
import digital.tonima.bibliadigital.ui.bible.BibleViewModel
import digital.tonima.bibliadigital.ui.bible.books.ListBooks
import digital.tonima.bibliadigital.ui.bible.chapters.ListChapters
import digital.tonima.bibliadigital.ui.bible.reading.BibleReading
import digital.tonima.bibliadigital.ui.theme.BibliaSagradaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val viewModel: BibleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                viewModel.getBooks()
            }
        callRequestPermissions()

        setContent {
            BibliaSagradaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    BibleApplication(viewModel)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.stopSpeech()
    }

    private fun callRequestPermissions() {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.INTERNET,
            ),
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BibleApplication(viewModel: BibleViewModel) {
    val failure: State<Failure?> = viewModel.failure.observeAsState(initial = null)
    val loading: State<Boolean> = viewModel.loading.observeAsState(initial = true)

    val keyboardController = LocalSoftwareKeyboardController.current

    failure.value?.let {
        when (it) {
            is Failure.NetworkConnection -> {
                Toast.makeText(
                    LocalContext.current,
                    stringResource(R.string.no_network),
                    Toast.LENGTH_LONG,
                )
                    .show()
            }
            is Failure.ServerError -> {
                Toast.makeText(
                    LocalContext.current,
                    stringResource(R.string.server_error),
                    Toast.LENGTH_LONG,
                ).show()
            }
            else -> {
                Toast.makeText(
                    LocalContext.current,
                    stringResource(R.string.unknown_error),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ListBooksScreen.route) {
        composable(route = ListBooksScreen.route) {
            ListBooks(viewModel, navController, loading, keyboardController)
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
                navBackStackEntry.arguments?.getString(
                    ARG_BOOK_NAME,
                )!!,
                navBackStackEntry.arguments?.getString(
                    ARG_BOOK_ABBREV,
                )!!,
                navBackStackEntry.arguments?.getInt(
                    ARG_CHAPTER_QUANTITY,
                )!!,
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
                navBackStackEntry.arguments?.getString(
                    ARG_BOOK_NAME,
                )!!,
                navBackStackEntry.arguments?.getString(
                    ARG_BOOK_ABBREV,
                )!!,
                navBackStackEntry.arguments!!.getInt(ARG_CHAPTER_ID),
                navBackStackEntry.arguments!!.getInt(ARG_CHAPTER_QUANTITY),
                navController,
                viewModel,
                loading,
            )
        }
    }
}
