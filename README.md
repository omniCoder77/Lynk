# Lynk | Reactive Communication Platform

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple?style=flat-square&logo=kotlin)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?style=flat-square&logo=spring)
![Security](https://img.shields.io/badge/Security-mTLS%20%7C%20SCRAM-red?style=flat-square&logo=lock)
![Architecture](https://img.shields.io/badge/Architecture-Reactive%20Microservices-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)

**Lynk** is a high-performance, distributed real-time communication platform engineered on the reactive principles of **Spring WebFlux**. Designed for massive concurrency and low-latency throughput, it leverages a non-blocking event-loop architecture to handle thousands of concurrent connections with minimal resource overhead.

The system employs an event-driven architecture using **Apache Kafka** for asynchronous decoupling, **Cassandra** for write-heavy chat logs, and **Redis** for high-speed caching.

> **Security Notice:** Lynk implements a Zero-Trust security model with end-to-end mTLS, strict SCRAM authentication, and rigorous network segmentation between public-facing services and private data infrastructure.

---

## 🏗 High-Level Architecture

The platform follows a domain-driven microservices architecture hosted within a VPC-like environment. The network is segmented into **Public/Private Bridge** subnets for microservices and strict **Private Subnets** for the data layer, ensuring databases and message brokers are never exposed to the public internet.

```mermaid
flowchart LR
linkStyle default interpolation basis,stroke-width:2px,fill:none,stroke:#B0B0B0

subgraph UserZone [User Interaction]
direction TB
CLIENT([<br/>fa:fa-user<br/><b>User / Client</b><br/>])
end

subgraph CloudVPC [Secure VPC Infrastructure]
direction LR

subgraph AppLayer [Public/Private Bridge Subnet]
direction TB
S_AUTH(fa:fa-shield-alt <b>Auth Service</b><br/>Spring WebFlux)
S_MSG(fa:fa-comments <b>Message Service</b><br/>Spring WebFlux)
S_NOTIF(fa:fa-bell <b>Notification Service</b><br/>Spring WebFlux)
S_MEDIA(fa:fa-images <b>Media Service</b><br/>Spring WebFlux)
end

subgraph DataLayer [Private Subnet]
direction TB
M_KAFKA{{fa:fa-random <b>Kafka Cluster</b><br/>SCRAM-512 / mTLS}}
D_REDIS[(fa:fa-memory <b>Redis Stack</b><br/>SCRAM-256 / mTLS)]
D_PG[(fa:fa-database <b>PostgreSQL</b><br/>SCRAM-256 / mTLS)]
D_CASS[(fa:fa-layer-group <b>Cassandra</b><br/>SCRAM-256 / mTLS)]
end
end

subgraph External [External Providers]
direction TB
E_TWILIO>fa:fa-mobile-alt <b>Twilio</b>]
E_FCM>fa:fa-paper-plane <b>FCM</b>]
E_S3>fa:fa-cloud <b>AWS S3</b>]
end

%% Client Interactions
CLIENT -.-o|1. HTTPS/WSS| S_AUTH
CLIENT <===>|2. Real-time WS| S_MSG
CLIENT ===|3. Upload/Download| S_MEDIA

%% Internal Auth Flows
S_AUTH ==>|mTLS| D_PG
S_AUTH ==>|mTLS| D_REDIS
S_AUTH -.->|mTLS + SCRAM| M_KAFKA

%% Message Flows
S_MSG ==>|mTLS| D_CASS
S_MSG ==>|mTLS| D_REDIS
S_MSG -.->|mTLS + SCRAM| M_KAFKA

%% Notification Flows
M_KAFKA -.->|Consume| S_NOTIF
S_NOTIF -->|mTLS| D_PG
S_NOTIF -->|HTTPS| E_FCM

%% External
S_AUTH --> E_TWILIO
S_MEDIA --> E_S3

classDef client fill:#FF6B6B,stroke:#c92a2a,stroke-width:2px,color:white;
classDef service fill:#4D96FF,stroke:#0055AA,stroke-width:2px,color:white;
classDef db fill:#FFD93D,stroke:#d4a017,stroke-width:2px,color:#333;
classDef bus fill:#6BCB77,stroke:#2e8b57,stroke-width:2px,color:white;
classDef ext fill:#E0E0E0,stroke:#9E9E9E,stroke-width:2px,stroke-dasharray: 5 5,color:#333;
classDef vpc fill:#F4F9F9,stroke:#607D8B,stroke-width:2px,stroke-dasharray: 5 5;
classDef private fill:#ECEFF1,stroke:#CFD8DC,stroke-width:2px;

class CLIENT client;
class S_AUTH,S_MSG,S_NOTIF,S_MEDIA service;
class D_PG,D_CASS,D_REDIS db;
class M_KAFKA bus;
class E_TWILIO,E_FCM,E_S3 ext;
class CloudVPC vpc;
class DataLayer private;
```

---

## 🔒 Security & Infrastructure

Lynk goes beyond standard security practices by implementing a hardened, defense-in-depth infrastructure strategy.

### 1. Network Segmentation (VPC)
*   **Private Subnet:** All persistence layers (Postgres, Cassandra, Redis) and the Event Bus (Kafka) reside in a strictly isolated private subnet. They are inaccessible from the public internet.
*   **Public/Private Bridge:** Microservices act as the gatekeepers. They are dual-homed: accessible via public load balancers for client traffic, but connected internally to the private subnet to access data.

### 2. Mutual TLS (mTLS) Encryption
Every internal connection requires mutual authentication. It is not enough to simply trust the network; services must cryptographically prove their identity to the databases.
*   **Implemented on:** Redis, Cassandra, PostgreSQL, and Kafka.
*   **Mechanism:** Self-signed CA governance with individual certificate generation for every service instance.

### 3. Advanced Authentication (SCRAM)
We strictly avoid cleartext passwords, utilizing Salted Challenge Response Authentication Mechanisms (SCRAM) for all connections.
*   **Kafka:** Implements **SCRAM-SHA-512** for the highest level of cryptographic strength on the event bus.
*   **Databases:** Redis, PostgreSQL, and Cassandra utilize **SCRAM-SHA-256**.

### 4. Granular Authorization (ACLs)
Implementation of the "Principle of The Least Privilege":
*   **Kafka ACLs:** Topic-level authorization is enforced.
    *   *Example:* The `notification-service` has `READ` permission on the `message.created` topic but cannot `WRITE` to it. The `auth-service` cannot access chat logs.
*   **Database RBAC:** Service users are restricted to specific tables and operations (SELECT/INSERT) relevant only to their domain.

---

## ⚡ Key Design Decisions

*   **Reactive Model (Project Reactor):** Chosen over the traditional thread-per-request model to handle high I/O wait times (DB, Network) without blocking threads, maximizing hardware utilization.
*   **Cassandra for Chat Logs:** Utilizes a Wide-Column Store for its high write throughput and ability to model time-series data (chat history) efficiently.
*   **Probabilistic Data Structures:** Implements **Redis Cuckoo Filters** for room name availability checks ($O(1)$ space-efficient lookups).
*   **Zero-Trust Architecture:** The decision to implement mTLS and SCRAM across all internal components ensures that a perimeter breach does not lead to lateral movement or data compromise.

---

## 🛠 Service Breakdown

### 🛡 Auth Service (`auth-service`)
*   **Role:** Identity Gateway.
*   **Security:** Handles JWT issuance and rotation. Connects to Postgres via mTLS/SCRAM-256.
*   **MFA:** Twilio SMS OTP and TOTP.

### 💬 Message Service (`message-service`)
*   **Role:** WebSocket Engine.
*   **Persistence:** Writes chat logs to Cassandra (mTLS secured).
*   **Events:** Publishes `message.received` events to Kafka (SCRAM-512 secured).

### 🔔 Notification Service (`notification-service`)
*   **Role:** Async Consumer.
*   **Access:** Read-only ACL access to Kafka topics.

### 📂 Media Service (`media-service`)
*   **Role:** Binary Management.
*   **Storage:** AWS S3 (via LocalStack).

---

## 🚀 Getting Started

### Prerequisites
*   **JDK 17+**
*   **Docker & Docker Compose** (v2.0+)
*   **OpenSSL** (Required for mTLS cert generation)

### 1. Repository Setup
```bash
git clone https://github.com/omniCoder77/Lynk.git
cd Lynk
```

### 2. Infrastructure Security Setup (Crucial)
Because Lynk runs with full security enabled, you **must** generate the mTLS certificates and credentials before starting the containers.

**Generate Root CA and Service Certificates:**
```bash
chmod +x init/*.sh
./init/generate_ca..sh
./init/generate_jwt..sh
```

**Generate Component-Specific Certs:**
```bash
./init/generate_kafka_certs.sh
./init/generate_postgres_certs.sh
./init/generate_redis_certs.sh
./init/generate_cassandra_certs.sh
```

### 3. Database Initialization
Start the persistence layer first.

```bash
docker compose up -d cassandra postgres kafka
```

**Initialize Cassandra Schema:**
```bash
docker exec -it cassandra cqlsh -f /init/init-cassandra.cql
```

### 4. Apply Security Configurations (ACLs)
Once Kafka is running, apply the Access Control Lists (ACLs) and SCRAM credentials. This script sets up the specific user permissions (e.g., *MessageService* can write to *ChatTopic*, *NotificationService* can only read).

```bash
./init/create-kafka-users.sh
```

### 5. Launch Microservices
```bash
docker compose -f docker-compose.prod.yml up -d
```

---

## 🤝 Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.


---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.