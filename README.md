## Overview

The Lynk Auth Service is a reactive, secure, and scalable microservice responsible for handling all aspects of user
authentication and authorization within the Lynk platform. It provides a centralized system for user registration,
login, multi-factor authentication (MFA), and token management using JSON Web Tokens (JWT).

Built with Kotlin and Spring WebFlux, it leverages a non-blocking, event-driven architecture to ensure high performance
and efficient resource utilization, making it suitable for modern, high-concurrency applications.

## Features

- **User Registration:** Secure sign-up flow using phone numbers with One-Time Password (OTP) verification via SMS.
- **User Login:**
    - Standard login with a registered phone number.
    - Support for Time-based One-Time Password (TOTP) for users with MFA enabled.
- **JWT-Based Authentication:**
    - Generates secure, signed Access Tokens and Refresh Tokens upon successful authentication.
    - Uses a robust KeyStore mechanism to manage signing keys securely.
- **Multi-Factor Authentication (MFA):**
    - Optional TOTP-based MFA can be enabled during registration.
    - Generates a QR code for easy setup with authenticator apps like Google Authenticator or Authy.
- **Token Management:**
    - **Token Refresh:** An endpoint to issue a new access token using a valid refresh token.
    - **Logout:** A secure logout mechanism that invalidates the user's current token.
- **Rate Limiting:** Protects critical endpoints (`/login`, `/register`) against brute-force and denial-of-service
  attacks using a Redis-based algorithm.
- **Reactive Architecture:** Fully non-blocking stack from the web layer (WebFlux) to the database (R2DBC), ensuring
  optimal performance and scalability.

## Technology Stack

| Category                    | Technology                                                                                 |
|-----------------------------|--------------------------------------------------------------------------------------------|
| **Language**                | Kotlin                                                                                     |
| **Framework**               | Spring Boot 3 & Spring WebFlux (Reactive)                                                  |
| **Security**                | Spring Security (Reactive)                                                                 |
| **Primary Datastore**       | PostgreSQL (with R2DBC for reactive access)                                                |
| **Caching & Rate Limiting** | Redis                                                                                      |
| **Authentication**          | JSON Web Tokens (JWT) via `jjwt` library                                                   |
| **SMS Gateway**             | Twilio                                                                                     |
| **MFA / TOTP**              | `googleauth` for TOTP validation, `zxing` for QR code generation                           |
| **Testing**                 | JUnit 5, Mockito, WebTestClient, Testcontainers (for PostgreSQL & Redis integration tests) |
| **Build Tool**              | Gradle                                                                                     |

---

## API Documentation

Explore the Lynk Auth Service API endpoints, methods, and examples directly through our interactive Postman
documentation:

[![Run in Postman](https://run.pstmn.io/button.svg)](https://www.postman.com/descent-module-engineer-21435196/workspace/auth-service-api-documentation/collection/38027824-a62b3f10-24a4-4bcc-89f5-dc777eb81058?action=share&creator=38027824&active-environment=38027824-29bdf959-46b1-4c93-aeb5-8850b1c7f3b2)
*Or view the full interactive documentation here:*
[Lynk Auth Service API Documentation](https://www.postman.com/descent-module-engineer-21435196/workspace/auth-service-api-documentation/collection/38027824-a62b3f10-24a4-4bcc-89f5-dc777eb81058?action=share&creator=38027824&active-environment=38027824-29bdf959-46b1-4c93-aeb5-8850b1c7f3b2)

---

## Configuration

The service requires the following environment variables to be configured:

### JWT Keystore Configuration

The service uses a Java KeyStore (`.p12` or `.jceks`) for securely managing the secret key used for signing JWTs. If the
keystore file is not found at the specified location, the application will generate a new one automatically.

- `JWT_KEYSTORE_LOCATION`: Path to the keystore file (e.g., `/etc/secrets/jwtKeystore.p12`).
- `JWT_KEYSTORE_PASSWORD`: The password for the keystore.
- `JWT_KEY_ALIAS`: The alias for the key entry within the keystore (defaults to `jwtKey`).
- `JWT_KEY_PASSWORD`: The password for the key itself.

### JWT Token Expiry

- `JWT_TOKEN_ACCESS_TOKEN_EXPIRY`: Expiration time for access tokens in milliseconds.
- `JWT_TOKEN_REFRESH_TOKEN_EXPIRY`: Expiration time for refresh tokens in milliseconds.

### Twilio Configuration (for SMS)

- `TWILIO_ACCOUNT_SID`: Your Twilio Account SID.
- `TWILIO_AUTH_TOKEN`: Your Twilio Auth Token.
- `TWILIO_PHONE_NUMBER`: The Twilio phone number used to send SMS.

### Other Configurations

- `ISSUER`: The issuer name used in the TOTP URI (e.g., "LynkApp").
- `SPRING_R2DBC_URL`: R2DBC URL for the PostgreSQL database.
- `SPRING_R2DBC_USERNAME`: Database username.
- `SPRING_R2DBC_PASSWORD`: Database password.
- `SPRING_DATA_REDIS_HOST`: Redis host.
- `SPRING_DATA_REDIS_PORT`: Redis port.

## How to Run

### Prerequisites

- JDK 17 or later
- Docker and Docker Compose
- A configured Twilio account for SMS functionality

### Running Locally

1. **Clone the repository:**
    ```bash
    git clone https://github.com/omniCoder77/Lynk.git
    cd Lynk
    ```

2. **Set Environment Variables:**
   Create a `.env` file in the root of the `Lynk` directory and populate it with the required configuration values.

3. **Create JWT keys**
    ```bash
   ./init/generate_jwt.sh
    ```

4. **Create Certificates**
    ```bash
   ./init/generate_ca.sh
    ./init/generate_kafka_certs.sh
    ./init/generate_postgres_certs.sh
    ./init/generate_redis_certs.sh
    ./init/generate_cassandra_certs.sh
    ```

5. **Initialize the services**
    ```bash
    docker compose up -d cassandra, postgres, kafka
    ```
   Once cassandra and postgres are up, run the CQL script to create the necessary keyspace and tables and :
    ```bash
   docker exec -it cassandra cqlsh -f /init/init-cassandra.cql
   sudo chmod 640 init/postgres/postgres.key
    ```

6. **Initialize users in kafka**
    ```bash
   docker exec kafka /usr/bin/kafka-configs --bootstrap-server localhost:9094 --alter --add-config 'SCRAM-SHA-512=[iterations=8192,password=message-service],SCRAM-SHA-512=[password=message-service]' --entity-type users --entity-name messageService
   docker exec kafka /usr/bin/kafka-configs --bootstrap-server localhost:9094 --alter --add-config 'SCRAM-SHA-512=[iterations=8192,password=notification-service],SCRAM-SHA-512=[password=notification-service]' --entity-type users --entity-name notificationService
   docker exec kafka /usr/bin/kafka-acls --bootstrap-server localhost:9094 --add --allow-principal User:messageService --operation Write --topic room.message --topic conversation.message --topic user.created
   docker exec kafka /usr/bin/kafka-acls --bootstrap-server localhost:9094 --add --allow-principal User:notificationService --operation Read --topic room.message --topic conversation.message --topic user.created
    ```

7. **Start the Services:**
    ```bash
   docker compose up -d
    ```