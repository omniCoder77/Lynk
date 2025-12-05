package com.ethyllium.userservice.infrastructure.inbound.grpc

import com.ethyllium.userservice.domain.model.Conversation
import com.ethyllium.userservice.domain.port.driven.ConversationRepository
import com.ethyllium.userservice.domain.port.driven.MemberRepository
import com.ethyllium.userservice.domain.port.driven.RoomRepository
import com.ethyllium.userservice.infrastructure.util.UUIDUtils
import com.ethyllium.userservice.infrastructure.web.grpc.ConversationValidationRequest
import com.ethyllium.userservice.infrastructure.web.grpc.ConversationValidationStatus
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.util.*

class ValidationServiceTest {

    @MockK
    lateinit var conversationRepository: ConversationRepository

    @MockK
    lateinit var roomRepository: RoomRepository

    @MockK
    lateinit var memberRepository: MemberRepository

    @InjectMockKs
    lateinit var validationService: ValidationService

    private val validSenderId = "11111111-1111-1111-1111-111111111111"
    private val validRecipientId = "22222222-2222-2222-2222-222222222222"
    private val validRoomId = "33333333-3333-3333-3333-333333333333"
    private val generatedConversationId = UUID.fromString("44444444-4444-4444-4444-444444444444")

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(UUIDUtils)

        every {
            UUIDUtils.merge(any<String>(), any<String>())
        } returns generatedConversationId
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(UUIDUtils)
    }

    @Test
    fun `validateConversation returns OK when conversation exists and is not blocked`() = runTest {
        val request =
            ConversationValidationRequest.newBuilder().setSenderId(validSenderId).setRecipientId(validRecipientId)
                .build()

        val mockConversation = mockk<Conversation>()
        every { mockConversation.isBlocked } returns false
        every { conversationRepository.select(generatedConversationId) } returns Mono.just(mockConversation)

        val response = validationService.validateConversation(request)

        assertEquals(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_OK, response.status)
        verify { UUIDUtils.merge(validSenderId, validRecipientId) }
    }

    @Test
    fun `validateConversation returns BLOCKED when conversation is blocked`() = runTest {
        val request =
            ConversationValidationRequest.newBuilder().setSenderId(validSenderId).setRecipientId(validRecipientId)
                .build()

        val mockConversation = mockk<Conversation>()
        every { mockConversation.isBlocked } returns true
        every { conversationRepository.select(generatedConversationId) } returns Mono.just(mockConversation)

        val response = validationService.validateConversation(request)

        assertEquals(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_BLOCKED, response.status)
    }

    @Test
    fun `validateConversation returns NOT_FOUND when conversation does not exist`() = runTest {
        val request =
            ConversationValidationRequest.newBuilder().setSenderId(validSenderId).setRecipientId(validRecipientId)
                .build()

        every { conversationRepository.select(generatedConversationId) } returns Mono.empty()

        val response = validationService.validateConversation(request)

        assertEquals(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_NOT_FOUND, response.status)
    }

    @Test
    fun `validateConversation returns BAD_FORMAT when sender UUID is invalid`() = runTest {
        val request =
            ConversationValidationRequest.newBuilder().setSenderId("invalid-uuid").setRecipientId(validRecipientId)
                .build()

        val response = validationService.validateConversation(request)

        assertEquals(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_BAD_FORMAT, response.status)

        verify(exactly = 0) { conversationRepository.select(any()) }
    }

    @Test
    fun `validateConversation returns BAD_FORMAT when recipient UUID is invalid`() = runTest {
        val request =
            ConversationValidationRequest.newBuilder().setSenderId(validSenderId).setRecipientId("invalid-uuid").build()

        val response = validationService.validateConversation(request)

        assertEquals(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_BAD_FORMAT, response.status)
        verify(exactly = 0) { conversationRepository.select(any()) }
    }
}