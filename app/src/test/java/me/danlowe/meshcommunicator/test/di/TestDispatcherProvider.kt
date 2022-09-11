@file:OptIn(ExperimentalCoroutinesApi::class)

package me.danlowe.meshcommunicator.test.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import me.danlowe.meshcommunicator.features.dispatchers.DispatcherProvider

class TestDispatcherProvider(scheduler: TestCoroutineScheduler) : DispatcherProvider {
    private val testDispatcher = StandardTestDispatcher(scheduler)

    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
    override val main: CoroutineDispatcher = testDispatcher
}
