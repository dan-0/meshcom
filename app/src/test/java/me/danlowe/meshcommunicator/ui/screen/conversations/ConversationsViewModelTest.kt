package me.danlowe.meshcommunicator.ui.screen.conversations

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import me.danlowe.meshcommunicator.features.db.conversations.ContactDto
import me.danlowe.meshcommunicator.features.db.conversations.ContactsDao
import me.danlowe.meshcommunicator.features.db.messages.MessagesDao
import me.danlowe.meshcommunicator.features.nearby.AppConnectionHandler
import me.danlowe.meshcommunicator.features.nearby.data.ExternalUserId
import me.danlowe.meshcommunicator.test.base.BaseUnitTest
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationConnectionState
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationInfo
import me.danlowe.meshcommunicator.ui.screen.conversations.data.ConversationsState
import me.danlowe.meshcommunicator.util.ext.asMilliToInstant
import me.danlowe.meshcommunicator.util.helper.time.TimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationsViewModelTest : BaseUnitTest() {

    private lateinit var ut: ConversationsViewModel

    private val savedStateHandle = SavedStateHandle()

    @RelaxedMockK
    lateinit var contactsDao: ContactsDao

    @RelaxedMockK
    lateinit var messagesDao: MessagesDao

    @RelaxedMockK
    lateinit var appConnectionHandler: AppConnectionHandler

    @RelaxedMockK
    lateinit var timeFormatter: TimeFormatter

    private lateinit var states: List<ConversationsState>

    override fun setup() {
        super.setup()

        ut = ConversationsViewModel(
            dispatchers = testDispatcher,
            savedStateHandle = savedStateHandle,
            contactsDao = contactsDao,
            messagesDao = messagesDao,
            appConnectionHandler = appConnectionHandler,
            timeFormatter = timeFormatter
        )

        states = ut.state.toLiveList()
    }

    @Test
    fun initialState() = coRunTest {
        val expected = listOf(ConversationsState.Loading)

        assertEquals(expected, states)
        verify(exactly = 1) { appConnectionHandler.start() }
    }

    @Test
    fun contentStateHappyPath() = coRunTest {
        val lastMessage = "Hello"
        val lastSeenTime = BASE_CONTACT_DTO.lastSeen.toString()

        val expected = listOf(
            ConversationsState.Loading,
            ConversationsState.Content(listOf()),
            ConversationsState.Content(
                listOf(
                    ConversationInfo(
                        userName = BASE_CONTACT_DTO.userName,
                        userId = BASE_CONTACT_DTO.externalUserId,
                        lastSeen = lastSeenTime,
                        connectionState = ConversationConnectionState.Connected,
                        lastMessage = lastMessage
                    )
                )
            )
        )

        // given
        coEvery {
            contactsDao.getAllAsFlow()
        } returns flowOf(listOf(BASE_CONTACT_DTO))

        coEvery {
            appConnectionHandler.activeConnectionsState
        } returns flowOf(setOf(ExternalUserId(BASE_CONTACT_DTO.externalUserId)))

        coEvery {
            messagesDao.getLastMessageByExternalUserId(BASE_CONTACT_DTO.externalUserId)
        } returns lastMessage

        every {
            timeFormatter.instantToMediumLocalizedDateTime(
                BASE_CONTACT_DTO.lastSeen.asMilliToInstant()
            )
        } returns lastSeenTime

        // when
        advanceUntilIdle()

        // then
        assertEquals(expected, states)
    }

    @Test
    fun contentStateNotConnected() = coRunTest {
        val lastMessage = "Hello"
        val lastSeenTime = BASE_CONTACT_DTO.lastSeen.toString()

        val expected = listOf(
            ConversationsState.Loading,
            ConversationsState.Content(listOf()),
            ConversationsState.Content(
                listOf(
                    ConversationInfo(
                        userName = BASE_CONTACT_DTO.userName,
                        userId = BASE_CONTACT_DTO.externalUserId,
                        lastSeen = lastSeenTime,
                        connectionState = ConversationConnectionState.NotConnected,
                        lastMessage = lastMessage
                    )
                )
            )
        )

        // given
        coEvery {
            contactsDao.getAllAsFlow()
        } returns flowOf(listOf(BASE_CONTACT_DTO))

        coEvery {
            appConnectionHandler.activeConnectionsState
        } returns flowOf(setOf())

        coEvery {
            messagesDao.getLastMessageByExternalUserId(BASE_CONTACT_DTO.externalUserId)
        } returns lastMessage

        every {
            timeFormatter.instantToMediumLocalizedDateTime(
                BASE_CONTACT_DTO.lastSeen.asMilliToInstant()
            )
        } returns lastSeenTime

        // when
        advanceUntilIdle()

        // then
        assertEquals(expected, states)
    }

    companion object {

        private val BASE_CONTACT_DTO = ContactDto(
            id = 0,
            userName = "Bob",
            externalUserId = "1234",
            lastSeen = 1663680538414
        )

    }
}
