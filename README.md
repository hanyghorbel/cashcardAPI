# CashCard API

A RESTful API built with Spring Boot for managing personal Cash Cards, based on the Spring Academy *Building a REST API with Spring Boot* course project using Test-Driven Development (TDD).

Each authenticated card owner can create, read, paginate/sort, update, and delete their own Cash Cards, with strict ownership isolation and input validation.

## Tech Stack

- **Java 21**
- **Spring Boot 4.1.2** (Gradle)
  - Spring Web MVC
  - Spring Data JPA
  - Spring Security (HTTP Basic + role-based access control)
  - Bean Validation (`jakarta.validation`)
- **H2 Database** (in-memory: `jdbc:h2:mem:cashcarddb`)
- **Lombok**
- **JUnit 5 / TestRestTemplate / JacksonTester** for integration and JSON tests

## Prerequisites

- **JDK 21** installed and configured (Gradle toolchain targets Java 21)
- No external database setup required (uses an embedded H2 in-memory database)

## Getting Started

### Run the Application

On Linux/macOS:
```bash
./gradlew bootRun
```

On Windows (PowerShell / CMD):
```powershell
.\gradlew.bat bootRun
```

The application starts on `http://localhost:8080` by default.

### Run the Tests

On Linux/macOS:
```bash
./gradlew test
```

On Windows (PowerShell / CMD):
```powershell
.\gradlew.bat test
```

Test suites include:
- `CashcardApplicationTests` — End-to-end integration tests for CRUD, pagination/sorting, authentication, authorization, and ownership enforcement (seeded via `src/test/resources/data.sql`).
- `CashCardDtoJsonTest` — JSON serialization and deserialization slice tests.

## Authentication & Authorization

All `/cashcard/**` endpoints require **HTTP Basic Authentication** and the `CARD-OWNER` role. Requests are scoped to the authenticated user (`Principal`), so users cannot view, update, or delete cards belonging to someone else (attempting to access another user's card returns `404 Not Found`).

The following in-memory users are configured in `SecurityConfig`:

| Username | Password | Role | Access to `/cashcard/**` |
| --- | --- | --- | --- |
| `sarah` | `abc123` | `CARD-OWNER` | Allowed (own cards only) |
| `vishal` | `123` | `CARD-OWNER` | Allowed (own cards only) |
| `keith` | `magtop` | `NON-OWNER` | `403 Forbidden` |

> **Note:** CSRF protection is disabled and users are stored in memory for development/testing purposes.

## API Reference

Base path: `/cashcard`

### Data Model (`CashCardDto`)

```json
{
  "id": 22,
  "balance": 23.6,
  "owner": "sarah"
}
```

- `id` (`Long`): Auto-generated identifier.
- `balance` (`Double`): Required (`@NotNull`), must be strictly positive (`@Positive`).
- `owner` (`String`): Automatically derived from the authenticated user on creation.

### Endpoints

| Method | Endpoint | Description | Success Status |
| --- | --- | --- | --- |
| `GET` | `/cashcard` | List authenticated user's cards (paginated & sorted) | `200 OK` |
| `GET` | `/cashcard/{id}` | Get a specific card by ID | `200 OK` |
| `POST` | `/cashcard` | Create a new card (sets `Location` header) | `201 Created` |
| `PUT` | `/cashcard/{id}` | Update the balance of an existing card | `200 OK` |
| `DELETE` | `/cashcard/{id}` | Delete an existing card | `204 No Content` |

#### Pagination & Sorting (`GET /cashcard`)

Supports standard Spring Data query parameters: `page`, `size`, and `sort`.
- If no `sort` parameter is provided, results default to sorting by `balance` in descending order (`balance,desc`).

### Example Requests (`curl`)

**Create a Cash Card:**
```bash
curl -i -u sarah:abc123 \
  -H "Content-Type: application/json" \
  -d '{"balance": 125.50}' \
  http://localhost:8080/cashcard
```

**Get all Cash Cards (paginated & sorted):**
```bash
curl -u sarah:abc123 "http://localhost:8080/cashcard?page=0&size=5&sort=balance,desc"
```

**Get a single Cash Card:**
```bash
curl -u sarah:abc123 http://localhost:8080/cashcard/1
```

**Update a Cash Card's balance:**
```bash
curl -X PUT -u sarah:abc123 \
  -H "Content-Type: application/json" \
  -d '{"balance": 200.00}' \
  http://localhost:8080/cashcard/1
```

**Delete a Cash Card:**
```bash
curl -X DELETE -u sarah:abc123 http://localhost:8080/cashcard/1
```

### Error Responses

Handled globally by `ApiExceptionHandler`:

- **`404 Not Found`** (missing card or owned by another user):
  ```json
  {
    "message": "CashCard not found with id: 1000"
  }
  ```
- **`400 Bad Request`** (validation failure or malformed JSON):
  ```json
  {
    "error": "Validation Failed",
    "status": 400,
    "details": {
      "balance": "double must be positive"
    }
  }
  ```

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   ├── com/cashcard/
│   │   │   ├── ApiExceptionHandler.java   # Global @RestControllerAdvice error handling
│   │   │   ├── CashcardApplication.java   # Spring Boot entry point
│   │   │   ├── CashCardController.java    # REST endpoints (/cashcard)
│   │   │   ├── CashCardDto.java           # Request/response record with validation
│   │   │   ├── CashCardEntity.java        # JPA entity mapped to CASH_CARD
│   │   │   ├── CashCardRepository.java    # Spring Data JPA repository
│   │   │   ├── CashCardService.java       # Business logic, pagination & ownership rules
│   │   │   ├── ErrorResponse.java         # Validation/malformed JSON error payload
│   │   │   └── SecurityConfig.java        # HTTP Basic auth, RBAC & in-memory users
│   │   └── exceptions/
│   │       └── CashCardNotFoundExcpetion.java
│   └── resources/
│       └── application.yaml               # H2 & JPA configuration
└── test/
    ├── java/com/cashcard/
    │   ├── CashcardApplicationTests.java  # Integration tests
    │   └── CashCardDtoJsonTest.java       # JSON serialization tests
    └── resources/
        ├── data.sql                       # Seed data for tests
        └── com/cashcard/
            ├── single.json
            └── list.json
```
