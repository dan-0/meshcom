package me.danlowe.meshcommunicator.ui.screen.conversation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.ui.button.StandardButton
import me.danlowe.meshcommunicator.ui.screen.loading.FullLoadingScreen
import me.danlowe.meshcommunicator.ui.theme.Dimens

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel = hiltViewModel(),
) {

    when (val state = viewModel.state.collectAsState(initial = ConversationState.Loading).value) {
        is ConversationState.Content -> ConversationContent(
            viewModel::sendMessage,
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
    sendMessage: (String) -> Unit,
    content: ConversationState.Content
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {

        val (messages, divider, sendBox) = createRefs()

        LazyColumn(
            modifier = Modifier
                .constrainAs(messages) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(sendBox.top)
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                }
        ) {
            items(content.messages) { messageData ->
                Message(messageData)
            }
        }

        Divider(
            modifier = Modifier
                .constrainAs(divider) {
                    top.linkTo(messages.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(sendBox.top)
                }
        )

        SendMessageBox(
            sendMessage = sendMessage,
            modifier = Modifier
                .constrainAs(sendBox) {
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                    top.linkTo(divider.bottom)
                }
        )

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
    sendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val sendMessageText = rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    ConstraintLayout(
        modifier = modifier
            .padding(Dimens.BasePadding)
            .height(IntrinsicSize.Min)
    ) {

        val (messageField, sendButton) = createRefs()

        OutlinedTextField(
            modifier = Modifier.constrainAs(messageField) {
                top.linkTo(parent.top)
                end.linkTo(sendButton.start)
                start.linkTo(parent.start)
                bottom.linkTo(parent.bottom)
            },
            maxLines = 3,
            value = sendMessageText.value,
            onValueChange = { newValue: TextFieldValue ->
                sendMessageText.value = newValue
            }
        )

        StandardButton(
            modifier = Modifier
                .constrainAs(sendButton) {
                    end.linkTo(parent.end)
                    start.linkTo(messageField.end, margin = Dimens.BaseItemSeparation)
                    bottom.linkTo(parent.bottom)
                },
            buttonText = R.string.btn_text_send
        ) {
            sendMessage(sendMessageText.value.text)
            sendMessageText.value = TextFieldValue("")
        }

    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConversationScreenPreview() {
    ConversationContent(
        sendMessage = {},
        content = ConversationState.Content(listOf())
    )
}