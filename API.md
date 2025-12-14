# 📚 Lynk API Documentation

This documentation outlines the RESTful endpoints, WebSocket events, and internal gRPC definitions for the Lynk platform.

## 🔐 Authentication & Security

**Base URL:** Varies per service (via Load Balancer/Gateway in production, mapped ports in Dev).

**Headers:**
Unless specified as **Public**, all endpoints require the following header:
```http
Authorization: Bearer <access_token>
```

**Common Response Codes:**
*   `200 OK` - Success
*   `201 Created` - Resource created successfully
*   `400 Bad Request` - Validation error
*   `401 Unauthorized` - Invalid or expired token
*   `403 Forbidden` - Insufficient permissions (e.g., Non-Admin trying to kick user)
*   `404 Not Found` - Resource does not exist
*   `429 Too Many Requests` - Rate limit exceeded

---

## 🛡️ Auth Service
**Port:** `8081`

Handles user registration, login, and session management.

### Register
**`POST /api/v1/auth/register`** (Public)
Initiates user registration. Sends an OTP via SMS.

**Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "mfa": true
}
```

**Response:**
*   `200 OK`: `{"type": "otp"}` (OTP sent)
*   `200 OK`: `{"type": "mfa", "qrCode": "base64..."}` (If MFA enabled)

### Verify Registration OTP
**`GET /api/v1/auth/register/{phoneNumber}/{otp}`** (Public)
Verifies the SMS OTP to complete registration.

**Response:**
```json
{
  "type": "token",
  "accessToken": "eyJhbG...",
  "refreshToken": "eyJhbG..."
}
```

### Login
**`POST /api/v1/auth/login`** (Public)
Login via Phone Number. May trigger OTP or require TOTP.

**Body:**
```json
{
  "phoneNumber": "+1234567890",
  "totp": "123456"
}
```

**Response:**
*   `200 OK`: `{"accessToken": "...", "refreshToken": "..."}`
*   `403 Forbidden`: `{"message": "OTP Sent..."}` (OTP triggered, user needs to wait/verify)
*   `400 Bad Request`: `{"message": "TOTP required"}`

### Refresh Token
**`POST /api/v1/token/refresh`**
Refreshes an expired access token using a valid refresh token.

**Headers:**
`Authorization: Bearer <refresh_token>`

### Logout
**`POST /api/v1/auth/logout`**
Invalidates the current session.

---

## 👤 User Service
**Port:** `8085`

Manages user profiles, blocking logic, and 1:1 conversation metadata.

### User Profile
*   **`GET /users/me`**: Get current user details.
*   **`GET /users/{userId}`**: Get public profile of another user.
*   **`GET /users/search?username=john&page=0&size=20`**: Search users.
*   **`PATCH /users/me`**: Update profile.
    *   **Params:** `username`, `bio`, `profile` (avatar URL).

### Conversations (1:1 Metadata)
*   **`POST /conversation`**: Initialize a conversation.
    *   **Body:** `{"userId": "uuid-of-recipient"}`
*   **`GET /conversation`**: List all active 1:1 conversations.
*   **`DELETE /conversation/{recipientId}`**: Delete a conversation.
*   **`PATCH /conversation/block/{userId}`**: Block a user.
*   **`PATCH /conversation/unblock/{userId}`**: Unblock a user.
*   **`GET /users/me/blocked`**: List blocked users.

---

## 🚪 Room Service
**Port:** `8086`

Handles Group metadata, membership logic, roles, and moderation (bans/kicks).

### Room Management
*   **`POST /rooms`**: Create a new room.
    *   **Body:**
    ```json
    {
      "idempotencyKey": "uuid",
      "roomName": "Tech Talk",
      "maxSize": 100,
      "visibility": "PUBLIC"
    }
    ```
*   **`PATCH /rooms`**: Update room details (Admin/Mod).
*   **`DELETE /rooms/{roomId}`**: Delete a room (Admin).

### Membership
*   **`POST /memberships/{roomId}`**: Join a public room.
*   **`POST /memberships/leave/{roomId}`**: Leave a room.
*   **`POST /memberships/kick/{userId}/{roomId}`**: Kick a user (Admin/Mod).
*   **`GET /memberships/{roomId}/members`**: List all members in a room.
*   **`GET /memberships/my-rooms`**: List rooms the current user has joined.

### Moderation (Ban System)
*   **`POST /ban/{roomId}/{userId}`**: Ban a user from the room (Mod+).
*   **`DELETE /ban/{roomId}/{userId}`**: Unban a user (Mod+).

---

## 💬 Message Service
**Port:** `8082`

Handles real-time chat persistence (Cassandra), WebSocket connections, and online status.

### Room Chat History
*   **`GET /api/v1/rooms/{roomId}/messages`**: Get chat history.
    *   **Params:** `start` (Instant), `end` (Instant).
*   **`POST /api/v1/rooms/{roomId}/messages`**: Send a message via REST (Alternative to WS).
    *   **Body:** `{"content": "Hello", "replyToMessageId": "uuid"}`

### Room Metadata (Cassandra View)
*   **`POST /api/v1/rooms`**: Create chat room bucket.
*   **`GET /api/v1/rooms`**: Get user's chat rooms.
*   **`POST /api/v1/rooms/{roomId}/members`**: Add member to chat bucket.

### 🔌 WebSockets

**1. 1:1 Chat**
*   **Endpoint:** `ws://localhost:8082/ws/chat`
*   **Auth:** Bearer Token in Handshake headers.
*   **Payload (Send):**
    ```json
    {
      "recipientId": "uuid",
      "content": "Hello world",
      "replyToMessageId": "uuid (optional)"
    }
    ```

**2. Group Chat**
*   **Endpoint:** `ws://localhost:8082/ws/room`
*   **Payload (Send):**
    ```json
    {
      "roomId": "uuid",
      "content": "Hello Team",
      "replyToMessageId": "uuid (optional)"
    }
    ```

---

## 📸 Media Service
**Port:** `8084`

Handles secure file uploads to S3.

### Profile Images
*   **`POST /api/v1/media/user-profile`**: Upload user avatar.
    *   **Content-Type:** `multipart/form-data`
    *   **Key:** `file`
*   **`POST /api/v1/media/room-profile`**: Upload room avatar.
    *   **Pre-requisite:** Requires a valid session/cache entry validating the user is the room admin.

### File Management
*   **`GET /api/v1/media/{fileName}`**: Download/Stream file.
*   **`PUT /api/v1/media/{fileName}`**: Update existing file.
*   **`DELETE /api/v1/media/{fileName}`**: Delete file (Admin only).

---

## 🔔 Notification Service
**Port:** `8083`

Manages FCM tokens and push notifications.

*   **`POST /api/v1/token/{fcmToken}`**: Register a device's FCM token for the current user.
*   **`POST /api/v1/subscribe/{topic}`**: Subscribe the current user's device to a specific notification topic (e.g., `chat_room_123`).

---

## 🔗 Internal gRPC
**Port:** `9090` (User Service)

Used for inter-service communication (primarily Message Service checking validation rules).

**Service:** `ValidationService`
*   **`validateConversation(ConversationValidationRequest)`**: Checks if a sender/recipient pair is valid (exists and not blocked).
    *   Returns: `OK`, `NOT_FOUND`, `SENDER_BLOCKED_RECIPIENT`, `RECIPIENT_BLOCKED_SENDER`.