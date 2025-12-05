package com.ethyllium.userservice.infrastructure.inbound.grpc

import com.ethyllium.userservice.domain.model.Conversation
import com.ethyllium.userservice.domain.model.Member
import com.ethyllium.userservice.domain.model.Room
import com.ethyllium.userservice.domain.port.driven.ConversationRepository
import com.ethyllium.userservice.domain.port.driven.MemberRepository
import com.ethyllium.userservice.domain.port.driven.RoomRepository
import com.ethyllium.userservice.infrastructure.util.UUIDUtils
import com.ethyllium.userservice.infrastructure.web.grpc.ConversationValidationRequest
import com.ethyllium.userservice.infrastructure.web.grpc.ConversationValidationStatus
import com.ethyllium.userservice.infrastructure.web.grpc.RoomValidationRequest
import com.ethyllium.userservice.infrastructure.web.grpc.RoomValidationStatus
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import java.util.UUID

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
            UUIDUtils.getConversationId(any<String>(), any<String>())
        } returns generatedConversationId
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(UUIDUtils)
    }

    @Test
    fun `validateConversation returns OK when conversation exists and is not blocked`() = runTest {
        val request = ConversationValidationRequest.newBuilder()
            .setSenderId(validSenderId)
            .setRecipientId(validRecipientId)
            .build()

        val mockConversation = mockk<Conversation>()
        every { mockConversation.isBlocked } returns false
        every { conversationRepository.select(generatedConversationId) } returns Mono.just(mockConversation)

        val response = validationService.validateConversation(request)

        assertEquals(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_OK, response.status)
        verify { UUIDUtils.getConversationId(validSenderId, validRecipientId) }
    }

    @Test
    fun `validateConversation returns BLOCKED when conversation is blocked`() = runTest {
        val request = ConversationValidationRequest.newBuilder()
            .setSenderId(validSenderId)
            .setRecipientId(validRecipientId)
            .build()

        val mockConversation = mockk<Conversation>()
        every { mockConversation.isBlocked } returns true
        every { conversationRepository.select(generatedConversationId) } returns Mono.just(mockConversation)

        val response = validationService.validateConversation(request)

        assertEquals(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_BLOCKED, response.status)
    }

    @Test
    fun `validateConversation returns NOT_FOUND when conversation does not exist`() = runTest {
        val request = ConversationValidationRequest.newBuilder()
            .setSenderId(validSenderId)
            .setRecipientId(validRecipientId)
            .build()

        every { conversationRepository.select(generatedConversationId) } returns Mono.empty()

        val response = validationService.validateConversation(request)

        assertEquals(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_NOT_FOUND, response.status)
    }

    @Test
    fun `validateConversation returns BAD_FORMAT when sender UUID is invalid`() = runTest {
        val request = ConversationValidationRequest.newBuilder()
            .setSenderId("invalid-uuid")
            .setRecipientId(validRecipientId)
            .build()

        val response = validationService.validateConversation(request)

        assertEquals(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_BAD_FORMAT, response.status)

        verify(exactly = 0) { conversationRepository.select(any()) }
    }

    @Test
    fun `validateConversation returns BAD_FORMAT when recipient UUID is invalid`() = runTest {
        val request = ConversationValidationRequest.newBuilder()
            .setSenderId(validSenderId)
            .setRecipientId("invalid-uuid")
            .build()

        val response = validationService.validateConversation(request)

        assertEquals(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_BAD_FORMAT, response.status)
        verify(exactly = 0) { conversationRepository.select(any()) }
    }

    @Test
    fun `validateRoom returns OK when room exists and member is allowed`() = runTest {
        val request = RoomValidationRequest.newBuilder()
            .setSenderId(validSenderId)
            .setRoomId(validRoomId)
            .build()

        val mockRoom = mockk<Room>()
        val mockMember = mockk<Member>()
        every { mockMember.isAllowedToSendMedia } returns true

        every { roomRepository.get(UUID.fromString(validRoomId)) } returns Mono.just(mockRoom)
        every { memberRepository.get(UUID.fromString(validSenderId)) } returns Mono.just(mockMember)

        val response = validationService.validateRoom(request)

        assertEquals(RoomValidationStatus.ROOM_VALIDATION_STATUS_OK, response.status)
    }

    @Test
    fun `validateRoom returns ROOM_NOT_FOUND when room does not exist`() = runTest {
        val request = RoomValidationRequest.newBuilder()
            .setSenderId(validSenderId)
            .setRoomId(validRoomId)
            .build()

        every { roomRepository.get(UUID.fromString(validRoomId)) } returns Mono.empty()
        every { memberRepository.get(UUID.fromString(validSenderId)) } returns Mono.just(mockk())

        val response = validationService.validateRoom(request)

        assertEquals(RoomValidationStatus.ROOM_VALIDATION_STATUS_ROOM_NOT_FOUND, response.status)
    }

    @Test
    fun `validateRoom returns SENDER_NOT_MEMBER when member does not exist`() = runTest {
        val request = RoomValidationRequest.newBuilder()
            .setSenderId(validSenderId)
            .setRoomId(validRoomId)
            .build()

        val mockRoom = mockk<Room>()
        every { roomRepository.get(UUID.fromString(validRoomId)) } returns Mono.just(mockRoom)
        every { memberRepository.get(UUID.fromString(validSenderId)) } returns Mono.empty()

        val response = validationService.validateRoom(request)

        assertEquals(RoomValidationStatus.ROOM_VALIDATION_STATUS_SENDER_NOT_MEMBER, response.status)
    }

    @Test
    fun `validateRoom returns SENDER_BLOCKED_FROM_ROOM when member is not allowed`() = runTest {
        val request = RoomValidationRequest.newBuilder()
            .setSenderId(validSenderId)
            .setRoomId(validRoomId)
            .build()

        val mockRoom = mockk<Room>()
        val mockMember = mockk<Member>()
        every { mockMember.isAllowedToSendMedia } returns false

        every { roomRepository.get(UUID.fromString(validRoomId)) } returns Mono.just(mockRoom)
        every { memberRepository.get(UUID.fromString(validSenderId)) } returns Mono.just(mockMember)

        val response = validationService.validateRoom(request)

        assertEquals(RoomValidationStatus.ROOM_VALIDATION_STATUS_SENDER_BLOCKED_FROM_ROOM, response.status)
    }

    @Test
    fun `validateRoom throws IllegalArgumentException for invalid Room UUID`() = runTest {
        val request = RoomValidationRequest.newBuilder()
            .setSenderId(validSenderId)
            .setRoomId("invalid-room-uuid")
            .build()

        assertThrows<IllegalArgumentException> {
            validationService.validateRoom(request)
        }

        verify(exactly = 0) { roomRepository.get(any()) }
    }

    @Test
    fun `validateRoom throws IllegalArgumentException for invalid Sender UUID`() = runTest {
        val request = RoomValidationRequest.newBuilder()
            .setSenderId("invalid-sender-uuid")
            .setRoomId(validRoomId)
            .build()

        assertThrows<IllegalArgumentException> {
            validationService.validateRoom(request)
        }

        verify(exactly = 0) { memberRepository.get(any()) }
    }
}