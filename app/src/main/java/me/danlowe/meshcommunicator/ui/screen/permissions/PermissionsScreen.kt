@file:OptIn(ExperimentalPermissionsApi::class)

package me.danlowe.meshcommunicator.ui.screen.permissions

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.ui.theme.Dimens

@Composable
fun PermissionsScreen(
    navHandler: (PermissionsNavEvent) -> Unit
) {

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
        LaunchedEffect(permissions.allPermissionsGranted) {
            navHandler(PermissionsNavEvent.PermissionsGranted)
        }
    } else {
        PermissionScreenContent(permissions)
    }
}

@Composable
private fun PermissionScreenContent(permissions: MultiplePermissionsState) {
    // TODO: Missing shouldShowRationale check, see issue #11
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.BaseItemSeparation)
    ) {
        Text(
            text = stringResource(R.string.prompt_permissions),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(stringResource(R.string.permission_text_location))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Text(stringResource(R.string.permission_text_bt_advertise))
            Text(stringResource(R.string.permission_text_bt_connect))
            Text(stringResource(R.string.permission_text_bt_scan))
        }
        // TODO list revoked permissions that need manual remediation
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = permissions::launchMultiplePermissionRequest
        ) {
            Text(stringResource(R.string.btn_text_request_permissions))
        }

    }
}