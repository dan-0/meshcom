package me.danlowe.meshcommunicator.ui.screen.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.ui.button.StandardButton
import me.danlowe.meshcommunicator.ui.screen.loading.FullLoadingScreen
import me.danlowe.meshcommunicator.ui.theme.Dimens

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel = hiltViewModel(),
) {

    val listState = rememberLazyListState()

    when (val viewState = viewModel.state.collectAsState(initial = ConversationState.Loading).value) {
        is ConversationState.Content -> {
            ConversationContent(
                viewModel::sendMessage,
                viewState,
                listState
            )
        }
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
    content: ConversationState.Content,
    listState: LazyListState
) {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {

        val (messagesView, divider, sendBox) = createRefs()

        LazyColumn(
            modifier = Modifier
                .constrainAs(messagesView) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(sendBox.top)
                    height = Dimension.fillToConstraints
                    width = Dimension.matchParent
                }
                .padding(Dimens.BasePadding),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(Dimens.BaseItemSeparation),
        ) {
            items(content.messages) { messageData ->
                if (messageData.isFromLocalUser) {
                    MessageFromUser(messageData = messageData)
                } else {
                    MessageToUser(messageData = messageData)
                }
            }
        }

        Divider(
            modifier = Modifier
                .constrainAs(divider) {
                    top.linkTo(messagesView.bottom)
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

    LaunchedEffect(content.messages.size) {
        listState.scrollToItem(content.messages.lastIndex)
    }
}

@Composable
private fun MessageToUser(messageData: MessageData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(Dimens.MessageCornerRounding)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colors.secondary)
                    .padding(Dimens.MessagePadding),
            ) {
                Text(
                    text = messageData.message,
                    color = MaterialTheme.colors.onSecondary
                )
            }
        }
    }
}

@Composable
private fun MessageFromUser(messageData: MessageData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            shape = RoundedCornerShape(Dimens.MessageCornerRounding)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colors.primary)
                    .padding(Dimens.MessagePadding)
            ) {
                Text(
                    text = messageData.message,
                    color = MaterialTheme.colors.onPrimary
                )
            }
        }
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

    Row(
        modifier = modifier
            .padding(Dimens.BasePadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.BaseItemSeparation),
        verticalAlignment = Alignment.Bottom
    ) {

        OutlinedTextField(
            modifier = Modifier.weight(1f),
            maxLines = 3,
            value = sendMessageText.value,
            onValueChange = { newValue: TextFieldValue ->
                sendMessageText.value = newValue
            }
        )

        StandardButton(
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
        content = ConversationState.Content(
            listOf(
                MessageData(
                    originUserId = ExternalUserId(id = "123"),
                    message = "Hello to user",
                    timeSent = "today",
                    timeReceived = "yesterday",
                    isFromLocalUser = false
                ),
                MessageData(
                    originUserId = ExternalUserId(id = "1234"),
                    message = "Hello from user",
                    timeSent = "today",
                    timeReceived = "yesterday",
                    isFromLocalUser = true
                )
            )
        ),
        rememberLazyListState()
    )
}