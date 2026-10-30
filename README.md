# SpendWise — Personal Finance Tracker

> **Academic Mini-Project for Server Side Engineering**  
> Developed using Spring Boot 3.3.5, Java 21, Spring Security (Session Authentication & BCrypt), Spring Data JPA, Hibernate ORM, MySQL, and a human-crafted editorial frontend with bundled offline Chart.js.

---

## 1. Problem Statement

College students and young professionals often struggle with tracking daily expenditures, budgeting across varied spending areas (food, transit, academic supplies), and understanding their monthly financial health. Traditional spreadsheets are cumbersome on mobile devices and lack automated progress alerts, while commercial banking applications frequently omit cash/stipend tracking and cross-account category budgets.

**SpendWise** solves this problem by providing an intuitive, editorial, and self-hosted personal finance platform. It enables individual users to securely register and log in, record income inflows and expense outflows, allocate monthly limits per category, track utilization with warning thresholds, and view analytical reports with month-over-month comparisons — all with complete multi-user data isolation.

---

## 2. Key Objectives & Features

- **Robust Layered Architecture**: Java 21 and Spring Boot 3.3.5 with clean separation of Controllers, Services, DTOs, Repositories, and Entities.
- **Session-Based Authentication & Security**: Spring Security session-based authentication with `JSESSIONID`, BCrypt password hashing, and CSRF protection (`CookieCsrfTokenRepository`).
- **Complete User Data Isolation**: Every transaction, budget, dashboard metric, and report query is strictly scoped to the authenticated `User`.
- **Relational MySQL Persistence**: Backed by **Spring Data JPA** and **Hibernate ORM**, ensuring exact monetary decimal precision (`DECIMAL(12, 2)` / `BigDecimal`).
- **Idempotent Demo Data Seeding & Selective Clearing**: Academic demo seeder that never duplicates records on repeated clicks, plus a dedicated "Clear Demo Data" action that preserves personal records.
- **Single Primary Action UI**: Exactly one Add Transaction button on the dashboard header, eliminating confusing duplicates.
- **Centered, Scroll-Locked Modal**: 620px max-width Add Transaction modal with 2-column Date/Payment Method desktop layout and background scroll locking.
- **Responsive Editorial Design System**: Warm stone `#F7F7F2` canvas, deep forest green `#276749`, terracotta `#B85C50`, warm amber `#C28A35`, and tabular numerals (`tabular-nums`).
- **100% Offline Capable**: Chart.js bundled locally in `/js/lib/` with no external CDN dependency.
- **31 Automated Tests**: Unit and integration test suite covering security, user isolation, idempotency, calculation logic, and MockMvc endpoints.

---

## 3. Technology Stack

### Backend
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.3.5
- **Security**: Spring Security 6.3 (`spring-boot-starter-security`, BCrypt password encoder, `CookieCsrfTokenRepository`)
- **Build Tool**: Apache Maven 3.9+
- **RESTful APIs**: Spring Web (`@RestController`, `@RequestMapping`, Content Negotiation)
- **Data Access & ORM**: Spring Data JPA, Hibernate 6.5
- **Database Engine**: MySQL 8.x / 9.x (`finance_tracker`)
- **Validation**: Jakarta Bean Validation (`@Valid`, `@DecimalMin`, `@NotBlank`, `@Pattern`, `@Email`)
- **Centralized Error Handling**: Spring `@RestControllerAdvice`
- **Testing**: JUnit 5, Spring Boot Test, Spring Security Test, MockMvc, H2 Database (in-memory test profile)

### Frontend
- **Markup**: Semantic HTML5 (Desktop persistent 250px sidebar, topbar, tabbed views, accessible dialogs)
- **Styling**: Modular CSS3 (custom CSS custom properties, editorial color palette `#F7F7F2` / `#276749`, responsive drawer)
- **Scripting**: Vanilla JavaScript (ES6 modules, Fetch API, DOM manipulation)
- **Charts**: Chart.js v4.4.x (bundled locally in `/js/lib/` for 100% offline operation)

---

## 4. Architecture Overview

SpendWise follows a strict **Layered Enterprise Architecture**:

```
[ Client Browser (HTML5 / CSS3 / Vanilla JS / Chart.js) ]
                        │
                        ▼ (HTTP JSON via Fetch API + JSESSIONID + XSRF-TOKEN)
[ Security Filter Chain: Session Authentication, CsrfFilter, BCrypt ]
                        │
                        ▼
[ Presentation Layer: REST Controllers & GlobalExceptionHandler ]
                        │
                        ▼ (Data Transfer Objects - DTOs & Authenticated User)
[ Business Logic Layer: Services, Validation & User Ownership Enforcement ]
                        │
                        ▼ (Entities: User, Transaction, Budget)
[ Data Persistence Layer: Spring Data JPA Repositories (User-Scoped Queries) ]
                        │
                        ▼ (JDBC / SQL Queries & Aggregations)
[ Database: MySQL Server (Database: finance_tracker) ]
```

### Source Package Structure

```
com.spendwise/
├── SpendWiseApplication.java          # Spring Boot Application Entry Point
├── config/
│   ├── SecurityConfig.java            # Spring Security session auth & CSRF config
│   └── WebConfig.java                 # Static resource handlers & CORS
├── controller/
│   ├── AuthController.java            # /api/auth (register, login, me, logout)
│   ├── TransactionController.java     # /api/transactions CRUD & pagination
│   ├── DashboardController.java       # /api/dashboard summary metrics & charts
│   ├── BudgetController.java          # /api/budgets category limits & tracking
│   ├── ReportController.java          # /api/reports trends & month-over-month deltas
│   └── DemoDataController.java        # /api/demo sample seeding, clear-demo & reset
├── dto/
│   ├── RegisterRequest.java           # Registration payload validation
│   ├── LoginRequest.java              # Login credentials payload
│   ├── UserProfileResponse.java       # Clean user info DTO
│   ├── TransactionRequest.java        # Validation annotated transaction payload
│   ├── TransactionResponse.java       # Clean JSON transaction representation with isDemo
│   ├── BudgetRequest.java             # Validation annotated budget payload
│   ├── BudgetResponse.java            # Spending progress, status & isDemo
│   ├── DashboardSummaryResponse.java  # Consolidated dashboard metrics
│   ├── CategorySpendingDto.java       # Category distribution & percentages
│   ├── MonthlyTrendDto.java           # 6-month historical trend datapoints
│   ├── MonthlyReportResponse.java     # Comparative report analytics
│   └── ApiResponse.java               # Standardized API response wrapper
├── entity/
│   ├── User.java                      # JPA Entity for `users` table
│   ├── Transaction.java               # JPA Entity for `transactions` table (ManyToOne User)
│   ├── Budget.java                    # JPA Entity for `budgets` table (ManyToOne User)
│   └── TransactionType.java           # Enum: INCOME, EXPENSE
├── exception/
│   ├── BadRequestException.java       # HTTP 400 Bad Request
│   ├── ResourceNotFoundException.java # HTTP 404 Not Found
│   ├── DuplicateResourceException.java# HTTP 409 Conflict
│   └── GlobalExceptionHandler.java    # Centralized @RestControllerAdvice
├── repository/
│   ├── UserRepository.java            # User email lookups & existence checks
│   ├── TransactionRepository.java     # User-scoped JPQL aggregations & specifications
│   └── BudgetRepository.java          # User-scoped month & category queries
└── service/
    ├── AuthService.java               # Registration, BCrypt hashing, SecurityContext helper
    ├── UserDetailsServiceImpl.java    # Spring Security UserDetailsService implementation
    ├── TransactionService.java        # Transaction lifecycle, ownership & filtering logic
    ├── BudgetService.java             # Budget limits & progress calculation per user
    ├── DashboardService.java          # Metric aggregation & recent activity per user
    ├── ReportService.java             # Trend analysis & percentage deltas per user
    └── DemoDataService.java           # Idempotent demo seeder, selective clearing & reset
```

---

## 5. Prerequisites

Before installing and running the project, verify that the following tools are installed on your workstation:
1. **Java Development Kit (JDK)**: Java 17, 21, or 25 installed. Verify with `java -version`.
2. **Apache Maven**: Version 3.8+ or 3.9+. Verify with `mvn -version`.
3. **MySQL Server**: MySQL 8.0+ or MySQL 9.x running on port `3306`.
4. Modern Web Browser: Google Chrome, Mozilla Firefox, Microsoft Edge, or Safari.

---

## 6. Database Setup Instructions

1. Start your local MySQL service (e.g., via Windows Services or MySQL Workbench).
2. Open MySQL Command Line Client or MySQL Workbench and execute:
   ```sql
   CREATE DATABASE IF NOT EXISTS finance_tracker;
   ```
3. The application automatically initializes the tables via Hibernate ORM on boot. To manually inspect the schema, refer to `src/main/resources/schema.sql`:
   ```sql
   USE finance_tracker;

   CREATE TABLE IF NOT EXISTS users (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       email VARCHAR(150) NOT NULL UNIQUE,
       password VARCHAR(255) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       INDEX idx_user_email (email)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

   CREATE TABLE IF NOT EXISTS transactions (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       user_id BIGINT NOT NULL,
       type VARCHAR(10) NOT NULL,
       amount DECIMAL(12, 2) NOT NULL,
       category VARCHAR(50) NOT NULL,
       description VARCHAR(255) NOT NULL,
       transaction_date DATE NOT NULL,
       payment_method VARCHAR(50) DEFAULT 'OTHER',
       is_demo BOOLEAN NOT NULL DEFAULT FALSE,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
       INDEX idx_trans_user (user_id),
       INDEX idx_trans_date (transaction_date),
       INDEX idx_trans_user_demo (user_id, is_demo)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

   CREATE TABLE IF NOT EXISTS budgets (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       user_id BIGINT NOT NULL,
       category VARCHAR(50) NOT NULL,
       budget_month VARCHAR(7) NOT NULL,
       limit_amount DECIMAL(12, 2) NOT NULL,
       is_demo BOOLEAN NOT NULL DEFAULT FALSE,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
       CONSTRAINT uk_user_category_month UNIQUE (user_id, category, budget_month),
       INDEX idx_budget_user_month (user_id, budget_month)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
   ```

---

## 7. Environment Configuration

SpendWise is configured via `src/main/resources/application.properties`. It supports direct environment variables with sensible defaults:

| Property Name | Environment Variable | Default Value | Description |
|---|---|---|---|
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/finance_tracker?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata` | MySQL JDBC Connection URL |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | `root` | MySQL Database User |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | `root` | MySQL Database Password |
| `server.port` | `PORT` | `8080` | Embedded Tomcat HTTP Port |

If your local MySQL root password differs (e.g. `yourpassword`), you can pass it when starting the application without editing source code:
```powershell
$env:SPRING_DATASOURCE_PASSWORD="yourpassword"
mvn spring-boot:run
```

---

## 8. Build and Run Commands

### 1. Compile the Project
```powershell
mvn clean compile
```

### 2. Execute Automated Test Suite (Uses In-Memory H2 Profile)
```powershell
mvn test
```
All **31 unit and integration tests** run in an isolated in-memory H2 database without requiring MySQL credentials.

### 3. Package Executable Fat JAR
```powershell
mvn package
```
Generates the self-contained executable jar in `target/spendwise-finance-tracker-1.0.0.jar`.

### 4. Run the Application
#### Option A: Running with Maven
```powershell
mvn spring-boot:run
```

#### Option B: Running the Packaged JAR
```powershell
java -jar target/spendwise-finance-tracker-1.0.0.jar
```

Once started, open your web browser and navigate to:
👉 **`http://localhost:8080`**

---

## 9. REST API Endpoint Reference

All endpoints return responses in a standardized JSON envelope:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-10-29T21:00:00"
}
```

### Authentication API (`/api/auth`)
- `POST /api/auth/register`
  - Body: `{ "name": "Alex", "email": "alex@test.com", "password": "SecretPassword123", "confirmPassword": "SecretPassword123" }`
  - Registers a new user with BCrypt password hashing.
- `POST /api/auth/login`
  - Body: `{ "email": "alex@test.com", "password": "SecretPassword123" }`
  - Authenticates credentials and sets HTTP session cookie (`JSESSIONID`).
- `GET /api/auth/me`
  - Returns authenticated user profile (401 if unauthenticated).
- `POST /api/auth/logout`
  - Invalidates session and clears `JSESSIONID` cookie.

### Transactions API (`/api/transactions`) [Requires Auth]
- `GET /api/transactions`
  - Query Parameters: `type` (INCOME/EXPENSE), `category`, `startDate`, `endDate`, `search`, `page` (default 0), `size` (default 10), `sort` (default `transactionDate,desc`)
  - Returns paginated transaction page for authenticated user.
- `GET /api/transactions/{id}`
  - Returns a single transaction by ID (404 if missing or belongs to another user).
- `POST /api/transactions`
  - Request Body: JSON with `type`, `amount`, `category`, `description`, `transactionDate`, `paymentMethod`.
  - Returns 201 Created with persisted record.
- `PUT /api/transactions/{id}`
  - Updates an existing transaction (enforces ownership).
- `DELETE /api/transactions/{id}`
  - Deletes transaction record (enforces ownership).

### Dashboard API (`/api/dashboard`) [Requires Auth]
- `GET /api/dashboard/summary?month=YYYY-MM`
  - Returns consolidated monthly income, expenses, net savings, all-time balance, remaining budget, category breakdowns, recent 5 transactions, and budget progress for authenticated user.

### Budgets API (`/api/budgets`) [Requires Auth]
- `GET /api/budgets?month=YYYY-MM`
  - Returns category budgets for the month with calculated `actualSpent`, `remainingAmount`, `percentageUsed`, and `status` (`NORMAL`, `WARNING`, `EXCEEDED`).
- `POST /api/budgets`
  - Creates a new category budget. Enforces uniqueness on `(user_id, category, month)`.
- `PUT /api/budgets/{id}`
  - Updates spending limit.
- `DELETE /api/budgets/{id}`
  - Deletes budget limit.

### Reports API (`/api/reports`) [Requires Auth]
- `GET /api/reports/monthly?month=YYYY-MM`
  - Returns monthly financial metrics and comparative deltas vs previous month.
- `GET /api/reports/category-breakdown?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
  - Returns category expenses for custom date ranges.
- `GET /api/reports/monthly-trend?months=6`
  - Returns historical income vs expenses trend series.

### Demo Data API (`/api/demo`) [Requires Auth]
- `POST /api/demo/seed`
  - Idempotently populates realistic student sample transactions and budgets across recent months (safe to click repeatedly).
- `POST /api/demo/clear-demo`
  - Selectively removes **only** demo-seeded records (`is_demo = true`), leaving personal records completely intact.
- `POST /api/demo/reset`
  - Deletes all transaction and budget records for the authenticated user.

---

## 10. Demonstration & Viva Guide

1. **Authentication & User Registration**:
   - Open `http://localhost:8080`. Unauthenticated visitors see the clean SpendWise login screen.
   - Click "Create an account" and register (e.g. `alex@college.edu`).
   - The application automatically logs you in and presents an empty dashboard with zeroed metrics.
2. **User Isolation Verification**:
   - In an incognito window, register a second account (e.g. `bob@college.edu`).
   - Add a transaction in Bob's account. Observe that Alex's account never sees or aggregates Bob's data.
3. **Idempotent Sample Demo Seeding**:
   - In Alex's account, click **"Load Demo Data"** from the dashboard banner or Settings.
   - Click it a second and third time. Show that the transaction count does NOT multiply — idempotency protects against duplicate records.
4. **Selective Demo Clearing**:
   - Add a real personal transaction (e.g., `Expense: ₹350.00, Canteen Lunch, CASH`).
   - Navigate to Settings and click **"Clear Demo Data Only"**.
   - Return to Transactions: all demo records are gone, while your personal ₹350 lunch expense is completely preserved.
5. **Modal UI & Scroll Locking**:
   - Click **"+ Add Transaction"** in the topbar (the single primary button).
   - Observe the centered 620px dialog with comfortable spacing, 46px input heights, and 2-column Date/Payment Method row.
   - Try to scroll the background page: scrolling is locked while the modal is open. Close via Escape key or backdrop click.
6. **Budget Warning & Exceeded Thresholds**:
   - Navigate to **Budgets**.
   - Show how the three visual states operate:
     - Normal (`< 80%`): Restrained forest green progress bar.
     - Warning (`80% – 100%`): Amber badge and progress fill as spending approaches the limit.
     - Exceeded (`> 100%`): Red alert badge and progress fill with negative remaining calculation.
7. **Reports & Month-over-Month Analytics**:
   - Navigate to **Reports**.
   - Point out the **Month-over-Month Delta Comparison**: real percentage and absolute amount changes calculated against the previous month.
