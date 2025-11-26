# Lynk API Documentation

This document outlines the main API endpoints for the Lynk microservices platform.

## 1. Auth Service

**Base URL:** `/api/v1/auth`

| Endpoint                        | Method | Description                                                               | Request Body (DTO)                                                | Success Response (200 OK)                        | Error Codes                                                                   |
|---------------------------------|--------|---------------------------------------------------------------------------|-------------------------------------------------------------------|--------------------------------------------------|-------------------------------------------------------------------------------|
| `/register`                     | `POST` | Registers a new user and initiates OTP verification.                      | `RegisterRequest`                                                 | `RegisterResponse.OTP` or `RegisterResponse.MFA` | 400 (Bad Request), 500 (SMS Send Failed)                                      |
| `/register/{phoneNumber}/{otp}` | `GET`  | Verifies the OTP sent during registration.                                | None                                                              | `RegisterResponse.Token`                         | 400 (Invalid OTP), 408 (Expired OTP)                                          |
| `/login`                        | `POST` | Authenticates a user. Handles TOTP and non-enabled (OTP required) states. | `LoginRequest`                                                    | `LoginResponse.Token`                            | 403 (OTP Sent), 404 (User Not Found), 401 (TOTP Invalid), 400 (TOTP Required) |
| `/logout`                       | `POST` | Invalidates the user's current access token.                              | None (`Authorization` Header required)                            | `200 OK`                                         | 500 (Internal Server Error)                                                   |
| `/token/refresh`                | `POST` | Generates a new access token using a valid refresh token.                 | None (`Authorization` Header required - containing refresh token) | `200 OK` (New Access Token as String)            | 400 (Invalid Token)                                                           |

### Request DTOs

**`RegisterRequest`**
```json
{
  "phoneNumber": "String",
  "firstName": "String",
  "lastName": "String",
  "mfa": "Boolean"
}
```

**`LoginRequest`**
```json
{
  "phoneNumber": "String",
  "totp": "String?"
}
```

### Response DTOs

**`RegisterResponse` (Sealed Interface with type property)**
| Type | Example | Description |
|---|---|---|
| `otp` | `{ "type": "otp" }` | OTP has been sent to the phone number. |
| `mfa` | `{ "type": "mfa", "qrCode": "base64_qr_code" }` | MFA is enabled, return the QR code to set up. |
| `token` | `{ "type": "token", "accessToken": "...", "refreshToken": "..." }` | OTP verified, returns JWT tokens. |

**`LoginResponse` (Sealed Interface with Status Codes)**
| Status | Type | Example | Description |
|---|---|---|---|
| 200 OK | `Token` | `{ "accessToken": "...", "refreshToken": "..." }` | Successful login. |
| 403 Forbidden | `OtpSent` | `{ "message": "Please enter the OTP..." }` | User is not enabled; OTP sent for first login. |
| 400 Bad Request | `TotpRequired` | `{ "message": "TOTP is required..." }` | User has MFA enabled but `totp` field is missing. |
| 401 Unauthorized | `TotpInvalid` | `{ "message": "Invalid totp given" }` | Provided TOTP code is incorrect. |
| 404 Not Found | `UserNotFound` | `{ "message": "User not found..." }` | The phone number does not correspond to a registered user. |

---

## 2. Message Service

**Base URL:** `/api/v1/rooms`

**All endpoints require an `Authorization: Bearer <access-token>` header.**

### Room Management (HTTP)

| Endpoint             | Method | Description                                                                   | Request Body (DTO)                               | Success Response (201/200)                | Error Codes                         |
|----------------------|--------|-------------------------------------------------------------------------------|--------------------------------------------------|-------------------------------------------|-------------------------------------|
| `/`                  | `POST` | Creates a new room and sets the creator as ADMIN.                             | `CreateRoomRequest`                              | `201 Created` with `{ "roomId": "UUID" }` | 403 (Invalid Creator ID)            |
| `/`                  | `GET`  | Retrieves all rooms the authenticated user is a member of.                    | None                                             | `200 OK` (`Flux<Room>`)                   | 401 (Unauthorized)                  |
| `/{roomId}`          | `PUT`  | Updates room details (name, description, avatar URL). Only ADMINs can update. | `UpdateRoomRequest`                              | `200 OK`                                  | 401 (Unauthorized), 403 (Not Admin) |
| `/{roomId}/members`  | `GET`  | Retrieves all members of a specific room.                                     | None                                             | `200 OK` (`List<RoomMemberResponse>`)     | 401 (Unauthorized)                  |
| `/{roomId}/members`  | `POST` | Adds a new member to a room. Only ADMINs can add.                             | `AddRoomMemberRequest`                           | `201 Created`                             | 401 (Unauthorized), 403 (Not Admin) |
| `/{roomId}/messages` | `GET`  | Retrieves messages for a room within a time range.                            | Query Params: `start`, `end` (Instant, optional) | `200 OK` (`List<MessageResponse>`)        | 401 (Unauthorized)                  |
| `/{roomId}/messages` | `POST` | Sends a message to a room.                                                    | `SendMessageRequest`                             | `201 Created`                             | 500 (Internal Error)                |

### Request DTOs for Room Management

**`CreateRoomRequest`**
```json
{
  "name": "String",
  "description": "String?",
  "initialMemberIds": ["UUID"]
}
```

**`UpdateRoomRequest`**
```json
{
  "name": "String?",
  "description": "String?",
  "avatarUrl": "String?"
}
```

**`AddRoomMemberRequest`**
```json
{
  "memberId": "UUID",
  "role": "RoomRole"
}
```

**`SendMessageRequest`**
```json
{
  "content": "String",
  "replyToMessageId": "UUID?",
  "timestamp": "Instant"
}
```

### Real-time Communication (WebSockets)

**Protocols:** Standard WebSocket (`ws://` or `wss://`). Authentication is handled by passing the JWT in the initial HTTP upgrade handshake (or potentially as a query parameter if configured).

| Endpoint | Protocol | Description | Message Format (DTO) |
|---|---|---|---|
| `/ws/chat` | WebSocket | Used for real-time one-on-one message exchange. | `ChatWebsocketMessage` (JSON) |
| `/ws/room` | WebSocket | Used for real-time room/group message exchange. | `RoomWebsocketMessage` (JSON) |

**`ChatWebsocketMessage` (Sent by Client)**
```json
{
  "recipientId": "String (UUID)",
  "replyToMessageId": "String (UUID)?",
  "content": "String",
  "timestamp": "Instant"
}
```

**`RoomWebsocketMessage` (Sent by Client)**
```json
{
  "roomId": "String (UUID)",
  "replyToMessageId": "String (UUID)?",
  "content": "String",
  "timestamp": "Instant"
}
```

---

## 3. Notification Service

**Base URL:** `/api/v1`

**All endpoints require an `Authorization: Bearer <access-token>` header.**

| Endpoint             | Method | Description                                                                           | Success Response (200 OK)                      | Error Codes                              |
|----------------------|--------|---------------------------------------------------------------------------------------|------------------------------------------------|------------------------------------------|
| `/token/{token}`     | `POST` | Saves the client's FCM device token for push notifications.                           | `200 OK` ("Successfully registered the token") | 400 (Invalid JWT)                        |
| `/subscribe/{topic}` | `POST` | Subscribes the authenticated user's device to a specific FCM topic (e.g., a Room ID). | `200 OK`                                       | 400 (Invalid JWT), 404 (Token not found) |

---

## 4. Media Service

**Base URL:** `/api/v1/media`

**All endpoints require an `Authorization: Bearer <access-token>` header.**

| Endpoint      | Method   | Description                     | Security Role     | Request Body                           | Success Response (200 OK)                            | Error Codes          |
|---------------|----------|---------------------------------|-------------------|----------------------------------------|------------------------------------------------------|----------------------|
| `/`           | `POST`   | Uploads a new media file.       | `USER` or `ADMIN` | `multipart/form-data` with `file` part | `200 OK` (`{ "message": "...", "fileName": "..." }`) | 400 (Upload Failed)  |
| `/{fileName}` | `GET`    | Downloads a media file.         | `USER` or `ADMIN` | None                                   | `200 OK` (File as `application/octet-stream`)        | 404 (File Not Found) |
| `/{fileName}` | `PUT`    | Updates an existing media file. | `USER` or `ADMIN` | `multipart/form-data` with `file` part | `200 OK` (`{ "message": "..." }`)                    | 400 (Update Failed)  |
| `/{fileName}` | `DELETE` | Deletes a media file.           | `ADMIN`           | None                                   | `200 OK` (`{ "message": "..." }`)                    | 400 (Delete Failed)  |