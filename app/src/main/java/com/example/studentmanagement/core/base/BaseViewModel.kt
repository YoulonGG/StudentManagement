package com.example.studentmanagement.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch


/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */

abstract class BaseViewModel<Intent, State> : ViewModel() {

    private val _state = MutableStateFlow<State?>(null)
    val state: StateFlow<State?> = _state.asStateFlow()

    private val intentChannel = Channel<Intent>(Channel.UNLIMITED)
    private var isIntentCollectorStarted = false

    fun sendIntent(intent: Intent) {
        viewModelScope.launch {
            intentChannel.send(intent)
        }
    }

    fun startIntentCollector() {
        if (!isIntentCollectorStarted) {
            isIntentCollectorStarted = true
            viewModelScope.launch {
                intentChannel.consumeAsFlow().collect { intent ->
                    handleIntent(intent)
                }
            }
        }
    }

    protected fun updateState(newState: State) {
        _state.value = newState
    }

    protected abstract suspend fun handleIntent(intent: Intent)
}
