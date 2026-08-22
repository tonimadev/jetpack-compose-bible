package digital.tonima.bibliadigital.core.common.core.exception

sealed class Failure {
    object NetworkConnection : Failure()

    object ServerError : Failure()

    object Error : Failure()
}
