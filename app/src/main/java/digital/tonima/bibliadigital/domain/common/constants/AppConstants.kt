package digital.tonima.bibliadigital.domain.common.constants

import digital.tonima.bibliadigital.BuildConfig

const val PLATFORM = "android"
const val SOCKET_TIMEOUT = 5000L

// destination parameters
const val ARG_BOOK_NAME = "book_name"
const val ARG_BOOK_ABBREV = "book_abbrev"
const val ARG_CHAPTER_QUANTITY = "chapter_quantity"
const val ARG_CHAPTER_ID = "chapterId"

// font size
const val MIN_FONT_SIZE = 8
const val MAX_FONT_SIZE = 128
const val STD_FONT_SIZE = 18

// playStore
const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
