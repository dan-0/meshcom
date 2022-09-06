@file:OptIn(ExperimentalPermissionsApi::class)

package me.danlowe.meshcommunicator.ui.screen.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import me.danlowe.meshcommunicator.R
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationConnectionState
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationInfo
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationsNavEvent
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationsState
import me.danlowe.meshcommunicator.ui.screen.loading.FullLoadingScreen
import me.danlowe.meshcommunicator.ui.theme.Dimens

@Composable
fun ConversationsScreen(
    viewModel: ConversationsViewModel = hiltViewModel(),
    navHandler: (ConversationsNavEvent) -> Unit
) {
    when (val state = viewModel.state.collectAsState(initial = ConversationsState.Loading).value) {
        is ConversationsState.Content -> ConversationsList(state.conversations, navHandler)
        ConversationsState.Loading -> FullLoadingScreen()
    }
}

@Composable
private fun ConversationsList(
    conversations: List<ConversationInfo>,
    navHandler: (ConversationsNavEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = Dimens.BaseHorizontalSpace,
                vertical = Dimens.BaseTopBottomPadding
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.BaseItemSeparation)
    ) {
        items(conversations) { conversation ->
            ConversationItem(navHandler, conversation)
        }
    }
}

@Composable
private fun ConversationItem(
    navHandler: (ConversationsNavEvent) -> Unit,
    conversation: ConversationInfo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navHandler(
                    ConversationsNavEvent.OpenConversation(
                        userName = conversation.userName,
                        externalUserId = ExternalUserId(conversation.userId),
                    )
                )
            },
        elevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.BasePadding)
                .height(IntrinsicSize.Max),
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(conversation.userName)
                Text(stringResource(R.string.label_prefix_last_seen) + conversation.lastSeen)
            }

            val backgroundColor = when (conversation.connectionState) {
                ConversationConnectionState.Connected -> Color.Green
                ConversationConnectionState.NotConnected -> Color.LightGray
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .border(1.dp, MaterialTheme.colors.primaryVariant, CircleShape),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ConversationItemPreview() {

    ConversationItem(
        {},
        ConversationInfo(
            userName = "Bob",
            userId = "sa4 aserAuserId",
            lastSeen = "now",
            connectionState = ConversationConnectionState.Connected,
            lastMessage = "Hello"
        )
    )

}

