@file:OptIn(ExperimentalPermissionsApi::class)

package me.danlowe.meshcommunicator.ui.screen.permissions

import android.Manifest
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.ui.theme.Dimens

@Composable
fun PermissionsScreen(
    permissions: MultiplePermissionsState = getMultiplePermissionState(),
    navHandler: (PermissionsNavEvent) -> Unit
) {
    if (permissions.allPermissionsGranted) {
        LaunchedEffect(permissions.allPermissionsGranted) {
            navHandler(PermissionsNavEvent.PermissionsGranted)
        }
    } else {
        PermissionScreenContent(permissions)
    }
}

@Composable
private fun getMultiplePermissionState(): MultiplePermissionsState {
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

    return rememberMultiplePermissionsState(
        permissions = dangerousPermissions
    )
}

@Composable
private fun PermissionScreenContent(permissions: MultiplePermissionsState) {
    // TODO: Missing shouldShowRationale check, see issue #11
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.BasePadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.BaseItemSeparation)
    ) {
        Text(
            text = stringResource(R.string.prompt_permissions),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (permissions.containsNonGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            PermissionText(
                permissionText = R.string.permission_text_location,
                permissionRational = R.string.permission_rationale_location,
                shouldShowRational = permissions.shouldShowRationale
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (permissions.containsNonGrantedPermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
                PermissionText(
                    permissionText = R.string.permission_text_bt_advertise,
                    permissionRational = R.string.permission_rationale_bt_advertise,
                    shouldShowRational = permissions.shouldShowRationale
                )
            }

            if (permissions.containsNonGrantedPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                PermissionText(
                    permissionText = R.string.permission_text_bt_connect,
                    permissionRational = R.string.permission_rationale_bt_connect,
                    shouldShowRational = permissions.shouldShowRationale
                )
            }

            if (permissions.containsNonGrantedPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                PermissionText(
                    permissionText = R.string.permission_text_bt_scan,
                    permissionRational = R.string.permission_rationale_bt_scan,
                    shouldShowRational = permissions.shouldShowRationale
                )
            }
        }
        permissions.permissions.forEach {
            it.status
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = permissions::launchMultiplePermissionRequest
        ) {
            Text(stringResource(R.string.btn_text_request_permissions))
        }

    }
}

private fun MultiplePermissionsState.containsNonGrantedPermission(permission: String): Boolean {
    return permissions.firstOrNull {
        it.permission == permission
    } != null
}


@Composable
private fun PermissionText(
    @StringRes permissionText: Int,
    @StringRes permissionRational: Int,
    shouldShowRational: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("permissionTextColumn$permissionText")
    ) {
        Text(
            text = stringResource(permissionText),
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("permissionText$permissionText")
        )
        if (shouldShowRational) {
            Text(
                text = stringResource(permissionRational),
                modifier = Modifier
                    .padding(start = Dimens.BasePadding)
                    .testTag("permissionRationale$permissionRational"),
                style = MaterialTheme.typography.subtitle1,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PermissionPreview() {
    val dangerousPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    val permissions = rememberMultiplePermissionsState(
        permissions = dangerousPermissions
    )

    PermissionScreenContent(permissions = permissions)
}
