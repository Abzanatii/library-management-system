# Library Management System

A backend application for running a library: managing the catalogue and its physical copies, customers, borrowing and returning, fines, and a reservation queue. It is built as a **modular monolith** with Spring Boot, with role-based access (ADMIN, LIBRARIAN, CUSTOMER) protected by JWT.

The focus is on real workflows rather than plain CRUD: borrowing, returning, fines, and reservations are transactional and follow explicit business rules (see [Main business rules](#main-business-rules) and [Assumptions and design decisions](#assumptions-and-design-decisions)).

## Contents

1. [Technologies](#technologies)
2. [Project structure](#project-structure)
3. [How to run](#how-to-run)
4. [Configuration](#configuration)
5. [How to run the tests](#how-to-run-the-tests)
6. [API documentation (Swagger)](#api-documentation-swagger)
7. [Authentication](#authentication)
8. [Endpoints](#endpoints)
9. [Main business rules](#main-business-rules)
10. [Example requests](#example-requests)
11. [Assumptions and design decisions](#assumptions-and-design-decisions)
12. [Limitations and not implemented](#limitations-and-not-implemented)

---

## Technologies

| Area | Choice |
|---|---|
| Language / build | Java 21, Maven |
| Framework | Spring Boot (exact version in `pom.xml`), Spring Web |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL |
| Schema management | Flyway migrations (`src/main/resources/db/migration`), Hibernate runs with `ddl-auto=validate` |
| Security | Spring Security, stateless JWT (JJWT, HS256), BCrypt password hashing |
| Validation | Bean Validation (`@NotBlank`, `@Email`, `@Size`, ...) |
| Boilerplate | Lombok |
| Testing | JUnit 5, Mockito, AssertJ |
| API docs | springdoc OpenAPI (Swagger UI) |

## Project structure

Package by feature. Each module holds its own `controller`, `service`, `repository`, `entity`, `dto` and `mapper` layers as needed.

```
com.abz.library
├── config/        Clock bean, configuration-property registration, dev account seeding
├── security/      JWT service and filter, Spring Security setup, 401 / 403 handlers
├── exception/     ErrorResponse, custom exceptions, @RestControllerAdvice
├── dto/           PageResponse (shared paging response)
├── util/          Shared helpers for LIKE filters and safe sorting
├── auth/          POST /api/auth/register and /login
├── user/          Users and customer management (admin)
├── collection/    Departments, authors, categories, books, physical copies, book search
├── borrowing/     Borrow, return, lost, borrowing history
├── fine/          Fines and fine payment
├── reservation/   Reservation queue
└── report/        Admin reports (no entities of its own)
```

Dependencies point one way: `borrowing`, `fine` and `reservation` use `collection` and `user`, never the reverse. When a copy becomes available, `collection` publishes a `CopyAvailableEvent` that `reservation` listens to, so `collection` does not depend on `reservation`. Controllers contain no business logic: they read the request, pass the logged-in user's id to a service, and return a DTO. Entities are never exposed.

## How to run

**Prerequisites:** JDK 21, Maven 3.9 or newer, and a PostgreSQL server (14 or newer).

1. **Create the database.** Locally installed PostgreSQL:

   ```sql
   CREATE DATABASE library_db;
   ```

   Or with Docker:

   ```bash
   docker run --name library-db -e POSTGRES_PASSWORD=change-me -e POSTGRES_DB=library_db -p 5432:5432 -d postgres:16
   ```

2. **Set the environment variables** (see [Environment variables](#environment-variables)). The two required ones are `DB_PASSWORD` and `JWT_SECRET`.

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

3. **Start the application:**

   ```bash
   mvn spring-boot:run
   ```

   On the first start Flyway creates the schema, and the application creates the development staff accounts (below). The API is then available at `http://localhost:8080`.

4. **Log in as a seeded account** to get started (development profile only):

   | Role | Email | Password |
   |---|---|---|
   | ADMIN | `admin@library.local` | `Admin@12345` |
   | LIBRARIAN | `librarian@library.local` | `Librarian@12345` |

   These credentials are for local development only. They come from the `app.seed.*` properties, which must be left empty in any real deployment. Customers register themselves with `POST /api/auth/register`.

## Configuration

### Database

| Property | Value |
|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/library_db` |
| `spring.datasource.username` | `${DB_USERNAME:postgres}` |
| `spring.datasource.password` | `${DB_PASSWORD}` |
| `spring.jpa.hibernate.ddl-auto` | `validate` |

The schema is owned by Flyway:

- `V1__create_schema.sql` creates all 11 tables, their foreign keys, check constraints and indexes.
- `V2__reservation_expiry_index.sql` adds an index that speeds up the reservation expiry check.

Hibernate only *validates* that the entities match the schema, so a mismatch stops the application at startup with a clear message. Never edit a migration that has already been applied: add a new `V3__...` instead.

Besides the plain unique constraints (`users.email`, `books.isbn`, `(book_id, copy_number)`, `fines.borrowing_id`), the schema enforces these rules in the database itself, as a safety net behind the service checks:

- department and category names are unique ignoring case;
- a physical copy can be on loan only once at a time (one open borrowing per copy);
- a customer can have only one open (WAITING or READY) reservation per book;
- status columns accept only the values of their enum.

### Environment variables

| Variable | Required | Meaning |
|---|---|---|
| `DB_PASSWORD` | yes | PostgreSQL password |
| `DB_USERNAME` | no (default `postgres`) | PostgreSQL user |
| `JWT_SECRET` | yes | Signing key: a Base64 string of **at least 32 random bytes**. Generate with `openssl rand -base64 32`. The application refuses to start if it is missing, not valid Base64, or too short. |
| `SPRING_PROFILES_ACTIVE` | no | Defaults to `dev` |

Never commit these values. Keep a local `.env` file out of Git, and share only a `.env.example` with empty values.

### Application properties

| Property | Default | Meaning |
|---|---|---|
| `app.jwt.secret` | `${JWT_SECRET}` | Signing key |
| `app.jwt.expiration-minutes` | `60` | Token lifetime |
| `app.borrowing.period-days` | `14` | Days until the due date |
| `app.borrowing.max-active` | `5` | Active borrowings allowed per customer |
| `app.borrowing.fine-per-day` | `10` | Fine in EGP per overdue day |
| `app.borrowing.fine-block-threshold` | `500` | Unpaid fines (EGP) above which borrowing is blocked |
| `app.reservation.hold-hours` | `48` | Hours a READY reservation is held |
| `app.seed.admin.email` / `.password` | empty | Creates an ADMIN on startup if both are set (development only) |
| `app.seed.librarian.email` / `.password` | empty | Creates a LIBRARIAN on startup if both are set (development only) |
| `spring.data.web.pageable.default-page-size` | `10` | Page size when `size` is not sent |
| `spring.data.web.pageable.max-page-size` | `100` | Largest page size a client can request |

The borrowing and reservation values have the assignment's numbers built in as defaults, so the application works even if the section is absent.

## How to run the tests

```bash
mvn test                                  # everything
mvn test -Dtest=BorrowingServiceTest      # only the borrowing tests
```

The tests are unit tests: repositories and other services are mocked with Mockito, and no database is used. A fixed `Clock` makes due dates and fines exact.

| Test class | Covers |
|---|---|
| `BorrowingServiceTest` | The 8 scenarios required by the assignment, numbered in the test names: **1** successful borrow, **2** five active borrowings, **3** unpaid fines above 500, **4** suspended customer, **5** no copy available, **6** same book already borrowed, **7** successful return, **8** overdue return with the fine calculation. Also boundaries (4 active borrowings allowed, exactly 500 EGP allowed), blocked customer, not-found cases, ownership, damaged and lost returns, and borrowing a held reservation copy. |
| `ReservationServiceTest` | Reserving and its rules, the queue hand-off with the 48-hour hold, expiry, completion, and cancelling |
| `ReportServiceTest` | Overview assembly, the overdue cutoff, and the most-borrowed limits |

## API documentation (Swagger)

With the application running:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

To call protected endpoints from Swagger UI, log in through `POST /api/auth/login`, copy the `accessToken`, click **Authorize**, and paste the token (without the word `Bearer`).

### Postman collection

`postman/Library-Management-System.postman_collection.json` contains every endpoint, grouped in 10 folders, with tests on the status code and the main fields of each response (101 requests).

1. Start the application with the `dev` profile and an empty database, so the seeded admin and librarian exist.
2. Import the collection into Postman. Its variables already hold `baseUrl` (`http://localhost:8080`) and the seeded credentials.
3. Run the folders in order with the Collection Runner. Folder `0. Authentication` logs in and stores the tokens that every later request uses.
4. Names, ISBNs and emails get a fresh run id, so the collection can be run again on the same database.

The folders follow the workflows: catalogue setup, book search, customer management, borrowing, reserving and returning (including the hand-off to the next customer in the queue), cancelling a reservation, librarian actions (damaged, borrowing for a customer, lost), fines, reports, and a folder of expected failures (401, 403, 400, 404, 409).

A fine is created only by a late return. To see one, run the requests of folder `7. Fines` one by one and, after the borrowing request, run this SQL once (the borrowing id is in the `borrowingId` variable):

```sql
UPDATE borrowings SET due_date = due_date - interval '4 days' WHERE id = <borrowingId>;
```

Returning that borrowing then creates a 40 EGP fine (4 days x 10 EGP), which the next requests list and pay.

## Authentication

1. **Register** a customer with `POST /api/auth/register`. Registration always creates a CUSTOMER, so nobody can register as staff. Staff accounts are created by the startup seeding (development) or directly in the database.
2. **Log in** with `POST /api/auth/login`. The response contains a signed JWT (`accessToken`, valid for `app.jwt.expiration-minutes`, default 60).
3. **Send the token** on every other request: `Authorization: Bearer <accessToken>`.

Access rules:

| URL pattern | Who can call it |
|---|---|
| `/api/auth/**` | Everyone |
| `/api/admin/**` | ADMIN |
| `/api/librarian/**` | ADMIN and LIBRARIAN |
| `/api/customer/**` | CUSTOMER only (an admin or librarian cannot call customer endpoints) |
| `GET /api/books`, `GET /api/books/{id}` | Any logged-in user |

A missing, invalid or expired token returns **401**. A valid token whose role is not allowed returns **403**. Both use the same JSON error format as every other error.

## Endpoints

Generated from the controllers, one row per endpoint. Everything except `/api/auth/**` needs `Authorization: Bearer <token>`.

### Authentication

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/auth/login` | public | Log in |
| POST | `/api/auth/register` | public | Register a customer |

### Departments

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/admin/departments` | ADMIN | List departments |
| POST | `/api/admin/departments` | ADMIN | Create a department |
| GET | `/api/admin/departments/{id}` | ADMIN | Get a department |
| PUT | `/api/admin/departments/{id}` | ADMIN | Update a department |
| DELETE | `/api/admin/departments/{id}` | ADMIN | Delete a department |

### Authors

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/admin/authors` | ADMIN | List authors |
| POST | `/api/admin/authors` | ADMIN | Create an author |
| GET | `/api/admin/authors/{id}` | ADMIN | Get an author |
| PUT | `/api/admin/authors/{id}` | ADMIN | Update an author |
| DELETE | `/api/admin/authors/{id}` | ADMIN | Delete an author |

### Categories

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/admin/categories` | ADMIN | List categories |
| POST | `/api/admin/categories` | ADMIN | Create a category |
| GET | `/api/admin/categories/{id}` | ADMIN | Get a category |
| PUT | `/api/admin/categories/{id}` | ADMIN | Update a category |
| DELETE | `/api/admin/categories/{id}` | ADMIN | Delete a category |

### Books

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/admin/books` | ADMIN | Create a book |
| PUT | `/api/admin/books/{id}` | ADMIN | Update a book |
| DELETE | `/api/admin/books/{id}` | ADMIN | Delete a book |

### Book copies

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/admin/book-copies` | ADMIN | List the copies of a book |
| POST | `/api/admin/book-copies` | ADMIN | Create a copy |
| GET | `/api/admin/book-copies/{id}` | ADMIN | Get a copy |
| PUT | `/api/admin/book-copies/{id}` | ADMIN | Change a copy number or status |

### Customers

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/admin/customers` | ADMIN | List customers |
| GET | `/api/admin/customers/{id}` | ADMIN | Get a customer |
| PUT | `/api/admin/customers/{id}/activate` | ADMIN | Activate a customer |
| PUT | `/api/admin/customers/{id}/suspend` | ADMIN | Suspend a customer |

### Reports

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/admin/reports/most-borrowed-books` | ADMIN | Most borrowed books |
| GET | `/api/admin/reports/overview` | ADMIN | Library overview |

### Book search

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/books` | any logged-in user | Search books |
| GET | `/api/books/{id}` | any logged-in user | Get a book |

### Librarian

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/librarian/borrow` | ADMIN, LIBRARIAN | Borrow a book for a customer |
| POST | `/api/librarian/borrowings/{id}/lost` | ADMIN, LIBRARIAN | Mark a borrowing lost |
| POST | `/api/librarian/borrowings/{id}/return` | ADMIN, LIBRARIAN | Return any borrowing |

### Borrowing (customer)

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/customer/borrow` | CUSTOMER | Borrow a book |
| GET | `/api/customer/borrowings` | CUSTOMER | List my borrowings |
| GET | `/api/customer/borrowings/{id}` | CUSTOMER | Get one of my borrowings |
| POST | `/api/customer/borrowings/{id}/return` | CUSTOMER | Return a book |

### Fines (customer)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/customer/fines` | CUSTOMER | List my fines |
| GET | `/api/customer/fines/{id}` | CUSTOMER | Get one of my fines |
| POST | `/api/customer/fines/{id}/pay` | CUSTOMER | Pay a fine |

### Reservations (customer)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/customer/reservations` | CUSTOMER | List my reservations |
| GET | `/api/customer/reservations/{id}` | CUSTOMER | Get one of my reservations |
| POST | `/api/customer/books/{bookId}/reserve` | CUSTOMER | Reserve a book |
| PUT | `/api/customer/reservations/{id}/cancel` | CUSTOMER | Cancel a reservation |

List endpoints that can grow (books, customers, borrowings, fines, reservations) are paged with `page`, `size` and `sort`. Page numbers start at 0. Query filters are described in Swagger UI and in the assumptions below.

## Main business rules

| Area | Rule |
|---|---|
| Borrowing limit | A customer can have at most **5** active borrowings (ACTIVE or OVERDUE). |
| Same book | A customer cannot borrow a book they already have an active borrowing for. |
| Fines block | A customer whose unpaid fines are **strictly above 500 EGP** cannot borrow. Exactly 500 is allowed. |
| Customer status | Suspended or blocked customers cannot borrow or reserve. |
| Availability | A book can be borrowed only if it has an AVAILABLE physical copy. |
| Loan period | The due date is the borrowing date plus **14 days**. |
| Fine | **10 EGP per overdue day**, counted in calendar days. A book returned on the due date, at any time of that day, is not late. One fine per borrowing, created only for a late return. |
| Fine payment | UNPAID becomes PAID with the payment time stored. A paid fine cannot be paid again, and a customer can pay only their own fines. |
| Copy status | Every copy has its own status, and only the transitions in the [table below](#copy-status-transitions) are allowed. |
| Reservations | When every copy is unavailable, customers join a queue. The first in line gets the next free copy and has **48 hours** to borrow it. If they do not, the reservation expires and the copy goes to the next customer. |
| Transactions | Borrowing, returning, and every copy hand-off run in a single transaction: if any step fails, nothing is saved. |
| Concurrency | Copies are protected by optimistic locking (`@Version`), so two customers cannot borrow the same physical copy at the same moment: the second request gets a 409. |

## Example requests

The examples use `curl` against `http://localhost:8080`. `$ADMIN` and `$TOKEN` hold the `accessToken` of an admin and a customer.

### Register and log in

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ali","lastName":"Hassan","email":"ali@example.com","phone":"01012345678","password":"Secret123!","dateOfBirth":"1995-04-12"}'
```

```json
{ "id": 3, "firstName": "Ali", "lastName": "Hassan", "email": "ali@example.com", "phone": "01012345678",
  "dateOfBirth": "1995-04-12", "role": "CUSTOMER", "status": "ACTIVE", "createdAt": "2026-10-13T09:00:00" }
```

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ali@example.com","password":"Secret123!"}'
```

```json
{ "accessToken": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer", "expiresInSeconds": 3600,
  "email": "ali@example.com", "role": "CUSTOMER" }
```

### Set up the catalogue (admin)

```bash
curl -X POST http://localhost:8080/api/admin/departments -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d '{"name":"IT","description":"Information technology"}'

curl -X POST http://localhost:8080/api/admin/authors -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d '{"name":"Robert C. Martin"}'

curl -X POST http://localhost:8080/api/admin/categories -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d '{"name":"Programming"}'

curl -X POST http://localhost:8080/api/admin/books -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"isbn":"978-0-13-235088-4","title":"Clean Code","description":"A handbook of agile software craftsmanship",
       "publisher":"Prentice Hall","publicationYear":2008,"language":"English",
       "departmentId":1,"authorIds":[1],"categoryIds":[1]}'

curl -X POST http://localhost:8080/api/admin/book-copies -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d '{"bookId":1,"copyNumber":"001"}'
```

The ISBN is stored without hyphens (`9780132350884`) and the copy number in upper case.

### Search books

```bash
curl "http://localhost:8080/api/books?title=clean&department=IT&available=true&page=0&size=10&sort=title,asc" \
  -H "Authorization: Bearer $TOKEN"
```

```json
{ "content": [ { "id": 1, "isbn": "9780132350884", "title": "Clean Code", "language": "English",
                 "department": { "id": 1, "name": "IT", "description": "Information technology" },
                 "authors": [ { "id": 1, "name": "Robert C. Martin" } ],
                 "categories": [ { "id": 1, "name": "Programming" } ] } ],
  "page": 0, "size": 10, "totalElements": 1, "totalPages": 1 }
```

(The book fields `description`, `publisher`, `publicationYear`, `createdAt` and `updatedAt` are omitted here for brevity.)

### Borrow and return

```bash
curl -X POST http://localhost:8080/api/customer/borrow -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"bookId":1}'
```

```json
{ "id": 1,
  "customer": { "id": 3, "firstName": "Ali", "lastName": "Hassan", "email": "ali@example.com" },
  "bookCopy": { "id": 1, "copyNumber": "001", "bookId": 1, "bookTitle": "Clean Code", "isbn": "9780132350884" },
  "borrowedAt": "2026-10-13T09:00:00", "dueDate": "2026-10-27T09:00:00", "returnedAt": null,
  "status": "ACTIVE", "fineAmount": 0 }
```

```bash
curl -X POST http://localhost:8080/api/customer/borrowings/1/return -H "Authorization: Bearer $TOKEN"
```

A book returned 4 days late gets `"status": "RETURNED"` and `"fineAmount": 40.00`, and a fine is created:

```bash
curl http://localhost:8080/api/customer/fines?status=UNPAID -H "Authorization: Bearer $TOKEN"
curl -X POST http://localhost:8080/api/customer/fines/1/pay -H "Authorization: Bearer $TOKEN"
```

```json
{ "id": 1, "borrowingId": 1, "bookTitle": "Clean Code", "amount": 40.00, "status": "PAID",
  "createdAt": "2026-10-13T09:00:00", "paidAt": "2026-10-13T09:30:00" }
```

### Reserve a book

```bash
curl -X POST http://localhost:8080/api/customer/books/1/reserve -H "Authorization: Bearer $TOKEN"
```

```json
{ "id": 1, "bookId": 1, "bookTitle": "Clean Code", "reservedAt": "2026-10-13T09:05:00",
  "status": "WAITING", "queuePosition": 1, "expiresAt": null }
```

When a copy is returned, the first reservation becomes `"status": "READY"` with `expiresAt` 48 hours ahead. The customer then borrows the book as usual and the reservation becomes COMPLETED.

### Admin: customers and reports

```bash
curl "http://localhost:8080/api/admin/customers?status=ACTIVE&search=ali" -H "Authorization: Bearer $ADMIN"
curl -X PUT http://localhost:8080/api/admin/customers/3/suspend -H "Authorization: Bearer $ADMIN"
curl http://localhost:8080/api/admin/reports/overview -H "Authorization: Bearer $ADMIN"
curl "http://localhost:8080/api/admin/reports/most-borrowed-books?limit=5" -H "Authorization: Bearer $ADMIN"
```

```json
{ "totalBooks": 1200, "totalCopies": 3500, "availableCopies": 2100, "borrowedCopies": 1100,
  "damagedCopies": 200, "reservedCopies": 40, "lostCopies": 30, "maintenanceCopies": 30,
  "totalCustomers": 850, "activeBorrowings": 950, "overdueBorrowings": 120, "unpaidFines": 35000.00 }
```

### Errors

Every error has the same shape. Business rule violations return 400:

```json
{ "timestamp": "2026-10-13T09:00:00", "status": 400,
  "message": "Customer already has the maximum number of active borrowings",
  "path": "/api/customer/borrow" }
```

Validation errors add a `fieldErrors` list:

```json
{ "timestamp": "2026-10-13T09:00:00", "status": 400, "message": "Validation failed", "path": "/api/auth/register",
  "fieldErrors": [ { "field": "email", "message": "must be a well-formed email address" } ] }
```

| Status | Meaning |
|---|---|
| 400 | Validation error, malformed request, or a business rule violation |
| 401 | Missing, invalid or expired token, or wrong login credentials |
| 403 | The role is not allowed, or the record belongs to another customer |
| 404 | The record does not exist |
| 409 | Duplicate value, the record is still referenced, or two requests changed the same record at once |
| 500 | Unexpected error (generic message; details only in the server log) |

---

# Assumptions and design decisions

The assignment leaves several points open. This section records how each one was resolved.

## 1. Users, roles and access

| Topic | Decision |
|---|---|
| User storage | Staff and customers share one `users` table. `role` is ADMIN, LIBRARIAN or CUSTOMER, and `status` is ACTIVE, SUSPENDED or BLOCKED. Phone and date of birth are nullable at the database level because staff accounts may not have them. |
| Registration | Always creates a CUSTOMER. First name, last name, email, password (8 to 72 characters, 72 is BCrypt's limit) and date of birth (in the past) are required. Phone is optional. Emails are trimmed and stored in lower case. A duplicate email returns 409. |
| Staff accounts | Created by `StaffSeeder` from `app.seed.*` (development) or directly in the database. No endpoint creates staff. |
| Login and status | Suspended and blocked users can log in and browse. The restriction applies when they borrow or reserve. |
| Role access | As in [Authentication](#authentication). `/api/customer/**` is CUSTOMER only, as the assignment states, so admins and librarians cannot call it. |
| Librarian endpoints | The assignment gives librarians no endpoints, so these were added, sharing the services of the customer endpoints: borrow on behalf of a customer, return any borrowing (optionally damaged), and mark a borrowing lost. |
| Tokens | JWT with the email as subject and the role as a claim, valid for 60 minutes by default. The user is reloaded from the database on every request. There is no refresh token and no logout (a token stays valid until it expires). |

## 2. Customer management

- Admin customer endpoints see only users with the CUSTOMER role. The id of a staff user returns 404, so an admin cannot suspend themselves or a colleague through this API.
- `suspend` sets SUSPENDED (suspending an already suspended customer changes nothing). A BLOCKED customer cannot be suspended: the request returns 400 and asks to activate first.
- `activate` sets ACTIVE from SUSPENDED or BLOCKED (activating an active customer changes nothing).
- BLOCKED is reserved: the borrowing and reservation rules treat it like SUSPENDED, but no endpoint sets it.
- Suspending a customer does not cancel their existing borrowings or reservations.
- The customer list can be filtered by `status` and by a `search` text (first name, last name or email contains it, ignoring case). It defaults to newest first.

## 3. Catalogue

| Topic | Decision |
|---|---|
| Book and copy | A book is a title. A copy is a physical item with its own status and a copy number that is unique within its book. |
| Relationships | A book belongs to exactly one department and has many authors and many categories (join tables `book_authors` and `book_categories`, mapped with `@ManyToMany` using a `Set`, no cascade, owned by `Book`). |
| Book rules | A book needs a department and at least one author. Categories are optional. `PUT` replaces every field. |
| ISBN | Stored without spaces and hyphens, with a trailing `x` upper-cased. It must be 13 digits, or 10 characters (the last may be X). The checksum is not verified. A duplicate returns 409. |
| Names | Department and category names are unique ignoring case (409). Authors may share a name, because two different people can. |
| Deleting | A department, author or category that is still linked to a book cannot be deleted (409). A book that still has copies cannot be deleted (409). |
| References | An unknown department, author or category id in a book request returns 404. For authors and categories the message lists every missing id. |
| Extra reads | `GET` and `DELETE` for authors and categories and `GET` for copies go beyond the assignment, which lists only `POST` and `PUT`. They are needed to manage the data. |
| Copies | The copy number is trimmed and upper-cased (letters, digits and hyphens, up to 20 characters). A copy cannot be moved to another book. There is no delete: a lost copy is marked LOST. |

### Copy status transitions

The whole state machine lives in the `CopyStatus` enum and is enforced by the services. An invalid change returns 400, for example "Cannot change copy status from LOST to AVAILABLE".

| From | To | Triggered by |
|---|---|---|
| AVAILABLE | BORROWED | Borrow |
| AVAILABLE | RESERVED | The reservation queue claims a returned copy for the first waiting customer |
| AVAILABLE | MAINTENANCE, DAMAGED, LOST | Admin, through the copy `PUT` |
| BORROWED | AVAILABLE | Return |
| BORROWED | DAMAGED | Librarian return with `?damaged=true` |
| BORROWED | LOST | Librarian "mark lost" |
| RESERVED | BORROWED | The customer holding the READY reservation borrows it |
| RESERVED | AVAILABLE | Reservation expired or cancelled |
| MAINTENANCE | AVAILABLE, DAMAGED | Admin |
| DAMAGED | MAINTENANCE | Admin |
| LOST | none | Terminal state |

- BORROWED and RESERVED are managed by the borrowing and reservation workflows only. The admin copy `PUT` cannot set them or leave them, because they also have to update the borrowing or reservation record in the same transaction.
- A copy only becomes RESERVED from AVAILABLE. A returned copy that has a queue goes BORROWED to AVAILABLE to RESERVED within one transaction.
- Sending the current status again is accepted and changes nothing.

## 4. Book search

`GET /api/books` is open to every logged-in role. All filters are optional and combine with AND.

| Filter | Matching |
|---|---|
| `title` | Contains the text, ignoring case |
| `author` | Any author's name contains the text, ignoring case |
| `isbn` | Exact match, ignoring hyphens and spaces |
| `category`, `department`, `language` | Exact name, ignoring case |
| `available` | `true`: at least one AVAILABLE copy. `false`: no AVAILABLE copy, which includes books without copies |

`%` and `_` in search text are treated as ordinary characters. Sorting is limited to `title`, `isbn`, `publicationYear`, `language`, `createdAt` and `updatedAt` (anything else returns 400), defaults to title, and always ends with the id so pages are stable. The search runs three queries however many books are on the page, and results do not include copy counts.

## 5. Borrowing and returning

| Topic | Decision |
|---|---|
| Active borrowing | ACTIVE and OVERDUE both count as active: they count toward the limit of 5, trigger the same-book rule, and can be returned. RETURNED and LOST do not count. |
| Check order | Customer exists (404), book exists (404), customer is ACTIVE, active-borrowing limit, unpaid fines, same book, copy available. |
| Which copy | The AVAILABLE copy with the lowest copy number. A customer with a READY reservation for the book receives the copy held for them instead. |
| Customer endpoint | The customer is always the logged-in user, never a value from the request body. |
| Due date | Borrowing time plus 14 days, taken from an injected `Clock`. |
| Return | The borrowing must be ACTIVE or OVERDUE, otherwise 400. The customer endpoint accepts only the customer's own borrowings: an unknown id is 404 and someone else's borrowing is 403. A librarian can return any borrowing. |
| Lateness | The whole calendar days between the due date and the return date. The time of day is ignored, so a book due at 23:59 and returned at 09:00 the next day is one day late. |
| Fine on return | Created only when the book is late: days times 10 EGP, with two decimals. `borrowings.fine_amount` mirrors it, and is 0 for on-time returns. |
| Damaged return | A librarian can add `?damaged=true`: the copy becomes DAMAGED instead of AVAILABLE, and a late fine is still created. |
| Lost | A librarian marks an active borrowing lost: the borrowing and its copy become LOST, `returnedAt` stays empty, and no replacement-cost fine is created (out of scope). |
| Messages | Borrow rule failures use clear messages, for example "Customer already has the maximum number of active borrowings". |

## 6. Fines

- A fine is UNPAID, PAID or WAIVED. There is one fine per borrowing (enforced by a unique constraint) and fines are paid in full.
- Paying sets PAID and stores the payment time. An unknown id returns 404, another customer's fine returns 403, and a fine that is already PAID or WAIVED returns 400. There is no payment gateway: the status change is the payment.
- Only UNPAID fines count toward the 500 EGP borrowing block. Paying a fine lowers the total, so it lifts the block with no extra step.
- WAIVED exists in the model, but no endpoint sets it because the assignment has no waive operation.

## 7. Reservations

| Topic | Decision |
|---|---|
| Eligibility | Only ACTIVE customers, only when the book has no AVAILABLE copy, and not for a customer who already has the book borrowed or already has a WAITING or READY reservation for it. A book with no copies can be reserved, and the first copy added goes to the queue. Reserving does not check fines or the borrowing limit. |
| Queue | WAITING reservations have a stored `queuePosition` (1, 2, 3...) that is recalculated whenever one leaves the queue. The position is null once a reservation is READY, completed, cancelled or expired. |
| Invariant | A book never has an AVAILABLE copy while a reservation is WAITING for it. |
| Hand-off | Whenever a copy becomes AVAILABLE (returned, repaired, newly added, or released), a `CopyAvailableEvent` is published. If somebody is waiting, the copy becomes RESERVED, the first reservation turns READY, records the held copy, and gets `expiresAt` = now plus 48 hours. This runs inside the same transaction as the change that freed the copy. |
| Borrowing the held copy | The READY customer's borrow request takes the held copy (RESERVED to BORROWED) and the reservation becomes COMPLETED in the same transaction. Nobody else can take it, because a held copy is never AVAILABLE. The usual borrowing rules still apply to them. |
| Expiry | A READY reservation past `expiresAt` becomes EXPIRED and its copy goes to the next waiting customer, or back to AVAILABLE. This is checked before every borrow, reserve, cancel and reservation list, so reads are always current without a scheduled job. |
| Cancelling | A customer can cancel their own WAITING or READY reservation. A WAITING one leaves the queue and the people behind move up. A READY one releases its copy to the next in line. Someone else's reservation returns 403, and one that is no longer active returns 400. |
| Race | Two customers reserving the same book at the very same moment could receive the same position, and ties are broken by id. The one-open-reservation-per-customer-and-book rule is enforced by the database. |

## 8. Reports

- `overview` uses count and sum queries only. It includes the assignment's fields plus `reservedCopies`, `lostCopies` and `maintenanceCopies`, so the copy figures add up to `totalCopies`.
- `activeBorrowings` is ACTIVE plus OVERDUE. `totalCustomers` counts every customer whatever their status. `unpaidFines` is the sum of UNPAID amounts.
- `overdueBorrowings` counts open borrowings whose due date was on an earlier calendar day than today, the same rule the fine uses. It is computed from the dates, so it is correct without a job that sets the OVERDUE status.
- `most-borrowed-books` counts every borrowing, including returned ones. `limit` defaults to 10 and may be 1 to 100.

## 9. Error handling

`GlobalExceptionHandler` turns every exception into the same JSON body. Business rule violations are 400 (as in the assignment's example), duplicates and "still in use" are 409, ownership problems are 403, and unknown ids are 404. The 401 and 403 produced by Spring Security's filters pass through the same handler, so they share the format. Unexpected errors return a generic message and are logged with the stack trace.

## 10. Architecture and data model

- **Identifiers:** all primary keys are `BIGINT` identity values. Access to other users' records is protected by ownership checks in the services.
- **Entities:** plain Lombok-based classes with no common base class. Equality is id-based only on the entities that are stored in `Set`s (author, category, copy). Relationships are lazy, and money is `BigDecimal`.
- **DTOs:** requests and responses are Java records. Entities are never returned from controllers.
- **Time:** services read the time from an injected `Clock`, which keeps the logic testable.
- **Configuration instead of constants:** loan period, borrowing limit, fine rate, fine threshold and reservation hold are properties.

## 11. Testing scope

The assignment requires unit tests for the Borrowing module, covered by `BorrowingServiceTest` (see [How to run the tests](#how-to-run-the-tests)). The reservation queue and the reports have unit tests as well. There are no integration tests against a real database.

---

## Limitations and not implemented

| Item | Status |
|---|---|
| Scheduled jobs (mark overdue borrowings, expire reservations) | Not implemented. Overdue is derived from due dates, and reservation expiry is evaluated on access. The OVERDUE status exists but is never set automatically, so a late borrowing stays ACTIVE until it is returned. |
| Email when a reservation becomes READY | Not implemented. The place to add it is `ReservationService.onCopyAvailable`, where the reservation turns READY. |
| Optimistic locking | Implemented with `@Version` on copies (a conflict returns 409). An automatic retry of the conflicting request is not implemented. |
| Replacement-cost fine for lost books | Out of scope |
| Waiving fines, partial payments, a payment gateway | Out of scope |
| Setting a customer BLOCKED | No endpoint |
| Password reset, refresh tokens, logout, revoking tokens | Out of scope |
| ISBN checksum validation | Only the format is checked |
| Pagination for departments, authors and categories | These lists return everything, sorted by name |
| Integration tests, API rate limiting | Not included |
