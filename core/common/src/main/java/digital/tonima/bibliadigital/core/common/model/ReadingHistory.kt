package digital.tonima.bibliadigital.core.common.model

data class ReadingHistory(
    val bookName: String,
    val bookAbbrev: String,
    val chapterId: Int,
    val chapterQuantity: Int,
)
