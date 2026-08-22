package digital.tonima.bibliadigital.core.common.core.network

interface DomainModel<out T> {
    fun toDomain(): T
}
