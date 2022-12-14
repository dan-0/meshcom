package me.danlowe.meshcommunicator.ui.screen.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationConnectionState
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationInfo
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationsNavEvent
import me.danlowe.meshcommunicator.ui.theme.Dimens
import me.danlowe.meshcommunicator.ui.theme.OkGreen
import me.danlowe.meshcommunicator.ui.theme.UnknownGrey

@Composable
fun ConversationItem(
    navHandler: (ConversationsNavEvent) -> Unit,
    conversation: ConversationInfo
) {
    ConstraintLayout {
        val (conversationCard, connectionIndicator) = createRefs()

        Card(
            modifier = Modifier
                .constrainAs(conversationCard) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                .clickable {
                    navHandler(
                        ConversationsNavEvent.OpenConversation(
                            userName = conversation.userName,
                            externalUserId = ExternalUserId(conversation.userId),
                        )
                    )
                }
                .testTag("conversationCard"),
            elevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(Dimens.BasePadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.BaseItemSeparation)
            ) {
                ConversationDataRow(conversation)
                ConversationMessageRow(conversation)
            }


        }

        val backgroundColor = when (conversation.connectionState) {
            ConversationConnectionState.Connected -> OkGreen
            ConversationConnectionState.NotConnected -> UnknownGrey
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(1.dp, MaterialTheme.colors.primaryVariant, CircleShape)
                .testTag("conversationStateIndicator")
                .constrainAs(connectionIndicator) {
                    top.linkTo(conversationCard.top)
                    bottom.linkTo(conversationCard.top)
                    end.linkTo(
                        conversationCard.end,
                        margin = Dimens.BaseVerticalSpace / 2
                    )
                },
        )

    }
}

@Composable
private fun ConversationMessageRow(conversation: ConversationInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        conversation.lastMessage?.let { lastMessage ->
            Text(
                text = "\"$lastMessage\"",
                style = MaterialTheme.typography.body2,
                modifier = Modifier
                    .testTag("conversationLastMessage")
            )
        }
    }
}

@Composable
private fun ConversationDataRow(conversation: ConversationInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = conversation.userName,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .testTag("conversationUserName"),
            style = MaterialTheme.typography.body1
        )
        Text(
            text = conversation.lastSeen,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.testTag("conversationLastSeen")
        )
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
            lastSeen = "Aug 31, 2022 13:45",
            connectionState = ConversationConnectionState.Connected,
            lastMessage = "Hello"
        )
    )

}
