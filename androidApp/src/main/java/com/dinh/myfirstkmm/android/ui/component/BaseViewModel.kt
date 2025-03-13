package com.dinh.myfirstkmm.android.ui.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

sealed class BaseState {
    data class Error(val mess: String) : BaseState()
    data class Message(val mess: String) : BaseState()
}

interface BaseOutput
abstract class BaseViewModel : ViewModel() {

    protected val _notification: MutableSharedFlow<BaseState> = MutableSharedFlow()
    val notification get() = _notification

    abstract val output: BaseOutput

    protected fun showError(error: String) {
        viewModelScope.launch {
            _notification.emit(BaseState.Error(error))
        }

    }
}