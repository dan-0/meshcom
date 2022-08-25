package me.danlowe.meshcommunicator.features.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Provides [CoroutineDispatcher]s. Allows dispatchers to be easily replaced for testing
 */
interface DispatcherProvider {

    val io: CoroutineDispatcher

    val default: CoroutineDispatcher

    val main: CoroutineDispatcher

}

/**
 * Returns a coroutine context with generic coroutine exception handling
 */
fun DispatcherProvider.buildHandledIoContext(
    doOnError: () -> Unit
): CoroutineContext {
    return io + CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
        doOnError()
    }
}

