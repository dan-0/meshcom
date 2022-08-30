@file:OptIn(ExperimentalPermissionsApi::class)

package me.danlowe.meshcommunicator.ui.screen.conversations

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
fun ConversationsScreen() {

    val dangerousPermissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION).let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            it + listOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
            )
        } else {
            it
        }
    }

    val permissions = rememberMultiplePermissionsState(
        permissions = dangerousPermissions
    )

    if (permissions.allPermissionsGranted) {
        ConversationScreenView()
    } else {
        PermissionScreen(permissions)
    }
}

@Composable
private fun PermissionScreen(permissions: MultiplePermissionsState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Before using MeshCommunicator we require the following permissions in order to communicate with other devices:",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text("Fine Location")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Text("Bluetooth Advertise")
            Text("Bluetooth Connect")
            Text("Bluetooth Scan")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                permissions.launchMultiplePermissionRequest()
            }
        ) {
            Text("Request permissions")
        }

    }
}

@Composable
private fun ConversationScreenView(
    viewModel: ConversationsViewModel = hiltViewModel()
) {
    when (val state = viewModel.state.collectAsState(initial = ConversationsState.Loading).value) {
        is ConversationsState.Content -> ConversationsList(state.conversations)
        ConversationsState.Loading -> ConversationsLoading()
    }
}

@Composable
private fun ConversationsList(conversations: List<ConversationInfo>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(conversations) { conversation ->
            Column {
                Text(conversation.userName)
            }
        }
    }
}

@Composable
private fun ConversationsLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}