package digital.tonima.bibliadigital.ui.bible.books

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import digital.tonima.bibliadigital.R
import digital.tonima.bibliadigital.domain.model.Book
import digital.tonima.bibliadigital.ui.bible.BibleIntent.ClearFilteredBooks
import digital.tonima.bibliadigital.ui.bible.BibleIntent.LoadBooks
import digital.tonima.bibliadigital.ui.bible.BibleIntent.SearchBook
import digital.tonima.bibliadigital.ui.bible.BibleIntent.UpdateLastSearch
import digital.tonima.bibliadigital.ui.bible.BibleViewModel
import digital.tonima.bibliadigital.ui.components.AppBar
import digital.tonima.bibliadigital.ui.components.ErrorScreen
import digital.tonima.bibliadigital.ui.components.Loading

@OptIn(ExperimentalFoundationApi::class)
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
            icon = Icons.AutoMirrored.Filled.MenuBook,
        )
    }) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (state.isLoading && state.books.isEmpty()) Loading()

            Column {
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

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (state.lastSearch.isBlank()) {
                        state.history?.let { history ->
                            item {
                                HistoryCard(history) {
                                    navController.navigate(
                                        "reading" +
                                            "/${history.bookName}" +
                                            "/${history.bookAbbrev}" +
                                            "/${history.chapterId}" +
                                            "/${history.chapterQuantity}",
                                    )
                                }
                            }
                        }
                    }

                    val booksToDisplay = state.filteredBooks ?: state.books

                    if (booksToDisplay.isEmpty() && !state.isLoading) {
                        item {
                            ErrorScreen { viewModel.sendIntent(LoadBooks) }
                        }
                    }

                    val groupedBooks = booksToDisplay.groupBy { it.testament }

                    groupedBooks.forEach { (testament, books) ->
                        stickyHeader {
                            TestamentHeader(testament)
                        }
                        items(books) { book ->
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
}

@Composable
fun TestamentHeader(testament: String) {
    val title = if (testament == "VT") "Velho Testamento" else "Novo Testamento"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun HistoryCard(
    history: digital.tonima.bibliadigital.data.datastore.ReadingHistory,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Continuar lendo",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                )
                Text(
                    text = "${history.bookName}, Capítulo ${history.chapterId}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
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

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp,
    ) {
        TextField(
            value = state.value,
            onValueChange = { value: TextFieldValue ->
                state.value = value
                onSearch(value.text)
            },
            placeholder = {
                Text(stringResource(id = R.string.find_book), color = Color.Gray)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
            },
            trailingIcon = {
                if (state.value.text.isNotBlank()) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.clickable { onDeleteClick() },
                        tint = Color.Gray,
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        )
    }
}

@Composable
fun BookItem(
    book: Book,
    fontSize: TextUnit,
    onBookClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onBookClick,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = book.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${book.chapters} capítulos",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    text = book.abbrev.uppercase(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
