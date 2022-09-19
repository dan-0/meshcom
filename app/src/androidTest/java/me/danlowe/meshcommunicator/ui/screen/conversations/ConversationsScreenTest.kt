package me.danlowe.meshcommunicator.ui.screen.conversations

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import me.danlowe.meshcommunicator.test.base.BaseComposeHiltTest
import me.danlowe.meshcommunicator.test.helper.assertBackgroundColor
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationConnectionState
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationInfo
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationsNavEvent
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationsState
import me.danlowe.meshcommunicator.ui.theme.MeshCommunicatorTheme
import me.danlowe.meshcommunicator.ui.theme.OkGreen
import me.danlowe.meshcommunicator.ui.theme.UnknownGrey
import org.junit.Test

@HiltAndroidTest
class ConversationsScreenTest : BaseComposeHiltTest() {

    @BindValue
    val viewModel: ConversationsViewModel = mockk(relaxed = true)

    private lateinit var stateFlow: MutableStateFlow<ConversationsState>

    private val navEvents = mutableListOf<ConversationsNavEvent>()

    override fun setup() {

        stateFlow = MutableStateFlow(ConversationsState.Loading)

        every { viewModel.state } returns stateFlow

        composeTestRule.setContent {
            MeshCommunicatorTheme {
                ConversationsScreen(viewModel, ::handleNav)
            }
        }
    }

    @Test
    fun loadingState() {

        composeTestRule
            .onNodeWithTag("fullLoadingScreen")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("conversationList")
            .assertDoesNotExist()

    }

    @Test
    fun singleConversation() {

        stateFlow.tryEmit(ConversationsState.Content(listOf(BASE_CONVERSATION_INFO)))

        composeTestRule
            .onNodeWithTag("conversationUserName", useUnmergedTree = true)
            .assertTextEquals(BASE_CONVERSATION_INFO.userName)

        composeTestRule
            .onNodeWithTag("conversationLastSeen", useUnmergedTree = true)
            .assertTextEquals(BASE_CONVERSATION_INFO.lastSeen)

        composeTestRule
            .onNodeWithTag("conversationLastMessage", useUnmergedTree = true)
            .assertTextEquals("\"${BASE_CONVERSATION_INFO.lastMessage}\"")

        composeTestRule
            .onNodeWithTag("conversationStateIndicator", useUnmergedTree = true)
            .assertBackgroundColor(UnknownGrey)
    }

    @Test
    fun singleConversationActive() {

        stateFlow.tryEmit(
            ConversationsState.Content(
                listOf(BASE_CONVERSATION_INFO.copy(connectionState = ConversationConnectionState.Connected))
            )
        )

        composeTestRule
            .onNodeWithTag("conversationStateIndicator", useUnmergedTree = true)
            .assertBackgroundColor(OkGreen)
    }

    @Test
    fun singleConversationSwitchBetweenActiveAndNot() {

        stateFlow.tryEmit(
            ConversationsState.Content(
                listOf(
                    BASE_CONVERSATION_INFO.copy(
                        connectionState = ConversationConnectionState.Connected
                    )
                )
            )
        )

        composeTestRule
            .onNodeWithTag("conversationStateIndicator", useUnmergedTree = true)
            .assertBackgroundColor(OkGreen)

        stateFlow.tryEmit(
            ConversationsState.Content(
                listOf(BASE_CONVERSATION_INFO)
            )
        )

        composeTestRule
            .onNodeWithTag("conversationStateIndicator", useUnmergedTree = true)
            .assertBackgroundColor(UnknownGrey)
    }

    @Test
    fun singleConversationNoLastMessage() {

        stateFlow.tryEmit(
            ConversationsState.Content(
                listOf(BASE_CONVERSATION_INFO.copy(lastMessage = null))
            )
        )

        composeTestRule
            .onNodeWithTag("conversationLastMessage", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun twoMessages() {
        val secondUserName = "Sally"
        stateFlow.tryEmit(
            ConversationsState.Content(
                listOf(
                    BASE_CONVERSATION_INFO,
                    BASE_CONVERSATION_INFO.copy(userName = secondUserName)
                )
            )
        )

        composeTestRule
            .onNodeWithText(BASE_CONVERSATION_INFO.userName)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(secondUserName)
            .assertIsDisplayed()
    }

    private fun handleNav(event: ConversationsNavEvent) {
        navEvents.add(event)
    }

    companion object {

        private val BASE_CONVERSATION_INFO = ConversationInfo(
            userName = "Bob",
            userId = "1234",
            lastSeen = "yesterday",
            connectionState = ConversationConnectionState.NotConnected,
            lastMessage = "Hello"
        )

    }
}