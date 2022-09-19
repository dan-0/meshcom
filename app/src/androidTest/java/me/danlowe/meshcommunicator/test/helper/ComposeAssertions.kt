package me.danlowe.meshcommunicator.test.helper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import org.junit.Assert

fun SemanticsNodeInteraction.assertBackgroundColor(expectedBackground: Color) {
    val capturedName = captureToImage().colorSpace.name
    Assert.assertEquals(expectedBackground.colorSpace.name, capturedName)
}