@file:OptIn(ExperimentalCoroutinesApi::class)

package me.danlowe.meshcommunicator.test.base

import io.mockk.MockKAnnotations
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.danlowe.meshcommunicator.test.di.TestDispatcherProvider
import me.danlowe.meshcommunicator.test.rule.MainDispatcherRule
import org.junit.After
import org.junit.Before
import org.junit.Rule

open class BaseUnitTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var testScope: TestScope

    lateinit var testDispatcher: TestDispatcherProvider
        private set

    private lateinit var _internalBaseScope: CoroutineScope

    @Before
    open fun setup() {
        _internalBaseScope = CoroutineScope(Dispatchers.Unconfined)
        MockKAnnotations.init(this, relaxUnitFun = true)
        testScope = TestScope()
        testDispatcher = TestDispatcherProvider(testScope.testScheduler)
    }

    /**
     * Run a coroutine test with the base test dispatcher
     */
    fun coRunTest(testBody: suspend TestScope.() -> Unit): TestResult {
        return testScope.runTest { testBody() }
    }

    @After
    open fun destroy() {
        _internalBaseScope.cancel()
    }

    /**
     * Converts a [Flow] to a [List] that is updated as the target [Flow] emits data. Helpful for
     * more easily verifying a series of state events as the occur.
     */
    fun <T> Flow<T>.toLiveList(): List<T> {
        val mutableList = mutableListOf<T>()
        _internalBaseScope.launch {
            collect { newItem ->
                mutableList.add(newItem)
            }
        }
        return mutableList
    }

}