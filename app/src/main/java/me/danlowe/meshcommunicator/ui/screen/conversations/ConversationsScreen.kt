package me.danlowe.meshcommunicator.ui.screen.conversations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ConversationsScreen(
    viewModel: ConversationsViewModel = hiltViewModel()
) {

    when (val state = viewModel.state.collectAsState(initial = ConversationsState.Loading).value) {
        is ConversationsState.Content -> ConversationsList(state.conversations)
        ConversationsState.Loading -> ConversationsLoading()
    }

}

@Composable
private fun ConversationsList(conversations: List<ConversationInfo>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(conversations) { conversation ->
            Column {
                Text(conversation.userName)
            }
        }
    }
}

@Composable
private fun ConversationsLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}