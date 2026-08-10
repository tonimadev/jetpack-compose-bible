package digital.tonima.bibliadigital.ui.bible.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import digital.tonima.bibliadigital.R
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.ui.bible.BibleIntent.ClearFilteredBooks
import digital.tonima.bibliadigital.ui.bible.BibleIntent.LoadBooks
import digital.tonima.bibliadigital.ui.bible.BibleIntent.SearchBook
import digital.tonima.bibliadigital.ui.bible.BibleIntent.UpdateLastSearch
import digital.tonima.bibliadigital.ui.bible.BibleViewModel
import digital.tonima.bibliadigital.ui.components.AppBar
import digital.tonima.bibliadigital.ui.components.ErrorScreen
import digital.tonima.bibliadigital.ui.components.Loading

@Composable
fun ListBooks(
    viewModel: BibleViewModel,
    navController: NavController,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val textState = remember { mutableStateOf(TextFieldValue(state.lastSearch)) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(topBar = {
        AppBar(
            title = stringResource(id = R.string.app_name),
            icon = Icons.Default.Home,
        )
    }) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            if (state.isLoading) Loading()
            Column {
                if (state.books.isEmpty() && !state.isLoading) {
                    ErrorScreen { viewModel.sendIntent(LoadBooks) }
                }
                SearchView(
                    state = textState,
                    onSearch = { query ->
                        viewModel.sendIntent(UpdateLastSearch(query))
                        viewModel.sendIntent(SearchBook(query))
                    },
                    onDeleteClick = {
                        textState.value = TextFieldValue("")
                        viewModel.sendIntent(UpdateLastSearch(""))
                        viewModel.sendIntent(SearchBook(""))
                        viewModel.sendIntent(ClearFilteredBooks)
                    },
                )
                LazyColumn {
                    items(state.filteredBooks ?: state.books) { book ->
                        BookItem(book, state.fontSize) {
                            keyboardController?.hide()
                            navController.navigate("chapters_list/${book.name}/${book.abbrev}/${book.chapters}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchView(
    state: MutableState<TextFieldValue>,
    onSearch: (String) -> Unit,
    onDeleteClick: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = state.value,
            onValueChange = { value: TextFieldValue ->
                state.value = value
                onSearch(value.text)
            },
            label = {
                Text(stringResource(id = R.string.find_book))
            },
            trailingIcon = {
                if (state.value.text.isNotBlank()) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier =
                            Modifier.clickable {
                                onDeleteClick()
                            },
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = { keyboardController?.hide() },
                ),
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookItem(
    book: BookResponse,
    fontSize: TextUnit,
    onBookClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onBookClick,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = book.name, fontSize = fontSize)
            Text(text = book.abbrev, fontSize = fontSize)
        }
    }
}
