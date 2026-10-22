# Library Management System

A backend application for running a library: managing the collection and its physical copies, customers, borrowing and returning, fines, and a reservation queue. Built as a **modular monolith** with Spring Boot and protected by role-based JWT authentication (ADMIN, LIBRARIAN, CUSTOMER).

## Table of Contents

- [Project Overview](#project-overview)
- [Technologies Used](#technologies-used)
- [How to Run the Project](#how-to-run-the-project)
- [Database Configuration](#database-configuration)
- [Environment Variables](#environment-variables)
- [How to Run the Tests](#how-to-run-the-tests)
- [Swagger URL](#swagger-url)
- [Authentication Process](#authentication-process)
- [Main Business Rules](#main-business-rules)
- [Example API Requests](#example-api-requests)
- [Assumptions](#assumptions)

## Project Overview

The focus is on real workflows rather than plain CRUD: borrowing, returning, fines and reservations are transactional and follow explicit business rules.

**Features**

- Collection management: departments, authors, categories, books and physical copies, each copy with its own status
- Book search with filters (title, author, ISBN, category, department, language, availability) and paging
- Borrowing and returning with automatic fine calculation
- Reservation queue with a 48-hour hold and automatic hand-off to the next customer
- Fine payment, and a borrowing block when unpaid fines exceed 500 EGP
- Admin customer management (activate / suspend) and reports
- Role-based access with stateless JWT
- Optimistic locking so two customers cannot borrow the same copy at once

**Project structure** (package by feature)

```
com.abz.library
├── config/        Clock bean, configuration properties, dev account seeding
├── security/      JWT service and filter, Spring Security setup
├── exception/     Error response, custom exceptions, global handler
├── auth/          Register and login
├── user/          Users and customer management
├── collection/    Departments, authors, categories, books, copies, search
├── borrowing/     Borrow, return, lost, history
├── fine/          Fines and payment
├── reservation/   Reservation queue
└── report/        Admin reports
```

`borrowing`, `fine` and `reservation` depend on `collection` and `user`, never the reverse. When a copy becomes available, `collection` publishes a `CopyAvailableEvent` that `reservation` listens to.

## Technologies Used

| Area              | Choice                                     |
| ----------------- | ------------------------------------------ | --------- |
| Language / build  | Java 21, Maven                             |
| Framework         | Spring Boot, Spring Web                    |
| Persistence       | Spring Data JPA, Hibernate, PostgreSQL     |
| Schema management | Flyway                                     |
| Security          | Spring Security, JWT (JJWT, HS256), BCrypt |
| Validation        | Bean Validation                            |
| Testing           | JUnit 5, Mockito, AssertJ                  |
| API docs          | springdoc OpenAPI (Swagger UI)             |
| Other             | Lombok                                     | MapStruct |

## How to Run the Project

**Prerequisites:** JDK 21, Maven 3.9+, PostgreSQL 14+

**1. Create the database**

```sql
CREATE DATABASE library_db;
```

Or with Docker:

```bash
docker run --name library-db -e POSTGRES_PASSWORD=change-me -e POSTGRES_DB=library_db -p 5432:5432 -d postgres:16
```

**2. Set the environment variables** (see [Environment Variables](#environment-variables))

```bash
# macOS / Linux / Git Bash
export DB_PASSWORD='change-me'
export JWT_SECRET="$(openssl rand -base64 32)"
```

```powershell
# Windows PowerShell
$env:DB_PASSWORD = 'change-me'
$b = New-Object byte[] 32; (New-Object Security.Cryptography.RNGCryptoServiceProvider).GetBytes($b)
$env:JWT_SECRET = [Convert]::ToBase64String($b)
```

**3. Start the application**

```bash
mvn spring-boot:run
```

On the first start, Flyway creates the schema and the application creates the development staff accounts. The API is available at `http://localhost:8080`.

**4. Log in with a seeded account** (development profile only)

| Role      | Email                    | Password        |
| --------- | ------------------------ | --------------- |
| ADMIN     | `admin@library.test`     | `Admintest`     |
| LIBRARIAN | `librarian@library.test` | `Librariantest` |

These credentials are for local development only. Customers register themselves with `POST /api/auth/register`.

## Database Configuration

| Property                        | Value                                         |
| ------------------------------- | --------------------------------------------- |
| `spring.datasource.url`         | `jdbc:postgresql://localhost:5432/library_db` |
| `spring.datasource.username`    | `${DB_USERNAME:postgres}`                     |
| `spring.datasource.password`    | `${DB_PASSWORD}`                              |
| `spring.jpa.hibernate.ddl-auto` | `validate`                                    |

The schema is managed by Flyway (`src/main/resources/db/migration`). Hibernate only validates that the entities match the schema. Never edit an applied migration: add a new one instead.

The database also enforces some rules as a safety net: unique emails, ISBNs and names (authors, departments, categories), one fine per borrowing, and valid status values. "One open borrowing per copy" and "one open reservation per customer and book" are enforced in the service layer only.

**Application properties**

| Property                                | Default                        | Meaning                                             |
| --------------------------------------- | ------------------------------ | --------------------------------------------------- |
| `jwt.expiration-minutes`                | `60`                           | Token lifetime                                      |
| `borrowing.period-days`                 | `14`                           | Days until the due date                             |
| `borrowing.max-borrow`                  | `5`                            | Active borrowings per customer                      |
| `borrowing.per-day-fine`                | `10`                           | Fine in EGP per overdue day                         |
| `borrowing.max-fines`                   | `500`                          | Unpaid fines (EGP) above which borrowing is blocked |
| `reservation.expiry-hours`              | `48`                           | Hours a READY reservation is held                   |
| `borrowing.overdue.cron` / `.timezone`  | `0 5 0 * * *` / `Africa/Cairo` | Nightly job that marks late borrowings OVERDUE      |
| `reservation.expiry.cron` / `.timezone` | `0 0 0 * * *` / `Africa/Cairo` | Nightly job that releases expired holds             |

## Environment Variables

| Variable                 | Required                    | Meaning                                         |
| ------------------------ | --------------------------- | ----------------------------------------------- |
| `DB_PASSWORD`            | No (default `postgres`)     | PostgreSQL password                             |
| `DB_USERNAME`            | No (default `postgres`)     | PostgreSQL user                                 |
| `JWT_SECRET`             | NO (default `fallback-key`) | Base64 signing key of at least 32 random bytes. |
| `SPRING_PROFILES_ACTIVE` | No (default `dev`)          | Active Spring profile                           |

Never commit these values. Set them in your shell or your IDE run configuration (Spring Boot does not read a `.env` file by itself).

## How to Run the Tests

```bash
mvn test                                  # all tests
mvn test -Dtest=BorrowingServiceTest      # borrowing tests only
```

The tests are unit tests: repositories and other services are mocked with Mockito, no database is used, and a fixed `Clock` makes due dates and fines exact.

| Test class             | Covers                                                                                                                                                                                          |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `BorrowingServiceTest` | Successful borrow, five active borrowings, unpaid fines above 500, suspended customer, no copy available, same book already borrowed, return without fine, overdue return with fine calculation |

## Swagger URL

With the application running:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

To call protected endpoints, log in through `POST /api/auth/login`, copy the `accessToken`, click **Authorize**, and paste the token (without the word `Bearer`).

A Postman collection covering every endpoint is in `postman/Library-API.postman_collection.json`.

## Authentication Process

1. **Register** a customer with `POST /api/auth/register`. Registration always creates a CUSTOMER, so nobody can register as staff.
2. **Log in** with `POST /api/auth/login` to receive a signed JWT (`accessToken`, valid for 60 minutes by default).
3. **Send the token** on every other request: `Authorization: Bearer <accessToken>`.

| URL pattern                             | Who can call it            |
| --------------------------------------- | -------------------------- |
| `/api/auth/**`                          | Everyone                   |
| `/api/admin/**`                         | ADMIN                      |
| `/api/librarian/**`                     | ADMIN and LIBRARIAN        |
| `/api/customer/**`                      | CUSTOMER only              |
| `GET /api/books`, `GET /api/books/{id}` | Everyone (no token needed) |

A missing, invalid or expired token returns **401**. A valid token with a role that is not allowed returns **403**. Passwords are hashed with BCrypt.

## Main Business Rules

| Area            | Rule                                                                                                                                                                                                            |
| --------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Borrowing limit | At most **5** active borrowings (ACTIVE or OVERDUE) per customer                                                                                                                                                |
| Same book       | A customer cannot borrow a book they already have an active borrowing for                                                                                                                                       |
| Fines block     | Unpaid fines **strictly above 500 EGP** block borrowing (exactly 500 is allowed)                                                                                                                                |
| Customer status | Suspended or blocked customers cannot borrow or reserve                                                                                                                                                         |
| Availability    | A book can be borrowed only if it has an AVAILABLE physical copy                                                                                                                                                |
| Loan period     | Due date is the borrowing date plus **14 days**                                                                                                                                                                 |
| Fine            | **10 EGP per overdue day**, counted in whole 24-hour periods after the due time. A book returned within 24 hours of the due time is not late. One fine per borrowing, created only when a late book is returned |
| Fine payment    | UNPAID becomes PAID. A paid fine cannot be paid again, and customers can pay only their own fines                                                                                                               |
| Reservations    | When every copy is unavailable, customers join a queue. The first in line gets the next free copy and has **48 hours** to borrow it, otherwise it goes to the next customer                                     |
| Transactions    | Borrowing, returning and every copy hand-off run in a single transaction                                                                                                                                        |
| Concurrency     | Copies use optimistic locking (`@Version`): the second of two simultaneous requests gets a 409                                                                                                                  |

## Example API Requests

`$ADMIN` and `$TOKEN` hold the `accessToken` of an admin and a customer.

**Register and log in**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ali","lastName":"Hassan","email":"ali@example.com","phone":"01012345678","password":"Secret123!","dateOfBirth":"1995-04-12"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ali@example.com","password":"Secret123!"}'
```

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600,
  "email": "ali@example.com",
  "role": "CUSTOMER"
}
```

**Set up the collection (admin)**

```bash
curl -X POST http://localhost:8080/api/admin/departments -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d '{"name":"IT","description":"Information technology"}'

curl -X POST http://localhost:8080/api/admin/authors -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d '{"name":"Robert C. Martin"}'

curl -X POST http://localhost:8080/api/admin/categories -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d '{"name":"Programming"}'

curl -X POST http://localhost:8080/api/admin/books -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"isbn":"978-0-13-235088-4","title":"Clean Code","publisher":"Prentice Hall",
       "publicationYear":2008,"language":"English","departmentId":1,"authorIds":[1],"categoryIds":[1]}'

curl -X POST http://localhost:8080/api/admin/book-copies -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d '{"bookId":1,"copyNumber":"001"}'
```

**Search books**

```bash
curl "http://localhost:8080/api/books?title=clean&department=IT&available=true&page=0&size=10&sort=title,asc" \
  -H "Authorization: Bearer $TOKEN"
```

**Borrow and return**

```bash
curl -X POST http://localhost:8080/api/customer/borrow -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"bookId":1}'
```

```json
{
  "id": 1,
  "customer": {
    "id": 3,
    "firstName": "Ali",
    "lastName": "Hassan",
    "email": "ali@example.com"
  },
  "bookCopy": {
    "id": 1,
    "copyNumber": "001",
    "bookId": 1,
    "bookTitle": "Clean Code",
    "isbn": "9780132350884"
  },
  "borrowedAt": "2026-10-13T09:00:00",
  "dueDate": "2026-10-27T09:00:00",
  "returnedAt": null,
  "status": "ACTIVE",
  "fineAmount": 0
}
```

```bash
curl -X POST http://localhost:8080/api/customer/borrowings/1/return -H "Authorization: Bearer $TOKEN"
```

A book returned 4 days late gets `"fineAmount": 40.00` and a fine is created.

**Pay a fine**

```bash
curl "http://localhost:8080/api/customer/fines?status=UNPAID" -H "Authorization: Bearer $TOKEN"
curl -X POST http://localhost:8080/api/customer/fines/1/pay -H "Authorization: Bearer $TOKEN"
```

**Reserve a book**

```bash
curl -X POST http://localhost:8080/api/customer/books/1/reserve -H "Authorization: Bearer $TOKEN"
```

```json
{
  "id": 1,
  "bookId": 1,
  "bookTitle": "Clean Code",
  "reservedAt": "2026-10-13T09:05:00",
  "status": "WAITING",
  "queuePosition": 1,
  "expiresAt": null
}
```

When a copy is returned, the first reservation becomes `READY` with `expiresAt` 48 hours ahead.

**Admin: customers and reports**

```bash
curl -X PUT http://localhost:8080/api/admin/customers/3/suspend -H "Authorization: Bearer $ADMIN"
curl http://localhost:8080/api/admin/reports/overview -H "Authorization: Bearer $ADMIN"
curl "http://localhost:8080/api/admin/reports/most-borrowed-books?limit=5" -H "Authorization: Bearer $ADMIN"
```

**Error format**

Every error has the same shape:

```json
{
  "timestamp": "2026-10-13T09:00:00",
  "status": 400,
  "message": "Customer already has the maximum number of active borrowings",
  "path": "/api/customer/borrow"
}
```

| Status | Meaning                                                                 |
| ------ | ----------------------------------------------------------------------- |
| 400    | Validation error, malformed request, or business rule violation         |
| 401    | Missing, invalid or expired token, or wrong credentials                 |
| 403    | Role not allowed, or the record belongs to another customer             |
| 404    | Record does not exist                                                   |
| 409    | Duplicate value, record still referenced, or concurrent update conflict |
| 500    | Unexpected error (details only in the server log)                       |

## Assumptions

The project requirements leaves several points open. These are the decisions made.

**Users and roles**

- Staff and customers share one `users` table. Staff accounts come from the development seeding or directly from the database.
- Suspended and blocked users can log in and browse. The restriction applies when they borrow or reserve.
- `/api/customer/**` is CUSTOMER only, so librarian endpoints were added: borrow for a customer, return any borrowing, and mark a borrowing lost.
- Suspending a customer does not cancel their existing borrowings or reservations.

**Collection**

- A book is a title. A copy is a physical item with its own status and a copy number unique within the book.
- ISBNs are stored without hyphens, and only the format is checked.
- A department, author or category linked to a book cannot be deleted, and neither can a book that still has copies.
- Copy status changes follow a strict state machine (for example LOST is terminal). BORROWED and RESERVED are set only by the borrowing and reservation workflows.

**Borrowing and fines**

- ACTIVE and OVERDUE both count as active borrowings.
- The copy given is the AVAILABLE one with the lowest copy number, or the held copy for a customer with a READY reservation.
- Lateness is counted in whole 24-hour periods after the due time (borrow time plus 14 days).
- A librarian can return a copy as damaged (`?damaged=true`) and mark a borrowing lost. A lost book creates no replacement-cost fine.
- Fines are paid in full. There is no payment gateway, so the status change is the payment.
- A fine is created only when a late book is returned, so a book that is never returned aquires no fine.

**Reservations**

- Only ACTIVE customers can reserve, and only when no copy is AVAILABLE. A book with no copies can be reserved.
- Whenever a copy becomes available, it is handed to the first waiting customer inside the same transaction.
- Expiry is checked on access (before borrow, reserve, cancel and list), and a nightly job at 00:00 (Africa/Cairo) also releases expired holds.

**Reports**

- `overdueBorrowings` counts borrowings with status OVERDUE. A nightly job at 00:05 (Africa/Cairo) marks late ACTIVE borrowings as OVERDUE, and the overview report runs the same check before counting.
