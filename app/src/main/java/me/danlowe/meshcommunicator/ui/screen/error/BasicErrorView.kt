package me.danlowe.meshcommunicator.ui.screen.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.ui.button.StandardButton
import me.danlowe.meshcommunicator.ui.theme.Dimens

@Composable
fun BasicErrorView(
    onRefresh: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("basicErrorView"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.error),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("errorText"),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5
        )
        onRefresh?.let { refresh ->
            Spacer(modifier = Modifier
                .height(Dimens.BaseItemSeparation)
                .testTag("errorRefreshSpacer")
            )
            StandardButton(
                buttonText = R.string.cta_refresh,
                modifier = Modifier.testTag("refreshButton")
            ) {
                refresh()
            }
        }
    }
}

@Preview
@Composable
private fun BasicErrorPreview() {
    BasicErrorView {}
}
