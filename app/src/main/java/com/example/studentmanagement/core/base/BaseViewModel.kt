package com.example.studentmanagement.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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

abstract class BaseViewModel<UiAction, UiState>(
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {


    abstract fun setInitialState(): UiState

    abstract fun onAction(event: UiAction)

    private val initialState: UiState by lazy { setInitialState() }

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    protected fun setState(reducer: UiState.() -> UiState) {
        viewModelScope.launch(mainDispatcher) {
            val newState = uiState.value.reducer()
            _uiState.value = newState
        }
    }

//    private val _state = MutableStateFlow<State?>(null)
//    val state: StateFlow<State?> = _state.asStateFlow()
//
//    private val intentChannel = Channel<Intent>(Channel.UNLIMITED)
//    private var isIntentCollectorStarted = false
//
//    fun sendIntent(intent: Intent) {
//        viewModelScope.launch {
//            intentChannel.send(intent)
//        }
//    }
//
//    fun startIntentCollector() {
//        if (!isIntentCollectorStarted) {
//            isIntentCollectorStarted = true
//            viewModelScope.launch {
//                intentChannel.consumeAsFlow().collect { intent ->
//                    handleIntent(intent)
//                }
//            }
//        }
//    }
//
//    protected fun updateState(newState: State) {
//        _state.value = newState
//    }
//
//    protected abstract suspend fun handleIntent(intent: Intent)
}
