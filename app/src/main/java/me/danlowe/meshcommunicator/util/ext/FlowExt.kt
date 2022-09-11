package me.danlowe.meshcommunicator.util.ext

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

@Suppress("FunctionName")
fun <T> EventFlow() = MutableSharedFlow<T>(
    0,
    1,
    BufferOverflow.DROP_OLDEST
)
