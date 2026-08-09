package digital.tonima.bibliadigital.domain.core.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import digital.tonima.bibliadigital.domain.core.exception.Failure

fun <T> LifecycleOwner.observe(
    liveData: LiveData<T>,
    action: (t: T) -> Unit,
) = liveData.observe(this) { it?.let { action(it) } }

fun <L : LiveData<Failure>> LifecycleOwner.failure(
    liveData: L,
    body: (Failure) -> Unit,
) = liveData.observe(this) { it?.let { body(it) } }
