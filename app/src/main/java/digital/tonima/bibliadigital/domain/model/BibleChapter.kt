package digital.tonima.bibliadigital.domain.model

data class BibleChapter(
    val version: String,
    val book: Book,
    val chapter: Chapter,
    val verses: List<Verse>,
)
