package me.danlowe.meshcommunicator.ui.screen.chat

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.test.base.BaseComposeHiltTest
import me.danlowe.meshcommunicator.ui.screen.chat.data.ChatData
import me.danlowe.meshcommunicator.ui.screen.chat.data.ChatState
import me.danlowe.meshcommunicator.ui.theme.MeshCommunicatorTheme
import me.danlowe.meshcommunicator.util.ext.EventFlow
import org.junit.Assert.assertEquals
import org.junit.Test

@HiltAndroidTest
class ChatScreenTest : BaseComposeHiltTest() {

    @BindValue
    val viewModel: ChatViewModel = mockk(relaxed = true)

    private lateinit var state: MutableSharedFlow<ChatState>

    override fun setup() {
        state = EventFlow()

        every {
            viewModel.state
        } returns state

        composeTestRule.setContent {
            MeshCommunicatorTheme {
                ChatScreen(viewModel)
            }
        }
    }

    @Test
    fun initialized() {
        onFullLoadingScreen
            .assertIsDisplayed()

        onBasicErrorView
            .assertDoesNotExist()

        onChatContent
            .assertDoesNotExist()
    }

    @Test
    fun loadingView() {
        state.tryEmit(ChatState.Loading)

        onFullLoadingScreen
            .assertIsDisplayed()

        onBasicErrorView
            .assertDoesNotExist()

        onChatContent
            .assertDoesNotExist()
    }

    @Test
    fun error() {
        state.tryEmit(ChatState.Error)

        onFullLoadingScreen
            .assertDoesNotExist()

        onChatContent
            .assertDoesNotExist()

        onBasicErrorView
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("refreshButton")
            .performClick()

        verify(exactly = 1) {
            viewModel.observeMessages()
        }

    }

    @Test
    fun contentState() {
        state.tryEmit(ChatState.Content(listOf()))

        onChatContent
            .assertIsDisplayed()

        onFullLoadingScreen
            .assertDoesNotExist()

        onBasicErrorView
            .assertDoesNotExist()
    }

    @Test
    fun receivedChatDisplayed() {
        state.tryEmit(ChatState.Content(listOf(RECEIVED_CHAT)))

        onChatContent
            .assertIsDisplayed()

        verifyChatMessageSize(1)

        composeTestRule
            .onNodeWithTag("messageToUser")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("messageText")
            .assertTextEquals(RECEIVED_CHAT.message)

        composeTestRule
            .onNodeWithTag("messageTimeText")
            .assertTextContains(RECEIVED_CHAT.timeSent)
    }

    @Test
    fun sentChatDisplayed() {
        state.tryEmit(ChatState.Content(listOf(SENT_CHAT)))

        onChatContent
            .assertIsDisplayed()

        verifyChatMessageSize(1)

        composeTestRule
            .onNodeWithTag("messageFromUser")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("messageText")
            .assertTextEquals(SENT_CHAT.message)

        composeTestRule.onNodeWithTag("messageReceivedIndicatorReceived")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("messageTimeText")
            .assertTextContains(SENT_CHAT.timeSent)
    }

    @Test
    fun sentChatDisplayedNotReceived() {
        val chatMessage = SENT_CHAT.copy(
            timeReceived = null
        )
        state.tryEmit(ChatState.Content(listOf(chatMessage)))

        onChatContent
            .assertIsDisplayed()

        verifyChatMessageSize(1)

        composeTestRule
            .onNodeWithTag("messageFromUser")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("messageText")
            .assertTextEquals(SENT_CHAT.message)

        composeTestRule
            .onNodeWithTag("messageReceivedIndicatorNotReceived")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("messageTimeText")
            .assertTextContains(SENT_CHAT.timeSent)
    }

    @Test
    fun sentAndReceivedMessages() {
        state.tryEmit(ChatState.Content(listOf(SENT_CHAT, RECEIVED_CHAT)))

        onChatContent
            .assertIsDisplayed()

        verifyChatMessageSize(2)

        composeTestRule
            .onNodeWithTag("messageFromUser")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("messageToUser")
            .assertIsDisplayed()
    }

    private val onChatMessages: SemanticsNodeInteraction
        get() = composeTestRule.onNodeWithTag("chatMessages")

    private val onBasicErrorView: SemanticsNodeInteraction
        get() = composeTestRule.onNodeWithTag("basicErrorView")

    private val onChatContent: SemanticsNodeInteraction
        get() = composeTestRule.onNodeWithTag("chatContent")

    private val onFullLoadingScreen: SemanticsNodeInteraction
        get() = composeTestRule.onNodeWithTag("fullLoadingScreen")

    private fun verifyChatMessageSize(expectedSize: Int) {
        onChatMessages
            .onChildren()
            .fetchSemanticsNodes()
            .apply {
                assertEquals(expectedSize, size)
            }
    }

    companion object {

        private const val TIME_SENT_RECEIVED_CHAT = "TIME_SENT_RECEIVED_CHAT"

        private const val TIME_SENT_SENT_CHAT = "TIME_SENT_SENT_CHAT"

        private val RECEIVED_CHAT = ChatData.ReceivedChat(
            originUserId = ExternalUserId(id = "123"),
            message = "Hello to user",
            timeSent = TIME_SENT_RECEIVED_CHAT,
            timeReceived = "yesterday"
        )

        private val SENT_CHAT = ChatData.SentChat(
            message = "Hello from user",
            timeSent = TIME_SENT_SENT_CHAT,
            timeReceived = "yesterday",
        )

    }

}