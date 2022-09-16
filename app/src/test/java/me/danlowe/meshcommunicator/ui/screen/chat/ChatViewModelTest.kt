package me.danlowe.meshcommunicator.ui.screen.chat

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import me.danlowe.meshcommunicator.AppSettings
import me.danlowe.meshcommunicator.features.appsettings.LiveAppSettings
import me.danlowe.meshcommunicator.features.db.messages.MessageDto
import me.danlowe.meshcommunicator.features.db.messages.MessagesDao
import me.danlowe.meshcommunicator.features.nearby.AppConnectionHandler
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.features.nearby.data.NearbyMessageResult
import me.danlowe.meshcommunicator.nav.AppDestinations
import me.danlowe.meshcommunicator.test.base.BaseUnitTest
import me.danlowe.meshcommunicator.ui.screen.chat.data.ChatData
import me.danlowe.meshcommunicator.ui.screen.chat.data.ChatState
import me.danlowe.meshcommunicator.util.helper.time.TimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest : BaseUnitTest() {

    private lateinit var ut: ChatViewModel

    @RelaxedMockK
    lateinit var messagesDao: MessagesDao

    @RelaxedMockK
    lateinit var appConnectionHandler: AppConnectionHandler

    @RelaxedMockK
    lateinit var appSettings: LiveAppSettings

    @RelaxedMockK
    lateinit var timeFormatter: TimeFormatter

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var states: List<ChatState>

    override fun setup() {
        super.setup()

        every {
            appSettings.appSettings
        } returns MutableStateFlow(TEST_APP_SETTINGS)

        savedStateHandle = SavedStateHandle()
            .apply {
                set(AppDestinations.Chat.PARAM_EXTERNAL_USER_ID, EXTERNAL_USER_ID)
                set("ConversationViewModelState", ChatState.Loading)
            }

        ut = ChatViewModel(
            savedStateHandle = savedStateHandle,
            dispatchers = testDispatcher,
            messagesDao = messagesDao,
            appConnectionHandler = appConnectionHandler,
            appSettings = appSettings,
            timeFormatter = timeFormatter
        )

        states = ut.state.toLiveList()
    }

    @Test
    fun initialState() = coRunTest {
        val expected = listOf(ChatState.Loading)
        assertEquals(expected, states)
    }

    @Test
    fun singleSentMessage() = coRunTest {
        val expected = listOf(
            ChatState.Loading,
            ChatState.Content(listOf(CHAT_DATA_SENT))
        )

        // given
        every { timeFormatter.instantToMediumLocalizedDateTime(any()) } returns TEST_TIME_SENT
        coEvery { messagesDao.getAllAsFlow() } returns flowOf(listOf(MESSAGE_DTO_SENT))

        // when
        advanceUntilIdle()

        // then
        assertEquals(expected, states)
    }

    @Test
    fun singleReceivedMessage() = coRunTest {
        val expected = listOf(
            ChatState.Loading,
            ChatState.Content(listOf(CHAT_DATA_RECEIVED))
        )

        // given
        every { timeFormatter.instantToMediumLocalizedDateTime(any()) } returns TEST_TIME_SENT
        coEvery { messagesDao.getAllAsFlow() } returns flowOf(listOf(MESSAGE_DTO_RECEIVED))

        // when
        advanceUntilIdle()

        // then
        assertEquals(expected, states)
    }

    @Test
    fun singleSentMessageNotReceived() = coRunTest {
        val expected = listOf(
            ChatState.Loading,
            ChatState.Content(listOf(CHAT_DATA_SENT.copy(timeReceived = null)))
        )

        // given
        every { timeFormatter.instantToMediumLocalizedDateTime(any()) } returns TEST_TIME_SENT

        coEvery { messagesDao.getAllAsFlow() } returns flowOf(
            listOf(MESSAGE_DTO_SENT.copy(timeReceived = MessageDto.NO_RECEIVED_TIME))
        )

        // when
        advanceUntilIdle()

        // then
        assertEquals(expected, states)
    }

    @Test
    fun sentAndReceivedMessages() = coRunTest {
        val expected = listOf(
            ChatState.Loading,
            ChatState.Content(listOf(CHAT_DATA_RECEIVED, CHAT_DATA_SENT))
        )

        // given
        every { timeFormatter.instantToMediumLocalizedDateTime(any()) } returns TEST_TIME_SENT

        coEvery { messagesDao.getAllAsFlow() } returns flowOf(
            listOf(MESSAGE_DTO_RECEIVED, MESSAGE_DTO_SENT)
        )

        // when
        advanceUntilIdle()

        // then
        assertEquals(expected, states)
    }

    @Test
    fun sendMessage() = coRunTest {
        val testMessage = "test message"

        // given
        every { timeFormatter.instantToMediumLocalizedDateTime(any()) } returns TEST_TIME_SENT

        coEvery { messagesDao.getAllAsFlow() } returns flowOf(
            listOf(MESSAGE_DTO_RECEIVED, MESSAGE_DTO_SENT)
        )

        // when
        advanceUntilIdle()
        ut.sendMessage(testMessage)
        advanceUntilIdle()

        // then
        coVerify(exactly = 1) {
            appConnectionHandler.sendMessage(
                externalUserId = ExternalUserId(EXTERNAL_USER_ID),
                message = testMessage,
                // randomly generated ID
                messageId = any()
            )
        }

    }

    @Test
    fun observeMessagesRetry() = coRunTest {
        // given
        every { timeFormatter.instantToMediumLocalizedDateTime(any()) } returns TEST_TIME_SENT

        coEvery { messagesDao.getAllAsFlow() } returns flowOf(
            listOf(MESSAGE_DTO_RECEIVED)
        )

        // when
        advanceUntilIdle()
        ut.observeMessages()
        advanceUntilIdle()

        // then
        coVerify(exactly = 2) {
            messagesDao.getAllAsFlow()
        }
    }

    companion object {

        private const val EXTERNAL_USER_ID = "externalUserId"

        private const val TEST_TIME_SENT = "testTimeSent"

        private const val TEST_TIME_LONG = 1663344644141L

        private const val TEST_TIME_STRING = "2022-09-16T16:10:44.141Z"

        private val TEST_APP_SETTINGS = AppSettings
            .newBuilder()
            .setUserId("idCurrentUser")
            .setUserName("nameCurrentUser")
            .build()

        private val MESSAGE_DTO_SENT = MessageDto(
            id = 0,
            uuid = "1234",
            originUserId = TEST_APP_SETTINGS.userId,
            targetUserId = EXTERNAL_USER_ID,
            message = "Hello from current user",
            timeSent = 0,
            timeReceived = TEST_TIME_LONG,
            sendState = NearbyMessageResult.Success
        )

        private val MESSAGE_DTO_RECEIVED = MessageDto(
            id = 0,
            uuid = "1234",
            originUserId = EXTERNAL_USER_ID,
            targetUserId = TEST_APP_SETTINGS.userId,
            message = "Hello from other user",
            timeSent = 0,
            timeReceived = TEST_TIME_LONG,
            sendState = NearbyMessageResult.None
        )

        private val CHAT_DATA_SENT = ChatData.SentChat(
            message = MESSAGE_DTO_SENT.message,
            timeSent = TEST_TIME_SENT,
            timeReceived = TEST_TIME_STRING
        )

        private val CHAT_DATA_RECEIVED = ChatData.ReceivedChat(
            message = MESSAGE_DTO_RECEIVED.message,
            timeSent = TEST_TIME_SENT,
            timeReceived = TEST_TIME_STRING,
            originUserId = ExternalUserId(id = EXTERNAL_USER_ID)
        )
    }
}