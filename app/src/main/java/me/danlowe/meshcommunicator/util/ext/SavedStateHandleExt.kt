package me.danlowe.meshcommunicator.util.ext

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A [MutableStateFlow] that saves each value change to a [savedStateHandle] with the given [key]
 */
private class SavedMutableStateFlow<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    initialValue: T
) : MutableStateFlow<T> {

    private val delegate = MutableStateFlow<T>(initialValue)

    override var value: T
        get() = delegate.value
        set(value) {
            savedStateHandle[key] = value
            delegate.value = value
        }
    override val subscriptionCount: StateFlow<Int> = delegate.subscriptionCount

    override suspend fun emit(value: T) = delegate.emit(value)

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        delegate.resetReplayCache()
    }

    override fun tryEmit(value: T): Boolean {
        return delegate.tryEmit(value)
    }

    override fun compareAndSet(expect: T, update: T): Boolean {
        return delegate.compareAndSet(expect, update)
    }

    override val replayCache: List<T> = delegate.replayCache

    override suspend fun collect(collector: FlowCollector<T>) = delegate.collect(collector)
}

/**
 * Returns a mutable stateflow backed by a [SavedStateHandle]
 */
fun <T : Parcelable> SavedStateHandle.getMutableStateFlow(key: String, defaultValue: T): MutableStateFlow<T> {

    val initialValue: T = get(key) ?: defaultValue

    return SavedMutableStateFlow(
        this,
        key,
        initialValue
    )

}
