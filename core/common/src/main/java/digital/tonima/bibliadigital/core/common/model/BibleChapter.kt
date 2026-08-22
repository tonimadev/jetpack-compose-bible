package digital.tonima.bibliadigital.core.common.model

data class BibleChapter(
    val version: String,
    val book: Book,
    val chapter: Chapter,
    val verses: List<Verse>,
)
