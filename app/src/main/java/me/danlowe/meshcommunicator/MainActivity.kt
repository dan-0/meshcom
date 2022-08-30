package me.danlowe.meshcommunicator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.danlowe.meshcommunicator.nav.AppDestinations
import me.danlowe.meshcommunicator.ui.screen.conversations.ConversationsScreen
import me.danlowe.meshcommunicator.ui.screen.signin.SignInScreen
import me.danlowe.meshcommunicator.ui.screen.signin.data.SignInNavEvent
import me.danlowe.meshcommunicator.ui.screen.splash.SplashScreen
import me.danlowe.meshcommunicator.ui.screen.splash.data.SplashNavEvent
import me.danlowe.meshcommunicator.ui.theme.MeshCommunicatorTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    @Composable
    private fun App() {

        val navController = rememberNavController()

        val title = rememberSaveable {
            mutableStateOf("")
        }

        val updateTitle = { newTitle: String ->
            title.value = newTitle
        }

        MeshCommunicatorTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(title.value) },
                        elevation = 20.dp
                    )
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.Splash.routeTemplate,
                    modifier = Modifier.padding(innerPadding)
                ) {

                    composableDestination(AppDestinations.Splash) { _, destination ->

                        updateTitle(stringResource(destination.title))

                        SplashScreen { navEvent ->
                            val newDestination = when (navEvent) {
                                SplashNavEvent.HasCredentials -> {
                                    AppDestinations.Conversations.routeTemplate
                                }
                                SplashNavEvent.NoCredentials -> {
                                    AppDestinations.SignIn.routeTemplate
                                }
                            }

                            navController.navigate(newDestination) {
                                popUpTo(0)
                            }
                        }
                    }

                    composableDestination(AppDestinations.SignIn) { _, destination ->

                        updateTitle(stringResource(destination.title))

                        SignInScreen { navEvent ->
                            when (navEvent) {
                                SignInNavEvent.Complete -> {
                                    navController.navigate(
                                        AppDestinations.Conversations.routeTemplate,
                                    ) {
                                        popUpTo(0)
                                    }
                                }
                            }
                        }
                    }

                    composableDestination(AppDestinations.Conversations) { _, destination ->

                        updateTitle(stringResource(destination.title))

                        ConversationsScreen()

                    }

                }
            }
        }

    }

    private fun <T : AppDestinations> NavGraphBuilder.composableDestination(
        destination: T,
        content: @Composable (backStackEntry: NavBackStackEntry, destination: T) -> Unit
    ) {
        composable(destination.routeTemplate) {
            content(it, destination)
        }
    }
}



