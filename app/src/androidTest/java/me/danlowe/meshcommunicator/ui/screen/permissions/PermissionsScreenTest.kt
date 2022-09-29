package me.danlowe.meshcommunicator.ui.screen.permissions

import android.Manifest
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.test.base.BaseComposeHiltTest
import me.danlowe.meshcommunicator.test.helper.getStringById
import me.danlowe.meshcommunicator.ui.theme.MeshCommunicatorTheme
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalPermissionsApi::class)
@HiltAndroidTest
class PermissionsScreenTest : BaseComposeHiltTest() {

    @Test
    fun allPermissionsGranted() {
        val expected = true

        // given
        val permissions: MultiplePermissionsState = mockk(relaxed = true) {
            every { allPermissionsGranted } returns true
        }

        var permissionGranted = false
        val navHandler = { event: PermissionsNavEvent ->
            when (event) {
                PermissionsNavEvent.PermissionsGranted -> permissionGranted = true
            }
        }

        // when
        composeTestRule.setContent {
            MeshCommunicatorTheme {
                PermissionsScreen(permissions, navHandler)
            }
        }

        // then
        composeTestRule.runOnIdle {
            assertEquals(expected, permissionGranted)
        }
    }

    @Test
    fun contentStateNoRationale() {

        // given
        val mockPermissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )

        val permissions: MultiplePermissionsState = mockk(relaxed = true) {
            every { allPermissionsGranted } returns false
            every { shouldShowRationale } returns false
            every { permissions } returns mockPermissions.map {
                getMockPermissionState(it)
            }
        }
        val navHandler = { event: PermissionsNavEvent ->
        }
        // when
        launchScreenRequiringPermissions(permissions, navHandler)

        // then
        composeTestRule
            .onNodeWithTag("permissionScreenContent")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("permissionPrompt")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("permissionText${R.string.permission_text_location}")
            .assertTextEquals(getStringById(R.string.permission_text_location))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("permissionRationale${R.string.permission_rationale_location}")
            .assertDoesNotExist()

    }

    @Test
    fun contentStateWithRationale() {

        // given
        val permissionStrings = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )

        val permissions: MultiplePermissionsState = mockk(relaxed = true) {
            every { allPermissionsGranted } returns false
            every { shouldShowRationale } returns true
            every { permissions } returns permissionStrings.map {
                getMockPermissionState(it, PermissionStatus.Denied(true))
            }
        }
        val navHandler = { event: PermissionsNavEvent ->
        }
        // when
        launchScreenRequiringPermissions(permissions, navHandler)

        // then
        composeTestRule
            .onNodeWithTag("permissionScreenContent")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("permissionPrompt")
            .assertIsDisplayed()

        assertPermissionWithRationale(
            textId = R.string.permission_text_location,
            rationaleId = R.string.permission_rationale_location
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            assertPermissionWithRationale(
                textId = R.string.permission_text_bt_advertise,
                rationaleId = R.string.permission_rationale_bt_advertise
            )

            assertPermissionWithRationale(
                textId = R.string.permission_text_bt_connect,
                rationaleId = R.string.permission_rationale_bt_connect
            )

            assertPermissionWithRationale(
                textId = R.string.permission_text_bt_scan,
                rationaleId = R.string.permission_rationale_bt_scan
            )
        } else {
            composeTestRule
                .onNodeWithTag("permissionTextColumn${R.string.permission_text_bt_advertise}")
                .assertDoesNotExist()

            composeTestRule
                .onNodeWithTag("permissionTextColumn${R.string.permission_text_bt_connect}")
                .assertDoesNotExist()

            composeTestRule
                .onNodeWithTag("permissionTextColumn${R.string.permission_text_bt_scan}")
                .assertDoesNotExist()
        }

    }

    private fun getMockPermissionState(
        permissionString: String,
        permissionStatus: PermissionStatus = PermissionStatus.Denied(false)
    ): PermissionState {
        return mockk(relaxed = true) {
            every {
                permission
            } returns permissionString
            every {
                status
            } returns permissionStatus
        }
    }

    private fun assertPermissionWithRationale(@StringRes textId: Int, @StringRes rationaleId: Int) {

        composeTestRule
            .onNodeWithTag("permissionText$textId")
            .performScrollTo()
            .assertTextEquals(getStringById(textId))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("permissionRationale$rationaleId")
            .performScrollTo()
            .assertTextEquals(getStringById(rationaleId))
            .assertIsDisplayed()
    }

    private fun launchScreenRequiringPermissions(
        permissions: MultiplePermissionsState,
        navHandler: (PermissionsNavEvent) -> Unit
    ) {
        composeTestRule.setContent {
            MeshCommunicatorTheme {
                PermissionsScreen(permissions, navHandler)
            }
        }
    }
}