package me.danlowe.meshcommunicator.util.ext

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A [MutableStateFlow] that saves each value change to a [savedStateHandle] with the given [key]
 */
private class SavedMutableStateFlow<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    initialValue: T
) : MutableStateFlow<T> by MutableStateFlow(initialValue){

    override var value: T = initialValue
        set(value) {
            savedStateHandle[key] = value
            field = value
        }

}

/**
 * Returns a mutable stateflow backed by a [SavedStateHandle]
 */
fun <T> SavedStateHandle.getMutableStateFlow(key: String, defaultValue: T): MutableStateFlow<T> {

    val initialValue: T = get(key) ?: defaultValue

    return SavedMutableStateFlow(
        this,
        key,
        initialValue
    )

}