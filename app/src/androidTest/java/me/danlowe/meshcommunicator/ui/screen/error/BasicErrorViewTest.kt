package me.danlowe.meshcommunicator.ui.screen.error

import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.test.base.BaseComposeHiltTest
import me.danlowe.meshcommunicator.test.helper.getStringById
import me.danlowe.meshcommunicator.ui.theme.Dimens
import me.danlowe.meshcommunicator.ui.theme.MeshCommunicatorTheme
import org.junit.Assert.assertEquals
import org.junit.Test

@HiltAndroidTest
class BasicErrorViewTest : BaseComposeHiltTest() {

    private var refreshClicks: Int = 0


    override fun setup() {
        refreshClicks = 0
    }

    @Test
    fun errorMessageNoCallback() = testWithoutCallback {
        composeTestRule
            .onNodeWithTag("errorText")
            .assertTextEquals(getStringById(R.string.error))

        composeTestRule
            .onNodeWithTag("refreshButton")
            .assertDoesNotExist()
    }

    @Test
    fun errorMessageWithCallback() = testWithCallback(::testRefresh) {
        composeTestRule
            .onNodeWithTag("errorText")
            .assertTextEquals(getStringById(R.string.error))

        composeTestRule
            .onNodeWithTag("errorRefreshSpacer")
            .assertHeightIsEqualTo(Dimens.BaseItemSeparation)

        composeTestRule
            .onNodeWithTag("refreshButton")
            .assertIsDisplayed()
            .assertTextEquals(getStringById(R.string.cta_refresh))
            .performClick()

        assertEquals(1, refreshClicks)
    }

    private fun testWithCallback(callback: () -> Unit, testFunction: () -> Unit) {
        composeTestRule.setContent {
            MeshCommunicatorTheme {
                BasicErrorView(callback)
            }
        }
        testFunction()
    }

    private fun testWithoutCallback(testFunction: () -> Unit) {
        composeTestRule.setContent {
            MeshCommunicatorTheme {
                BasicErrorView()
            }
        }
        testFunction()
    }

    private fun testRefresh() {
        refreshClicks++
    }
}