package me.danlowe.meshcommunicator.ui.screen.conversation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.ui.button.StandardButton
import me.danlowe.meshcommunicator.ui.screen.loading.FullLoadingScreen
import me.danlowe.meshcommunicator.ui.theme.Dimens

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel = hiltViewModel(),
    externalUserId: ExternalUserId
) {

    when (val state = viewModel.state.collectAsState(initial = ConversationState.Loading).value) {
        is ConversationState.Content -> ConversationContent(
            viewModel,
            state
        )
        ConversationState.Error -> {
            // TODO make a real error view
            Text("Error")
        }
        ConversationState.Loading -> FullLoadingScreen()
    }


}

@Composable
private fun ConversationContent(
    viewModel: ConversationViewModel,
    content: ConversationState.Content
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = Dimens.BaseHorizontalSpace)
    ) {

        LazyColumn(
        ) {
            items(content.messages) { messageData ->
                Message(messageData)
            }
        }

        Spacer(
            modifier = Modifier.weight(.7f)
        )

        SendMessageBox(viewModel)

    }
}

@Composable
private fun Message(
    messageData: MessageData
) {

    Column {
        // TODO add more data
        Text(text = messageData.message)
    }

}

@Composable
private fun SendMessageBox(
    viewModel: ConversationViewModel
) {

    val sendMessageText = rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    Row(
        modifier = Modifier
            .padding(horizontal = Dimens.BaseHorizontalSpace)
    ) {

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(.8f),
            value = sendMessageText.value,
            onValueChange = { newValue: TextFieldValue ->
                sendMessageText.value = newValue
            }
        )

        StandardButton(
            onClick = {
                viewModel.sendMessage(sendMessageText.value.text)
                sendMessageText.value = TextFieldValue("")
            },
            buttonText = R.string.btn_text_send
        )

    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConversationScreen() {
    ConversationContent(
        viewModel = hiltViewModel(),
        content = ConversationState.Content(listOf())
    )
}