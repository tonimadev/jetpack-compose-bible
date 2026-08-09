package digital.tonima.bibliadigital.domain.core.exception

sealed class Failure {
    object NetworkConnection : Failure()

    object ServerError : Failure()

    object Error : Failure()
}
