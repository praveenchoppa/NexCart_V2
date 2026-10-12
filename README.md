# NexCart 

**NexCart** is an evolving e-commerce backend built with Java and Spring Boot, designed to explore secure API development, transactional data consistency, concurrency control, and real-time communication.

The project follows a feature-oriented, layered architecture and focuses on the engineering challenges behind reliable backend systems.

##  Features

### Authentication and Security
- JWT-based authentication.
- Spring Security integration.
- Role-based access control.
- Protected REST endpoints.
- JWT authentication for WebSocket/STOMP connections.
- Conversation participant authorization for messaging subscriptions.

###  Product Management
- REST APIs for product-related operations.
- Layered architecture separating controllers, services, repositories, and entities.
- Database persistence using JPA/Hibernate.

### 🛒 Cart and Checkout
- Cart operations and order creation.
- Order items associated with their corresponding products.
- Inventory validation during checkout.
- Stock decrement during successful order processing.
- Cart clearing after successful checkout.

###  Transaction Management and Concurrency Control
- **Database transactions:** Use `@Transactional` to manage related checkout operations as a transactional unit.
- **Pessimistic locking:** Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` to acquire a database lock when retrieving products for protected stock updates.
- **Inventory consistency:** Validate available stock while holding the lock before decrementing inventory.
- **Race-condition handling:** Prevent concurrent checkout requests from both purchasing the same remaining stock.
- **Rollback support:** Allow failed transactional operations to roll back database changes according to transaction configuration and rollback rules.

###  Real-Time Messaging
- Real-time messaging using WebSocket and STOMP.
- WebSocket endpoint at `/ws`.
- Authenticated STOMP connections using JWT.
- Application message destination: `/app/chat.send`.
- Conversation-specific subscriptions using `/topic/conversations/{conversationId}`.
- Server-side authorization for conversation participants.
- Message persistence before broadcasting to subscribers.
- Request validation for non-empty message content, with a maximum length of 2,000 characters.

###  Automated Testing
- Unit and application-context tests using JUnit and Mockito.
- Controller and service tests.
- WebSocket message-handler tests.
- JWT authentication and STOMP authorization tests.
- A browser-based WebSocket tester for manual verification.

The latest recorded clean test run completed with **49 tests, 0 failures, 0 errors, and 0 skipped tests**.

##  Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Target Java version |
| Spring Boot 4.0.7 | Backend application framework |
| Spring Security | Authentication and access control |
| Spring Data JPA | Data access and persistence |
| Hibernate | ORM and database interaction |
| PostgreSQL | Development database |
| Maven | Build and dependency management |
| JWT | Authentication tokens |
| WebSocket | Full-duplex communication |
| STOMP | Messaging protocol over WebSocket |
| JUnit | Automated testing |
| Mockito | Mocking and unit testing |
| H2 | In-memory database for tests |

##  Architecture

NexCart follows a feature-oriented, layered backend architecture. Each feature separates responsibilities to keep business logic, API handling, and data access organized.

```text
backend/
└── src/
    ├── main/
    │   ├── java/
    │   │   └── ...
    │   └── resources/
    └── test/
        └── java/
```

Typical responsibilities within a feature:

- **Controller:** Receives requests and returns responses.
- **Service:** Implements business logic and coordinates operations.
- **Repository:** Performs database access.
- **Entity:** Represents persisted domain data.
- **DTO:** Defines the request and response data structures.

This structure makes features easier to maintain, test, and extend independently.

## How Transactional Checkout Works

One important engineering challenge in an e-commerce application is preventing overselling when multiple customers attempt to purchase the same limited inventory.

Consider a product with only one unit remaining. Two checkout requests arrive at nearly the same time.

Without appropriate concurrency control, both requests could read the same stock value before either updates it.

NexCart uses a transactional checkout flow with pessimistic locking to protect the inventory update.

```text
Checkout Request
       |
       v
Begin Transaction
       |
       v
Retrieve Product with Pessimistic Write Lock
       |
       v
Validate Available Stock
       |
       +---- Insufficient Stock ----> Reject Checkout
       |
       v
Create Order and Order Items
       |
       v
Decrement Inventory
       |
       v
Complete Remaining Checkout Operations
       |
       v
Commit Transaction
```

The lock coordinates competing transactions that attempt to modify the same locked product row. A competing checkout must wait for the lock to be released before proceeding, subject to database and transaction configuration.

### Why Pessimistic Locking?

Pessimistic locking is useful when concurrent operations may modify the same resource and conflicting updates must be controlled.

In inventory management, it helps ensure that stock validation and the corresponding update are performed under appropriate concurrency control.

**Important:** The exact locking behavior depends on the database, transaction boundaries, and error-handling implementation.

##  Real-Time Messaging Architecture

NexCart also supports real-time communication through WebSocket and STOMP.

```text
Client
  |
  | WebSocket Connection + JWT
  v
STOMP JWT Authentication
  |
  v
STOMP Authorization
  |
  v
Chat Message Handler
  |
  v
Message Service
  |
  v
Database Persistence
  |
  v
STOMP Topic Broadcast
  |
  v
Conversation Subscribers
```

### Messaging Destinations

| Purpose | Destination |
|---|---|
| WebSocket endpoint | `/ws` |
| Send a message | `/app/chat.send` |
| Subscribe to a conversation | `/topic/conversations/{conversationId}` |

The server authenticates STOMP connections, checks subscription permissions, and derives the sender's identity from the authenticated principal rather than trusting a client-supplied sender ID.

Messages are saved before being broadcast, so successful delivery through the messaging flow follows database persistence.

The messaging feature has been manually tested with two participants communicating through separate browser tabs.

##  Getting Started

### Prerequisites

- Git
- JDK 21
- PostgreSQL

The project targets Java 21. Using JDK 21 locally aligns the development environment with the configured Java target.

### 1. Clone the repository

```bash
git clone https://github.com/praveenchoppa/NexCart_V2.git
cd NexCart_V2
```

### 2. Configure PostgreSQL

Create a PostgreSQL database for the application.

Configure the database connection and required secrets using the property names expected by the backend's application configuration.

Before running the application, verify that:
- PostgreSQL is running.
- The database exists.
- The database credentials are correct.
- Required JWT or other application secrets are configured.

**Never commit real passwords, tokens, API keys, or production secrets to GitHub.**

### 3. Run the backend

From the repository root, run the following commands.

**Windows PowerShell**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**Linux or macOS**

```bash
cd backend
chmod +x mvnw
./mvnw spring-boot:run
```

##  Run the Tests

Run the tests from the `backend` directory.

**Windows PowerShell**

```powershell
.\mvnw.cmd clean test
```

**Linux or macOS**

```bash
./mvnw clean test
```

The latest recorded clean test execution passed all 49 tests without failures or errors.

The tests use an H2 in-memory database for the application test context, keeping the automated test setup separate from the local PostgreSQL development database.

##  Manual WebSocket Testing

A browser-based testing page is available at:

```text
backend/websocket-test/index.html
```

To test the messaging flow locally:

1. Start the backend.
2. Serve the HTML file using a local development server.
3. Obtain valid JWTs for two users who participate in the same conversation.
4. Open the tester in two browser tabs.
5. Connect both clients to the WebSocket endpoint.
6. Subscribe both clients to the same authorized conversation.
7. Send messages and verify that both participants receive the updates.

The tester is intended for development and manual verification, not as a production chat interface.

##  Security Considerations

- Authenticate requests using JWT where required.
- Enforce role-based authorization on protected endpoints.
- Verify conversation membership before allowing subscriptions.
- Derive the message sender from the authenticated principal.
- Validate incoming request data.
- Keep secrets outside source control.
- Review authorization rules whenever new features or message destinations are introduced.

##  Roadmap

NexCart is an ongoing engineering project. Future development can focus on:

- Expanding the e-commerce feature set.
- Strengthening checkout and inventory consistency.
- Improving automated concurrency and integration testing.
- Enhancing API documentation and error handling.
- Improving observability and operational reliability.
- Exploring scalable architecture and distributed systems concepts.

##  Author

**Praveen Choppa**

GitHub: [@praveenchoppa](https://github.com/praveenchoppa)

---

*Built with Java and Spring Boot, with a focus on secure backend engineering, transactional consistency, concurrency control, and real-time systems.*
