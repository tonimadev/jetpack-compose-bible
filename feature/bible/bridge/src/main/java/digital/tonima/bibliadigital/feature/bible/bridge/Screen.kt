package digital.tonima.bibliadigital.feature.bible.bridge

import digital.tonima.bibliadigital.core.common.constants.ARG_BOOK_ABBREV
import digital.tonima.bibliadigital.core.common.constants.ARG_BOOK_NAME
import digital.tonima.bibliadigital.core.common.constants.ARG_CHAPTER_ID
import digital.tonima.bibliadigital.core.common.constants.ARG_CHAPTER_QUANTITY

sealed class Screen(val route: String)

object ListBooksScreen : Screen("list_books")

object ListChaptersScreen :
    Screen("chapters_list/{$ARG_BOOK_NAME}/{$ARG_BOOK_ABBREV}/{$ARG_CHAPTER_QUANTITY}")

object BibleReadingScreen :
    Screen("reading/{$ARG_BOOK_NAME}/{$ARG_BOOK_ABBREV}/{$ARG_CHAPTER_ID}/{$ARG_CHAPTER_QUANTITY}")
