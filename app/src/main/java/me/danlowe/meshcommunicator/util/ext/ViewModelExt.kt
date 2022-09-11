package me.danlowe.meshcommunicator.util.ext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Wrapper around [viewModelScope] to simplify launching coroutines
 */
fun ViewModel.launchInContext(
    coroutineContext: CoroutineContext,
    action: suspend () -> Unit
): Job {
    return viewModelScope.launch(coroutineContext) {
        action()
    }
}
