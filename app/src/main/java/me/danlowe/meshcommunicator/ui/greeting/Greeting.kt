package me.danlowe.meshcommunicator.ui.greeting

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.danlowe.meshcommunicator.ui.theme.MeshCommunicatorTheme

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MeshCommunicatorTheme {
        Greeting("Android")
    }
}