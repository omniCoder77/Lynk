# Synapse: A Resilient, Event-Driven E-Commerce Platform
Synapse is a sophisticated, backend-driven e-commerce platform built on a fully asynchronous, reactive microservices' architecture. It leverages **Kotlin**, the **Spring Ecosystem**, and modern, event-driven patterns to create a scalable, fault-tolerant, and high-performance system.

This project is not just a standard e-commerce application; it's a showcase of advanced software engineering principles, including **CQRS**, **Event Sourcing with an Outbox Pattern**, polyglot persistence, and **secure-by-design** development.

# 🚀 Live Demo & API Documentation
A consolidated API documentation for all services is exposed through the API Gateway via Swagger UI.

* Swagger UI (API Docs): http://localhost:8080/swagger-ui.html
* API Gateway Entrypoint: http://localhost:8080

*(Note: The links above are for the local development environment.)*
# 🏛️ System Architecture
Synapse is composed of several independent microservices that communicate through a combination of synchronous (gRPC, REST) and asynchronous (Kafka) protocols. This design ensures loose coupling, high availability, and independent scalability of each component.
``` mermaid
graph TD
    %% Client Layer
    subgraph ClientLayer ["🌐 Client Layer"]
        direction LR
        WebApp["📱 Web/Mobile Client<br/>React/Flutter"]
    end
    
    %% API Gateway Layer
    subgraph GatewayLayer ["🚪 API Gateway Layer"]
        Gateway["🔗 API Gateway<br/>Spring Cloud Gateway"]
    end
    
    %% Service Layer
    subgraph ServiceLayer ["⚙️ Microservices Layer"]
        direction LR
        AuthService["🔐 Authentication Service<br/>Spring Boot"]
        MessageService["💬 Message Service<br/>Spring Boot"]
        NotificationService["🔔 Notification Service<br/>Spring Boot"]
    end
    
    %% Data Layer
    subgraph DataLayer ["💾 Data Layer"]
        direction LR
        AuthDB[("🗄️ PostgreSQL<br/>User Data")]
        MessageDB[("📊 Cassandra<br/>Messages")]
        SearchDB[("🔍 Elasticsearch<br/>Search Index")]
        AuthCache[("⚡ Redis<br/>Sessions")]
    end
    
    %% Message Queue
    subgraph MessageQueue ["📨 Message Queue"]
        Kafka["🌊 Apache Kafka<br/>Event Streaming"]
    end
    
    %% Service Discovery
    subgraph ServiceDiscovery ["🗺️ Service Discovery"]
        Eureka["🎯 Eureka Server<br/>Service Registry"]
    end
    
    %% External Services
    subgraph ExternalServices ["🌍 External Services"]
        direction LR
        FCM["📲 FCM"]
        APNS["🍎 APNS"]
        WNS["🪟 WNS"]
    end
    
    %% Client to Gateway
    WebApp -.->|"🔒 HTTPS/WSS"| Gateway
    
    %% Gateway to Services
    Gateway -->|"⚡ gRPC"| AuthService
    Gateway -->|"🌐 REST"| MessageService
    Gateway -.->|"📡 WebSocket"| MessageService
    
    %% Services to Data
    AuthService -->|"🔗 R2DBC"| AuthDB
    AuthService -->|"⚡ Cache"| AuthCache
    MessageService -->|"📝 Store"| MessageDB
    MessageService -->|"🔍 Index"| SearchDB
    
    %% Event Streaming
    AuthService -->|"📤 Publish Events"| Kafka
    MessageService -->|"📤📥 Pub/Sub"| Kafka
    NotificationService -->|"📥 Consume Events"| Kafka
    
    %% Notifications
    NotificationService -->|"📨 Push"| FCM
    NotificationService -->|"📨 Push"| APNS
    NotificationService -->|"📨 Push"| WNS
    
    %% Service Discovery
    Gateway -.->|"🔍 Discover"| Eureka
    AuthService -.->|"📋 Register"| Eureka
    MessageService -.->|"📋 Register"| Eureka
    NotificationService -.->|"📋 Register"| Eureka
    
    %% Styling
    classDef clientStyle fill:#e1f5fe,stroke:#01579b,stroke-width:3px,color:#000
    classDef gatewayStyle fill:#fff3e0,stroke:#e65100,stroke-width:3px,color:#000
    classDef serviceStyle fill:#f3e5f5,stroke:#4a148c,stroke-width:2px,color:#000
    classDef dataStyle fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px,color:#000
    classDef queueStyle fill:#fff8e1,stroke:#f57f17,stroke-width:2px,color:#000
    classDef discoveryStyle fill:#fce4ec,stroke:#ad1457,stroke-width:2px,color:#000
    classDef externalStyle fill:#f1f8e9,stroke:#33691e,stroke-width:2px,color:#000
    
    class WebApp clientStyle
    class Gateway gatewayStyle
    class AuthService,MessageService,NotificationService serviceStyle
    class AuthDB,MessageDB,SearchDB,AuthCache dataStyle
    class Kafka queueStyle
    class Eureka discoveryStyle
    class FCM,APNS,WNS externalStyle
    
    %% Subgraph styling
    style ClientLayer fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    style GatewayLayer fill:#fff8e1,stroke:#f57c00,stroke-width:2px
    style ServiceLayer fill:#f8f4ff,stroke:#673ab7,stroke-width:2px
    style DataLayer fill:#e8f5e8,stroke:#388e3c,stroke-width:2px
    style MessageQueue fill:#fff9c4,stroke:#f9a825,stroke-width:2px
    style ServiceDiscovery fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    style ExternalServices fill:#f1f8e9,stroke:#689f38,stroke-width:2px
```

# ✨ Key Features & Service Breakdown
# ✨ Key Features & Service Breakdown

| Service                | Core Responsibilities & Features                                                                                                                                                                                                 | Key Technologies                                                   | Status       |
|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|--------------|
| **API Gateway**        | • Single entry point for all clients.<br>• Dynamic routing & service discovery.<br>• Centralized authentication filter.<br>• Rate limiting and Circuit Breaker patterns for resilience.                                          | Spring Cloud Gateway, WebFlux, <br>Resilience4j                    | ✅ Functional |
| **🔐 Auth Service**    | • Secure user registration & JWT-based authentication.<br>• MFA with TOTP (Google Authenticator) & QR code generation.<br>• Password reset and email verification flows.<br>• Secure credential storage & device fingerprinting. | Spring Security, R2DBC, JWT, Twilio, <br>Quartz, PostgreSQL, Redis | ✅ Functional |
| **📦 Product Service** | • Comprehensive product catalog management (CRUD).<br>• Rich product model with variants, SEO, media, and specifications.<br>• Role-based access control (RBAC) for sellers & admins.                                            | Spring Data MongoDB, MongoDB                                       | ✅ Functional |
| **🛍️ Order Service**  | • Manages the complete order lifecycle.<br>• Validates product availability via gRPC calls to the Product Service.<br>• Publishes `OrderCreated` events to Kafka.                                                                | Spring Data JPA, gRPC, Kafka, <br>PostgreSQL                       | ✅ Functional |
| **💳 Payment Service** | • Integration with **Razorpay** for order creation and payment processing.<br>• Secure webhook handling with signature verification.<br>• Implements the **Transactional Outbox Pattern** for reliable event publishing.         | Spring Data JPA, Kafka, <br>Razorpay API, PostgreSQL               | ✅ Functional |
| **🔍 Search Service**  | • Provides advanced, full-text product search.<br>• Consumes product events from Kafka to keep the search index synchronized.<br>• Offers filtering, sorting, and autocomplete suggestions.                                      | Spring Data Elasticsearch, <br>Elasticsearch                       | ✅ Functional |
# 💡 Technical Highlights & Design Patterns

This project goes beyond a simple implementation and showcases a deep understanding of modern backend engineering.

* **Event-Driven Architecture with Transactional Outbox**: The `payment-service` uses the **Outbox Pattern** to guarantee "at-least-once" delivery of critical business events. Events are written to a local database table within the same transaction as the business operation and then reliably published to Kafka by a separate process. This ensures data consistency across microservices, even in the event of publisher failure.
* **Polyglot Persistence**: The architecture deliberately uses different database technologies, each chosen for its strengths in handling a specific type of data:
    * **PostgreSQL**: For transactional, relational data in the `Auth`, `Order`, and `Payment` services.
    * **MongoDB**: For the flexible, document-based structure of the product catalog in the `Product Service`.
    * **Elasticsearch**: For powerful, fast, and complex search queries in the `Search Service`.
    * **Redis**: For caching, rate limiting, and managing ephemeral state like OTPs and sessions.
* **Reactive & Asynchronous Core**: Built from the ground up with a non-blocking stack (**Spring WebFlux**, **Project Reactor**, **R2DBC**) to handle high concurrency with efficient resource utilization, essential for a responsive e-commerce platform.
* **Secure by Design**: Security is a cornerstone of the platform, with features including:
    * **Centralized Authentication**: The API Gateway enforces JWT validation for all protected routes.
    * **Role-Based Access Control (RBAC)**: Fine-grained permissions are enforced at the controller level (e.g. `@RequiresRoles({"SELLER"})`).
    * **Secure Webhooks**: Payloads from Razorpay are verified using HMAC-SHA256 signatures to prevent tampering.
    * **MFA and Secure Credentials**: Strong password hashing (BCrypt), TOTP, and secure key management using a Java KeyStore.
* **High-Performance Inter-Service Communication**: The system uses a mix of communication styles:
    * **gRPC**: For low-latency, synchronous communication where a direct response is needed (e.g., `Order Service` validating products with `Product Service`).
    * **Kafka**: For asynchronous, event-driven communication to decouple services and improve resilience.
    * **REST/HTTP**: For external client-facing APIs.
# 🛠️ Tech Stack
| Category                     | Technologies                                                                                                                |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| **Languages and Frameworks** | `Kotlin, Spring Boot, Spring Cloud (Gateway, OpenFeign), Spring Data (JPA, MongoDB, Elasticsearch, R2DBC), Spring Security` |
| Databases                    | 	PostgreSQL, MongoDB, Elasticsearch, Redis                                                                                  |
 | Communication                | 	RESTful APIs, gRPC, Apache Kafka, WebSockets (STOMP)                                                                       |
 | Authentication               | 	JWT, MFA/TOTP, BCrypt, Java KeyStore (JCEKS)                                                                               |
 | External APIs                | 	Razorpay, Twilio                                                                                                           |
 | DevOps & Tooling             | 	Gradle, Docker, Swagger/OpenAPI, Ehcache                                                                                   |
# 🚀 Getting Started
The entire platform can be run locally using Docker and Docker Compose.
Prerequisites
* Java 17 or higher
* Docker & Docker Compose

Local Setup
1. Environment Variables: Create a `.env` file in the root of the project by copying the `example.env` file. Populate it with your credentials for external services like Razorpay and Twilio.
2. Build the Project: Build all the service modules to create the necessary JAR files.
   ``` bash
   ./gradlew clean build
   ```
3. Run with Docker Compose: Launch all the services and backing infrastructure (databases, Kafka, etc.) using Docker Compose.
   ```bash
   docker-compose up -d
   ```
4. **Accessing Services:**
    * **API Gateway:** `http://localhost:8080`
    * **Swagger UI:** `http://localhost:8080/swagger-ui.html`
    * Individual services can also be accessed on their respective ports if needed.
# 🔮 Project Status & Future Roadmap

This project is a functional and robust platform, but it also serves as a foundation for future enhancements.

## Current Status

* ✅ All core services are functional and integrated.
* ✅ End-to-end flows for user registration, product creation, ordering, and payment are implemented.
* ✅ Event-driven communication for key business processes is in place.

## Future Enhancements

* ✨ **Implement Config Server**: Centralize all configuration using Spring Cloud Config for better management.
* ✨ **Expand Test Coverage**: Increase unit, integration, and end-to-end test coverage across all services.
* ✨ **CI/CD Pipeline**: Set up a full continuous integration and deployment pipeline using GitHub Actions.
* ✨ **Frontend Application**: Develop a React or Vue.js client to consume the backend APIs.
* ✨ **Observability**: Integrate distributed tracing (e.g., OpenTelemetry) and centralized logging (ELK Stack) for better monitoring.
