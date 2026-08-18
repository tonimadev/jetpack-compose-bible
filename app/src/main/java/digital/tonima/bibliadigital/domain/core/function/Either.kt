package digital.tonima.bibliadigital.domain.core.function

import digital.tonima.bibliadigital.domain.core.function.Either.Fail
import digital.tonima.bibliadigital.domain.core.function.Either.Success

/**
 * Represents a value of one of two possible types (a disjoint union).
 * Instances of [Either] are either an instance of [Fail] or [Success].
 * FP Convention dictates that [Fail] is used for "failure"
 * and [Success] is used for "success".
 *
 * @see Fail
 * @see Success
 */
sealed class Either<out FAIL, out SUCCESS> {
    /** * Represents the left side of [Either] class which by convention is a "Failure". */
    data class Fail<out FAIL>(val a: FAIL) : Either<FAIL, Nothing>()

    /** * Represents the right side of [Either] class which by convention is a "Success". */
    data class Success<out SUCCESS>(val b: SUCCESS) : Either<Nothing, SUCCESS>()

    /**
     * Returns true if this is a Right, false otherwise.
     * @see Success
     */
    val isSuccess get() = this is Success<SUCCESS>

    /**
     * Returns true if this is a Left, false otherwise.
     * @see Fail
     */
    val isFail get() = this is Fail<FAIL>

    /**
     * Creates a Left type.
     * @see Fail
     */
    fun <FAIL> fail(a: FAIL) = Fail(a)

    /**
     * Creates a Right type.
     * @see Success
     */
    fun <SUCCESS> success(b: SUCCESS) = Success(b)

    /**
     * Applies fnL if this is a Left or fnR if this is a Right.
     * @see Fail
     * @see Success
     */
    fun <T> fold(
        fnL: (FAIL) -> T,
        fnR: (SUCCESS) -> T,
    ): T =
        when (this) {
            is Fail -> fnL(a)
            is Success -> fnR(b)
        }
}

/**
 * Left-biased onFailure() FP convention dictates that when this class is Left, it'll perform
 * the onFailure functionality passed as a parameter, but, overall will still return an either
 * object so you chain calls.
 */
fun <FAIL, SUCCESS> Either<FAIL, SUCCESS>.onFailure(fn: (failure: FAIL) -> Unit): Either<FAIL, SUCCESS> =
    this.apply { if (this is Fail) fn(a) }

/**
 * Right-biased onSuccess() FP convention dictates that when this class is Right, it'll perform
 * the onSuccess functionality passed as a parameter, but, overall will still return an either
 * object so you chain calls.
 */
fun <FAIL, SUCCESS> Either<FAIL, SUCCESS>.onSuccess(fn: (success: SUCCESS) -> Unit): Either<FAIL, SUCCESS> =
    this.apply { if (this is Success) fn(b) }
