package digital.tonima.bibliadigital.domain.core.network

interface DomainModel<out T> {
    fun toDomain(): T
}
