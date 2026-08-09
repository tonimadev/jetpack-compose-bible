package digital.tonima.bibliadigital.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import digital.tonima.bibliadigital.domain.core.exception.Failure
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    private val _failure: MutableLiveData<Failure> = MutableLiveData()
    private val _loading: MutableLiveData<Boolean> = MutableLiveData()

    val failure: LiveData<Failure> = _failure
    val loading: LiveData<Boolean> = _loading

    protected fun handleFailure(failure: Failure) {
        handleLoading(false)
        _failure.postValue(failure)
        viewModelScope.launch {
            delay(1000)
            _failure.postValue(null)
        }
    }

    protected fun handleLoading(isLoading: Boolean) {
        _loading.postValue(isLoading)
    }
}
