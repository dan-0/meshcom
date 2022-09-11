@file:OptIn(ExperimentalCoroutinesApi::class)

package me.danlowe.meshcommunicator.test.base

import io.mockk.MockKAnnotations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.danlowe.meshcommunicator.test.di.TestDispatcherProvider
import me.danlowe.meshcommunicator.test.rule.MainDispatcherRule
import org.junit.Before
import org.junit.Rule

open class BaseUnitTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var testScope: TestScope

    lateinit var testDispatcher: TestDispatcherProvider
        private set

    @Before
    open fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        testScope = TestScope()
        testDispatcher = TestDispatcherProvider(testScope.testScheduler)
    }

    fun runScopedTest(testBody: suspend TestScope.() -> Unit): TestResult {
        return testScope.runTest { testBody() }
    }

}