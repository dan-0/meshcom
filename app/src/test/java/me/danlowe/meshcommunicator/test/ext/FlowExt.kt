package me.danlowe.meshcommunicator.test.ext

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

suspend fun <T> Flow<T>.toLiveList(): List<T> {
    val mutableList = mutableListOf<T>()
    coroutineScope {
        launch {
            collect { newItem ->
                mutableList.add(newItem)
            }
        }
    }
    return mutableList
}