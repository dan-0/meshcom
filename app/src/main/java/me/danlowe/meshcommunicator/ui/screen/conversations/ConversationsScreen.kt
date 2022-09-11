package me.danlowe.meshcommunicator.ui.screen.conversations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
