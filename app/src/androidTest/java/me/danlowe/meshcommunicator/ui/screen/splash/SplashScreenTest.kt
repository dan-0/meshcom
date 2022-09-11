package me.danlowe.meshcommunicator.ui.screen.splash

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso.onIdle
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import me.danlowe.meshcommunicator.test.base.BaseComposeHiltTest
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashEvent
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashNavEvent
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashState
import me.danlowe.meshcommunicator.ui.theme.MeshCommunicatorTheme
import org.junit.Assert.assertEquals
import org.junit.Test

@HiltAndroidTest
class SplashScreenTest : BaseComposeHiltTest() {

    @BindValue
    val viewModel: SplashViewModel = mockk(relaxed = true)

    private lateinit var eventFlow: MutableSharedFlow<SplashEvent>

    private lateinit var stateFlow: MutableStateFlow<SplashState>

    private lateinit var receivedNavEvents: List<SplashNavEvent>

    override fun setup() {

        eventFlow = MutableSharedFlow(1, 0)
        stateFlow = MutableStateFlow(SplashState.Loading)
        val eventList = mutableListOf<SplashNavEvent>()

        every { viewModel.state } returns stateFlow

        every { viewModel.event } returns eventFlow

        receivedNavEvents = eventList

        composeTestRule.setContent {
            MeshCommunicatorTheme {
                SplashScreen(viewModel) {
                    eventList.add(it)
                }
            }
        }
    }

    @Test
    fun initialState() {
        composeTestRule
            .onNodeWithTag("fullLoadingScreen")
            .assertIsDisplayed()
    }

    @Test
    fun errorState() {
        stateFlow.tryEmit(SplashState.Error)

        composeTestRule
            .onNodeWithTag("basicErrorView")
            .assertIsDisplayed()
    }

    @Test
    fun noCredentialsNavigation() {
        val expected = listOf(SplashNavEvent.NoCredentials)

        eventFlow.tryEmit(SplashEvent.NoCredentials)

        onIdle()

        assertEquals(expected, receivedNavEvents)
    }

    @Test
    fun hasCredentialsNavigation() {
        val expected = listOf(SplashNavEvent.HasCredentials)

        eventFlow.tryEmit(SplashEvent.HasCredentials)

        onIdle()

        assertEquals(expected, receivedNavEvents)
    }

}