package me.danlowe.meshcommunicator.ui.screen.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import me.danlowe.meshcommunicator.ui.screen.loading.FullLoadingScreen
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashEvent
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashNavEvent
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashState

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    navHandler: (SplashNavEvent) -> Unit
) {
    LaunchedEffect(key1 = Unit) {

        viewModel.event.collect { event ->

            when (event) {
                SplashEvent.NoCredentials -> navHandler(SplashNavEvent.NoCredentials)
                SplashEvent.HasCredentials -> navHandler(SplashNavEvent.HasCredentials)
            }

        }

    }

    when (viewModel.state.collectAsState(initial = SplashState.Loading).value) {
        SplashState.Loading -> FullLoadingScreen()
        // TODO replace with error refresh screen when made
        SplashState.Error -> FullLoadingScreen()
    }
}