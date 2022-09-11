package me.danlowe.meshcommunicator.ui.button

import androidx.annotation.StringRes
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.danlowe.meshcommunicator.ui.theme.Numbers

@Composable
fun StandardButton(
    modifier: Modifier = Modifier,
    @StringRes
    buttonText: Int,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = CutCornerShape(Numbers.BUTTON_CORNER_CUT_PERCENT),
    ) {
        Text(stringResource(buttonText))
    }
}
