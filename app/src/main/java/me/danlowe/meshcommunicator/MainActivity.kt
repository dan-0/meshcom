package me.danlowe.meshcommunicator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.danlowe.meshcommunicator.nav.AppDestinations
import me.danlowe.meshcommunicator.ui.greeting.Greeting
import me.danlowe.meshcommunicator.ui.screen.signin.SignInScreen
import me.danlowe.meshcommunicator.ui.screen.signin.SignInViewModel
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
                    startDestination = AppDestinations.SignIn.routeTemplate,
                    modifier = Modifier.padding(innerPadding)
                ) {

                    composableDestination(AppDestinations.SignIn) { _, destination ->

                        updateTitle(stringResource(destination.title))

                        val viewModel: SignInViewModel = hiltViewModel()
                        SignInScreen(viewModel)

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



