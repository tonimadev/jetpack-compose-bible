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
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import digital.tonima.bibliadigital.R
import digital.tonima.bibliadigital.domain.model.BookResponse
import digital.tonima.bibliadigital.ui.bible.BibleViewModel
import digital.tonima.bibliadigital.ui.components.AppBar
import digital.tonima.bibliadigital.ui.components.ErrorScreen
import digital.tonima.bibliadigital.ui.components.Loading

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ListBooks(
    viewModel: BibleViewModel,
    navController: NavController,
    loading: State<Boolean>,
    keyboardController: SoftwareKeyboardController?,
) {
    val fontSizeState: State<TextUnit> = viewModel.fontSize.observeAsState(initial = 16.sp)

    val textState =
        remember { mutableStateOf(viewModel.lastSearch.value?.let { TextFieldValue(it) }) }

    val booksState: State<List<BookResponse>> =
        viewModel.books.observeAsState(initial = emptyList())
    val filteredBooksState: State<List<BookResponse>?> =
        viewModel.filteredBooks.observeAsState(
            initial = null,
        )

    Scaffold(topBar = {
        AppBar(
            stringResource(id = R.string.app_name),
            icon = Icons.Default.Home,
        )
    }) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (loading.value) Loading()
            Column {
                if (booksState.value.isEmpty() && !loading.value) ErrorScreen { viewModel.getBooks() }
                SearchView(textState, viewModel, keyboardController) {
                    textState.value = TextFieldValue("")
                    viewModel.searchBook("")
                }
                LazyColumn {
                    items(filteredBooksState.value ?: booksState.value) { book ->
                        BookItem(book, fontSizeState) {
                            keyboardController?.hide()
                            navController.navigate("chapters_list/${book.name}/${book.abbrev.pt}/${book.chapters}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchView(
    state: MutableState<TextFieldValue?>,
    viewModel: BibleViewModel,
    keyboardController: SoftwareKeyboardController?,
    onDeleteClick: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        state.value?.let {
            TextField(
                value = it,
                onValueChange = { value: TextFieldValue ->
                    state.value = value
                    viewModel.updateLastSearch(value.text)
                    if (value.text.isBlank()) {
                        viewModel.clearFilteredBooks()
                    } else {
                        viewModel.searchBook(
                            value.text,
                        )
                    }
                },
                label = {
                    Text(stringResource(id = R.string.find_book))
                },
                trailingIcon = {
                    if (state.value?.text?.isBlank() == false) {
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
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookItem(
    book: BookResponse,
    fontSizeState: State<TextUnit>,
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
            Text(text = book.name, fontSize = fontSizeState.value)
            Text(text = book.abbrev.pt, fontSize = fontSizeState.value)
        }
    }
}
