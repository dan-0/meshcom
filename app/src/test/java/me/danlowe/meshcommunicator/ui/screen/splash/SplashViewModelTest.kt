package me.danlowe.meshcommunicator.ui.screen.splash

import androidx.datastore.core.DataStore
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.test.base.BaseUnitTest
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashEvent
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest : BaseUnitTest() {

    private lateinit var ut: SplashViewModel

    @RelaxedMockK
    lateinit var dataStore: DataStore<AppSettings>

    private lateinit var states: List<SplashState>

    private lateinit var events: List<SplashEvent>

    override fun setup() {
        super.setup()

        ut = SplashViewModel(
            dataStore,
            testDispatcher
        )

        states = ut.state.toLiveList()
        events = ut.event.toLiveList()
    }

    @Test
    fun `initial startup behavior, with user`() = coRunTest {
        val expectedState = listOf(SplashState.Loading)
        val expectedEvents = listOf(SplashEvent.HasCredentials)

        // given
        every { dataStore.data } returns flowOf(VALID_APP_SETTINGS)

        // when
        advanceUntilIdle()

        // then
        assertEquals(expectedState, states)
        assertEquals(expectedEvents, events)

    }

    @Test
    fun `initial startup behavior, no user`() = coRunTest {
        val expectedState = listOf(SplashState.Loading)
        val expectedEvents = listOf(SplashEvent.NoCredentials)

        // given
        every { dataStore.data } returns flowOf()

        // when
        advanceUntilIdle()

        // then
        assertEquals(expectedState, states)
        assertEquals(expectedEvents, events)

    }

    @Test
    fun `initial startup behavior, has error`() = coRunTest {
        val expectedState = listOf(SplashState.Loading, SplashState.Error)
        val expectedEvents = listOf<SplashEvent>()

        // given
        every { dataStore.data } throws IOException("Test")

        // when
        advanceUntilIdle()

        // then
        assertEquals(expectedState, states)
        assertEquals(expectedEvents, events)
    }

    @Test
    fun `initial startup behavior, error state retry`() = coRunTest {
        val expectedState = listOf(SplashState.Loading, SplashState.Error, SplashState.Loading)
        val expectedEvents = listOf<SplashEvent>(SplashEvent.HasCredentials)

        // given
        every { dataStore.data } throws IOException("Test") andThen flowOf(VALID_APP_SETTINGS)

        // when
        advanceUntilIdle()
        ut.loadCredentials()
        advanceUntilIdle()

        // then
        assertEquals(expectedState, states)
        assertEquals(expectedEvents, events)
    }

    companion object {

        private val VALID_APP_SETTINGS = AppSettings.newBuilder()
            .setUserId("12345-32134-asdfasd-3234")
            .setUserName("Bob")
            .build()

    }

}

