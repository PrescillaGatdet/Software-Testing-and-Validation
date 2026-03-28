# ENSE 375 – Software Testing and Validation

# Budget Management System

**Team Members:**

- Nyabijek Gatdet (200479720)
- Chop Peter Kur (200497265)
- Aubin Chriss Izere (200490675)

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Design Problem](#2-design-problem)
  - 2.1 [Problem Definition](#21-problem-definition)
  - 2.2 [Design Requirements](#22-design-requirements)
3. [Solution](#3-solution)
  - 3.1 [Solution 1](#31-solution-1)
  - 3.2 [Solution 2](#32-solution-2)
  - 3.3 [Final Solution](#33-final-solution)
4. [Team Work](#4-team-work)
  - 4.1 [Meeting 1](#41-meeting-1)
  - 4.2 [Meeting 2](#42-meeting-2)
  - 4.3 [Meeting 3](#43-meeting-3)
  - 4.4 [Meeting 4](#44-meeting-4)
  - 4.5 [Meeting 5](#45-meeting-5)
  - 4.6 [Meeting 6](#46-meeting-6)
  - 4.7 [Meeting 7](#47-meeting-7)
5. [Project Management](#5-project-management)
6. [Conclusion and Future Work](#6-conclusion-and-future-work)
7. [References](#7-references)
8. [Appendix](#8-appendix)

---

## List of Figures

| Figure | Caption | Section |
|--------|---------|---------|
| Fig. 1 | Budget Management System Architecture — Unidirectional dependency flow across Main, View, Controller, DAO, Model, and Local Storage layers | §3.3.1 |
| Fig. A.1 | Control Flow Graph (CFG) for `TransactionController.filterByDateRange()` — Five nodes with three decision branches yielding V(G) = 5 | §A.1 |
| Fig. A.2 | State Transition Diagram — `Transaction` object lifecycle (Created → Modified states) | §A.2 |
| Fig. A.3 | State Transition Diagram — `Budget` object lifecycle (Unconfigured → Normal → Warning → Exceeded → Recovered) | §A.2 |

---

## List of Tables

| Table | Caption | Section |
|-------|---------|---------|
| Table 1 | Design Constraints — Eight constraints categorized by Economic, Security, Reliability, Sustainability, Ethics, Societal Impact, and Technical considerations | §2.2.3 |
| Table 2 | Architecture Comparison — Solution 1 (Monolithic) vs. Solution 2 (Two-Tier) vs. Final Solution (MVC) across eight testing criteria | §3.3 |
| Table 3 | System Components — All fifteen Java classes and two enums with their layer, purpose, and testing method | §3.3.1 |
| Table 4 | Test Suite Summary — 279 tests across six categories: Model, DAO, Controller, Structural, Integration, and Validation | §3.3.3 |
| Table 5 | Meeting 1 Progress — January 23, 2026: Project Initiation and task assignments | §4.1 |
| Table 6 | Meeting 2 Progress — January 30, 2026: Architecture Decision review and assignments | §4.2 |
| Table 7 | Meeting 3 Progress — February 12, 2026: Model Layer review and DAO planning | §4.3 |
| Table 8 | Meeting 4 Progress — March 10, 2026: DAO review and Controller development sprint | §4.4 |
| Table 9 | Meeting 5 Progress — March 21, 2026: View Layer, Structural, and Integration Testing review | §4.5 |
| Table 10 | Meeting 6 Progress — March 22, 2026: Pre-deadline coordination and documentation | §4.6 |
| Table 11 | Meeting 7 Progress — March 27, 2026: Final review and submission preparation | §4.7 |
| Table 12 | Gantt Chart — 38 tasks mapped across ten calendar weeks (W1–W10) | §5 |
| Table 13 | Task Dependency and Critical Path — Predecessor, duration, slack, and critical path designation for all 38 tasks | §5 |

---

## 1. Introduction

Personal financial management has become increasingly important as individuals seek better control over their income, expenses, and savings. Many people struggle to track where their money goes, leading to overspending and difficulty achieving financial goals. A budget management system addresses this need by providing users with tools to record transactions, organize them into categories, and view summaries that reveal spending patterns.

The Budget Management System developed in this project allows users to input income and expenses, assign transactions to predefined categories, and generate reports summarizing their financial activity. The system is built using a modular architecture that separates data handling, business logic, and user interface components, making it well-suited for applying structured software testing methodologies.

Software testing is critical for financial applications because calculation errors or data handling bugs could cause users to misunderstand their financial situation. A budget tool that displays incorrect totals or loses transaction data would fail its fundamental purpose. This project applies software testing and validation principles including test planning, test case design, validation techniques, and defect tracking to ensure the system performs correctly and reliably.

The following sections describe the problem definition and design requirements in Section 2, present alternative solutions and the final chosen approach in Section 3, document team collaboration and meetings in Section 4, outline the project schedule in Section 5, and conclude with a summary of achievements and recommendations for future work in Section 6.

---

## 2. Design Problem

### 2.1 Problem Definition

Individuals often lack visibility into their spending habits, making it difficult to manage finances effectively and achieve savings goals. Without a structured system to record and categorize transactions, users may lose track of expenses, overspend in certain categories, or fail to identify areas where they could reduce costs. Manual tracking methods such as spreadsheets are error-prone and time-consuming, while many existing budgeting applications are either too complex or lack the reliability users need to trust their financial data.

The goal of this project is to develop a Budget Management System that enables users to track income and expenses, categorize transactions, and generate accurate budget summaries and reports. The system must be thoroughly tested and validated to ensure that all financial calculations are correct, data is stored and retrieved reliably, input validation prevents invalid entries, and reports accurately reflect the user's financial activity.

From a software testing perspective, the problem is to design and execute a comprehensive test strategy that verifies the system's functionality, accuracy, and robustness. This includes creating test cases that cover normal operations, boundary conditions, and error scenarios, as well as tracking and resolving any defects discovered during testing. The testing effort must demonstrate that the Budget Management System meets its requirements and can be trusted to provide users with accurate financial information.

### 2.2 Design Requirements

#### 2.2.1 Functions

The Budget Management System shall perform the following functions:

- **Record income transactions** – Allow users to input income entries with amount, date, source, and category
- **Record expense transactions** – Allow users to input expense entries with amount, date, description, and category
- **Categorize transactions** – Assign and manage categories (e.g., Food, Transportation, Entertainment, Utilities, Salary, Investments) for both income and expenses
- **Set budget limits** – Enable users to define monthly spending limits for individual categories or overall budget
- **Calculate budget summaries** – Compute totals, averages, and remaining budget based on recorded transactions
- **Generate financial reports** – Produce summary reports showing income vs. expenses over specified time periods (weekly, monthly, yearly)
- **Alert on budget exceedance** – Notify users when spending in a category approaches or exceeds the defined limit
- **Filter and search transactions** – Allow users to find specific transactions by date range, category, or amount
- **Persist data** – Save all transaction and budget data to local storage for retrieval between sessions
- **Export data** – Allow users to export transaction history and reports to external file formats (e.g., CSV)

#### 2.2.2 Objectives

The Budget Management System should exhibit the following quality attributes:

- **User-friendly** – The interface should be intuitive and require minimal learning curve for new users
- **Accurate** – All financial calculations must be precise with proper handling of decimal currency values
- **Responsive** – The system should provide immediate feedback for user actions and calculations
- **Secure** – Sensitive financial data should be protected from unauthorized access
- **Reliable** – The system should perform consistently without crashes or data corruption
- **Maintainable** – Code should be well-structured following MVC architecture for easy modifications and updates
- **Testable** – All components should be designed to facilitate comprehensive unit, integration, and validation testing
- **Efficient** – The system should perform operations quickly even with large transaction datasets
- **Portable** – The application should run on standard computing environments without specialized dependencies

#### 2.2.3 Constraints

The following constraints must be satisfied by the Budget Management System:


| Constraint                           | Category           | Description                                                                                                                        |
| ------------------------------------ | ------------------ | ---------------------------------------------------------------------------------------------------------------------------------- |
| **C1: No External Paid Services**    | Economic           | The system must be developed using only free and open-source tools and libraries, with zero cost for third-party APIs or services  |
| **C2: Local Data Storage Only**      | Security & Privacy | All user financial data must be stored locally on the user's machine; no data transmission to external servers is permitted        |
| **C3: Data Integrity Preservation**  | Reliability        | The system must ensure no data loss during normal operations, including proper handling of unexpected shutdowns or invalid inputs  |
| **C4: Cross-Platform Compatibility** | Sustainability     | The application must run on major operating systems (Windows, macOS, Linux) without modification to the core codebase              |
| **C5: No Personal Data Sharing**     | Ethics             | The system must not collect, transmit, or share any user personal or financial information with third parties                      |
| **C6: Accessible Interface**         | Societal Impact    | The system must provide clear text-based feedback and support keyboard navigation for users with varying technical abilities       |
| **C7: JUnit Test Coverage**          | Technical          | All core business logic must have corresponding JUnit test cases with minimum 80% code coverage                                    |
| **C8: MVC Architecture Compliance**  | Technical          | The implementation must strictly follow Model-View-Controller separation with no direct coupling between View and Model components |


---

## 3. Solution

This section describes the iterative design process followed to develop the Budget Management System. Three architectural approaches were considered and evaluated based on their testability, maintainability, and compliance with the design requirements and constraints outlined in Section 2. Each solution was formally compared using criteria drawn directly from the project's testing requirements, leading to the selection of a full MVC architecture for the final implementation.

### 3.1 Solution 1

#### Monolithic Procedural Approach

##### Description

The first solution considered was a simple procedural approach where all functionality would be contained within a single Java class file. This design would use static methods for all operations including transaction recording, category management, budget calculations, and report generation. Data would be stored in simple arrays or ArrayLists as class-level static variables, and the user interface would consist of `System.out.println()` statements mixed directly with the business logic.

The single class (`BudgetManager.java`) would contain all variables (transactions, categories, budget amounts) and all methods (`main`, `addTransaction`, `calculateTotal`, `printReport`, `saveToFile`, `displayMenu`) in one place with no separation between components.

##### Reasons for Not Selecting (Testing Perspective)

This solution was rejected primarily due to significant testing limitations:

- **Tight Coupling** — Business logic, data storage, and user interface code are intermingled in the same class. This makes it impossible to test calculation logic without also triggering console output, violating the principle of separation of concerns.

- **Static Method Testing Difficulty** — Static methods cannot be easily mocked or overridden, making it difficult to isolate units for testing. JUnit tests would need to test the entire application flow rather than individual components.

- **No Dependency Injection** — With hardcoded dependencies, we cannot substitute test doubles (mocks, stubs) for components like file I/O during testing. This prevents effective unit testing of business logic in isolation.

- **State Management Issues** — Static variables maintain state across test executions, causing tests to interfere with each other. Each test would need extensive setup and teardown to reset the global state, making the test suite fragile and order-dependent.

- **Path Testing Complexity** — With all logic in one class, the control flow graph would be extremely complex, making it difficult to identify and systematically test all independent basis paths.

- **Limited Integration Testing** — Since there are no separate modules, integration testing across layer boundaries is not applicable, missing an important validation layer entirely.

### 3.2 Solution 2

#### Layered Architecture (Two-Tier)

##### Description

The second solution introduced a layered architecture separating the application into two tiers: a Data Layer and an Application Layer.

The Data Layer would handle all data storage and retrieval operations using dedicated classes for file management, including `FileManager.java` (for reading, writing, and parsing CSV files) and `TransactionDAO.java` (for save, load, and delete operations).

The Application Layer would contain `BudgetApplication.java` with `TransactionService` for business logic, `ReportService` for calculations, and `UserInterface` for console I/O. However, the UI methods would still call service methods directly, keeping them partially coupled.

##### Improvements Over Solution 1

- Separate Data Layer allows testing of file operations independently
- Service classes can be instantiated (non-static), enabling better unit testing
- Data Access Objects (DAO) pattern allows mocking of data operations

##### Reasons for Not Selecting (Testing Perspective)

While this solution improved testability, it was still rejected due to the following testing concerns:

- **View-Logic Coupling** — The user interface code remains coupled with business logic in the Application Layer. Testing business rules still requires dealing with console I/O, complicating automated testing.

- **Incomplete Separation** — Without a dedicated Controller layer, the flow of data between user input and business logic is not clearly defined. This makes it difficult to apply state transition testing effectively, as there are no clearly bounded state machines to model.

- **Limited Mock Injection** — While the Data Layer can be mocked, the tight coupling in the Application Layer means we cannot easily inject mock services for testing the UI flow without triggering actual business logic.

- **Decision Table Testing Challenges** — User input handling and business rule processing are combined, making it difficult to create clean decision tables that map inputs to expected outputs without entangling UI state.

- **Integration Test Gaps** — The two-tier structure provides only one integration boundary (Application-to-Data). A three-tier MVC architecture would provide two or more integration points, allowing more thorough integration testing across distinct layer boundaries.

- **Does Not Meet Constraint C8** — This solution does not comply with the MVC Architecture Compliance constraint, which requires strict separation with no direct coupling between View and Model components.

### 3.3 Final Solution

The final solution implements a complete Model-View-Controller (MVC) architecture that directly addresses all testing limitations identified in Solutions 1 and 2. The choice of MVC was driven by the project's testing requirements rather than being purely an architectural preference — MVC creates the component isolation, constructor-based dependency injection pathways, and clearly defined integration boundaries that are necessary to apply all five validation testing techniques, structural path-based testing, Mockito-based unit testing, and real file I/O integration testing in a systematic and verifiable way.

The system is organized into four distinct layers:

1. **Model Layer** — Pure data classes with no I/O: `Transaction`, `Budget`, `Report`, and two enums (`TransactionType`, `Category`). All field validation lives in constructors and setters, making each class independently unit testable with no mocking required.
2. **DAO Layer** — File I/O abstraction: `FileManager` (low-level CSV operations), `TransactionDAO`, and `BudgetDAO`. These classes contain no business logic and are fully testable using real temporary files via JUnit's `@TempDir`.
3. **Controller Layer** — All business logic: `TransactionController`, `BudgetController`, and `ReportController`. Controllers receive DAOs through constructor injection, enabling Mockito-based unit testing with zero file I/O during any unit test.
4. **View Layer** — Pure output: `ConsoleView`, `TransactionView`, `ReportView`, and `Main.java`. Views accept injected `Scanner` and `PrintStream` instances so that console input and output can be redirected during automated testing.

**Comparison Table:**


| Criteria | Solution 1 (Monolithic) | Solution 2 (Two-Tier) | Final Solution (MVC) |
| -------- | ----------------------- | --------------------- | -------------------- |
| Unit Test Isolation | Not possible — all code in one class | Partial — Data Layer isolated, Application Layer still coupled | Full — Model, DAO, and Controller layers each independently testable |
| Dependency Injection Support | None — static methods and class-level state | Partial — DAOs injectable, UI methods hardcoded | Complete — all DAOs and views injected via constructors |
| Integration Test Boundaries | None — single monolithic module | One boundary: Application ↔ Data | Two clear boundaries: Controller ↔ DAO ↔ FileManager |
| Mockito Mock Usage | Not applicable | Partial — DAOs mockable, services still coupled | Full — all DAOs mocked in controller tests; zero file I/O in unit tests |
| Decision Table Testing | Very difficult — rules mixed with output statements | Difficult — no clean input/output mapping | Natural — BudgetController rules map directly to a four-rule decision table |
| State Transition Testing | Not possible — no defined states | Partially possible | Clear — Transaction and Budget lifecycle states with explicit transitions |
| MVC Compliance (Constraint C8) | Fails — single class violates all MVC principles | Fails — no dedicated Controller layer | Passes — strict View/Controller/Model separation enforced throughout |
| Code Coverage (JaCoCo ≥80%) | Low — static dependencies limit instrumentation | Moderate — some paths untestable | Achieved — all layers fully instrumented; ≥80% confirmed |


---

#### 3.3.1 Components

The Budget Management System consists of fifteen Java classes organized across four architectural layers, supported by two enums. Each component was designed with testability as a first-class concern, so that every class can be unit tested in isolation without depending on any other layer.


| Component | Layer | Purpose | Testing Method |
| --------- | ----- | ------- | -------------- |
| `TransactionType` | Model | Enum defining INCOME and EXPENSE transaction types; used by Transaction, controllers, and report generation | Used as a typed parameter in unit and integration tests — no dedicated test class needed |
| `Category` | Model | Enum with seven spending categories: FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, SALARY, INVESTMENT, OTHER; each has `getDisplayName()` for console table formatting | Used as a typed parameter in unit and integration tests — no dedicated test class needed |
| `Transaction` | Model | Core data class: id (UUID auto-generated), amount, date, description, category, type; constructor validates all six fields; equality is id-based only | Unit Testing — 24 tests (TransactionTest) covering constructor validation, setters, UUID generation, and id-based equality |
| `Budget` | Model | Tracks spending limit and current spending per category; computes `isExceeded()`, `isNearLimit()` (≥80% threshold), `getRemainingBudget()`, and `getUsagePercentage()` | Unit Testing — 26 tests (BudgetTest) verifying threshold logic at and around 80%, setter validation, and edge cases |
| `Report` | Model | Immutable value object: totalIncome, totalExpense, period label; computes `getBalance()` and `getSavingsRate()` with a zero-income guard | Unit Testing — 18 tests (ReportTest) covering positive/negative balance, zero-income edge case, and period string formatting |
| `FileManager` | DAO | Low-level CSV I/O: `readLines`, `writeLines`, `appendLine`, `fileExists`, `deleteFile`; creates parent directories automatically on write | Unit Testing with @TempDir — 13 tests (FileManagerTest) covering read/write/append round-trips, overwriting, directory creation, and deletion |
| `TransactionDAO` | DAO | Persists and retrieves Transaction objects via CSV; handles RFC 4180 quoting for descriptions with commas; delegates all file ops to FileManager | Unit Testing with @TempDir — 13 tests (TransactionDAOTest) covering save/reload round-trip, header skipping, deleteById, findByCategory, findById |
| `BudgetDAO` | DAO | Persists Budget objects with upsert logic (category-keyed replace-or-append); delegates file ops to FileManager | Unit Testing with @TempDir — 12 tests (BudgetDAOTest) covering upsert behavior, findByCategory, deleteByCategory, and round-trip persistence |
| `TransactionController` | Controller | Business logic: add, remove, filter by category/date range, search by description, export to CSV; validates inputs and delegates persistence to TransactionDAO | Mockito Unit Testing — 24 tests (TransactionControllerTest) with mocked DAO; uses ArgumentCaptor to verify exact CSV export content |
| `BudgetController` | Controller | Implements four-rule Decision Table for budget alerts: no budget (Rule 1), under 80% (Rule 2), 80–100% warning (Rule 3), over 100% exceeded (Rule 4); upserts budget limits preserving existing spending | Mockito Unit Testing + Decision Table Testing — 27 tests (BudgetControllerTest) verifying all four rules including exact 80% and 100% boundaries |
| `ReportController` | Controller | Generates monthly reports (validates month 1–12), yearly reports, and category expense breakdowns (EXPENSE transactions only, using EnumMap) | Mockito Unit Testing — 14 tests (ReportControllerTest) covering aggregation, invalid month rejection, and income exclusion from breakdowns |
| `ConsoleView` | View | All console I/O: main menu (12 options), category menu, type menu, input prompts with validation loops; accepts injected Scanner and PrintStream for testability | Tested indirectly via integration and use case tests; constructor injection allows output capture in automated tests |
| `TransactionView` | View | Tabular transaction list with fixed-width columns (ID 36, Amount 8, Date 10, Description 20, Category 13, Type 8); truncates descriptions >20 chars; handles empty list gracefully | Tested indirectly via integration tests with injected PrintStream |
| `ReportView` | View | Displays budget status with ASCII progress bars and [WARNING]/[EXCEEDED] flags, financial report summaries, and category breakdown tables with TOTAL row | Tested indirectly via integration tests with injected PrintStream |
| `Main` | Application | MVC wiring entry point: constructs all layers in dependency order (FileManager → DAOs → Controllers → Views) and runs the console dispatch loop; handles IOException and IllegalArgumentException | Verified end-to-end via use case tests (UC-01 to UC-08); parseCategory and parseType helpers tested as part of integration scenarios |


*Fig. 1 — Budget Management System Architecture. The diagram below shows the unidirectional dependency flow across the four layers. `Main.java` at the top wires all components together. The View layer receives user input and delegates actions to the Controller layer. Controllers apply all business logic and delegate persistence to the DAO layer. The DAO layer reads and writes local CSV files in the `data/` directory. No reverse dependencies exist between layers, preserving strict MVC separation as required by Constraint C8.*

```
┌─────────────────────────────────────────────────────────────────┐
│                           Main.java                             │
│                  (MVC Wiring + Application Loop)                │
└──────────────┬──────────────────────────────┬───────────────────┘
               │                              │
               ▼                              ▼
┌──────────────────────────┐   ┌──────────────────────────────────┐
│        View Layer        │   │        Controller Layer           │
│  ConsoleView             │◄──│  TransactionController            │
│  TransactionView         │   │  BudgetController                 │
│  ReportView              │   │  ReportController                 │
└──────────────────────────┘   └────────────────┬─────────────────┘
                                                │
                                                ▼
                               ┌────────────────────────────────────┐
                               │            DAO Layer                │
                               │  FileManager  (CSV I/O)             │
                               │  TransactionDAO                     │
                               │  BudgetDAO                          │
                               └────────────────┬───────────────────┘
                                                │
                                                ▼
                               ┌────────────────────────────────────┐
                               │           Model Layer               │
                               │  Transaction       Budget           │
                               │  Report            Category (enum)  │
                               │  TransactionType (enum)             │
                               └────────────────┬───────────────────┘
                                                │
                                                ▼
                               ┌────────────────────────────────────┐
                               │          Local Storage              │
                               │    data/transactions.csv            │
                               │    data/budgets.csv                 │
                               └────────────────────────────────────┘
```

---

#### 3.3.2 Environmental, Societal, Safety, and Economic Considerations

The development and deployment of the Budget Management System involved considerations that extend beyond functional correctness. The following discusses how the system design addresses environmental, societal, safety, and economic concerns, mapped to the constraints defined in Section 2.2.3.

**Economic Considerations (Constraint C1 — No External Paid Services)**

The system was built entirely using free and open-source tools: OpenJDK 17, Apache Maven, JUnit 5.10.1, Mockito 5.7.0, and JaCoCo 0.8.11. No paid APIs, cloud subscriptions, or proprietary libraries were used at any point in development or runtime. This is particularly relevant for a personal budgeting tool — it would be counterproductive to impose recurring software costs on users who are trying to manage their finances. The zero-cost stack also ensures long-term sustainability, as no dependency on an external paid service can change its pricing model or cease to exist and break the application.

**Security and Privacy Considerations (Constraints C2 and C5 — Local Storage and No Data Sharing)**

All user financial data is stored exclusively in local CSV files (`data/transactions.csv`, `data/budgets.csv`) on the user's own machine. No network connections are made, no data is transmitted to external servers, and no third-party analytics or tracking libraries are included in the dependency tree. This was an explicit architectural decision to protect sensitive financial information. Transactions record only amounts, dates, descriptions, and categories — no user account identifiers or personally identifiable information are ever collected. This design ensures full compliance with data minimization principles and aligns with ethical software development practices for applications handling sensitive personal financial data.

**Reliability and Data Safety Considerations (Constraint C3 — Data Integrity Preservation)**

The system includes multiple safeguards against data loss and corruption. All model constructors validate inputs strictly — null fields, amounts below 0.01, empty descriptions, and oversized descriptions all throw `IllegalArgumentException` before any data is persisted, ensuring that no invalid record ever reaches the file system. The `BudgetDAO` upsert logic rewrites the entire budget file on every save, ensuring atomic updates and preventing partially written states. `FileManager.appendLine()` creates parent directories automatically on first write, preventing `FileNotFoundException` on initial startup. `TransactionDAO.loadAll()` skips malformed CSV rows rather than crashing, providing resilience against file corruption from unexpected shutdowns or external edits. All 38 DAO tests validate actual file I/O behavior using isolated `@TempDir` directories.

**Societal and Accessibility Considerations (Constraint C6 — Accessible Interface)**

The console-based interface provides clear text output and full keyboard navigation, making the system accessible to users who cannot use graphical interfaces due to hardware limitations or accessibility needs. All output uses structured, readable formatting — fixed-width tabular transaction lists, ASCII progress bar-style budget status displays, and labeled financial summaries. Error messages are descriptive and guide the user toward valid input rather than displaying cryptic error codes. The budget alert system (WARNING at ≥80% spending, EXCEEDED at >100%) gives users proactive, actionable financial awareness. From a broader societal perspective, providing individuals with clear visibility into their spending patterns supports financial literacy and responsible financial decision-making, which is the fundamental purpose of the system.

**Sustainability and Cross-Platform Compatibility (Constraint C4)**

Java's write-once-run-anywhere model means the application runs identically on Windows, macOS, and Linux without any platform-specific code changes. The system uses only the Java SE standard library with no operating system calls, native binaries, or platform-dependent file paths. Data is stored in plain-text CSV format, which is universally readable across all operating systems and software environments, ensuring long-term data accessibility without depending on proprietary formats. CSV also has a minimal storage footprint compared to binary or database formats, and allows users to inspect or migrate their financial history using any standard spreadsheet application.

**Ethical Considerations**

The system was designed to give users accurate and transparent information about their finances. Calculation correctness was treated as a safety concern throughout the project — a budget application that reports incorrect totals could cause users to make harmful financial decisions. This is why all financial computations (balance, savings rate, budget usage percentage, monthly and yearly aggregations) are covered by dedicated unit tests at the model layer (68 tests) and validated again through integration testing. The system avoids any gamification mechanics, engagement-maximizing notifications, or dark patterns that might encourage unnecessary spending, keeping its purpose focused entirely on informing and empowering the user.

---

#### 3.3.3 Test Cases and Results

The test suite for the Budget Management System was designed systematically to cover all layers of the architecture using multiple complementary testing techniques. All 279 test cases pass as of the final submission, confirmed by `mvn test` with BUILD SUCCESS. The suite is organized into six categories; full test case documentation, CFG diagrams, DU-pair tables, decision table rules, state diagrams, and use case scenarios are provided in [TESTING.md](TESTING.md).

**Summary Table**

| Testing Category | Test Files | Tests | Technique |
| ---------------- | ---------- | ----- | --------- |
| Model Unit Tests | TransactionTest, BudgetTest, ReportTest | 68 | JUnit 5, TDD |
| DAO Unit Tests | FileManagerTest, TransactionDAOTest, BudgetDAOTest | 38 | JUnit 5 with real @TempDir file I/O |
| Controller Unit Tests | TransactionControllerTest, BudgetControllerTest, ReportControllerTest | 65 | Mockito-based unit testing |
| Structural Tests | PathTestingTest, DataFlowTestingTest | 33 | CFG basis path coverage + data flow all-uses criterion |
| Integration Tests | TransactionIntegrationTest, ReportIntegrationTest | 14 | End-to-end real file I/O, no mocks |
| Validation Tests | BoundaryValueTest, EquivalenceClassTest, DecisionTableTest, StateTransitionTest, UseCaseTest | 61 | Five formal validation techniques |
| **Total** | **13 test files** | **279** | |

**Model Unit Tests (68 tests)**

The model layer was developed test-first using Test-Driven Development. Tests were written before any implementation to ensure each class met its contract from the start. `TransactionTest` (24 tests) covers constructor validation for all six fields, setter behavior, auto-generated UUID uniqueness, and the rule that equality is determined by ID only — not by field values. `BudgetTest` (26 tests) systematically checks `isExceeded()` above and below the limit, `isNearLimit()` at exactly 80%, at 79%, and at 81%, and `getUsagePercentage()` under various spending amounts. `ReportTest` (18 tests) covers positive and negative balance calculations, the zero-income guard in `getSavingsRate()` that returns 0.0 instead of throwing a divide-by-zero exception, and period label formatting.

**DAO Unit Tests (38 tests)**

All DAO tests use real file I/O — JUnit 5's `@TempDir` annotation creates a fresh temporary directory for each test that is automatically deleted afterward. This was a deliberate decision: mocking file I/O would allow tests to pass even if the CSV parsing logic was broken. `FileManagerTest` (13 tests) tests reading a non-existent file (returns empty list, does not throw), writing and re-reading, appending without overwriting, creating parent directories that do not yet exist, and the `fileExists`/`deleteFile` utilities. `TransactionDAOTest` (13 tests) verifies the complete save-reload round-trip, that the header row is skipped on load, that `deleteById` correctly rewrites the file excluding the deleted record, and that descriptions containing commas are correctly quoted and unquoted per RFC 4180. `BudgetDAOTest` (12 tests) confirms that saving the same category twice performs an upsert (replaces the existing row) rather than duplicating it, and that computed properties like `isExceeded()` survive a full persistence round-trip.

**Controller Unit Tests (65 tests)**

All three controller test classes use Mockito (`@ExtendWith(MockitoExtension.class)`) with DAOs injected as mocks via constructors — no file I/O occurs in any of these tests. `TransactionControllerTest` (24 tests) uses `ArgumentCaptor` to verify the exact CSV content emitted during `exportToCSV`, confirms that `removeTransaction` trims whitespace and handles null/empty IDs correctly, and verifies inclusive date boundary behavior in `filterByDateRange`. `BudgetControllerTest` (27 tests) verifies all four decision table rules: `updateSpending` is silently ignored when no budget exists (Rule 1); no alert fires below 80% (Rule 2); a WARNING fires at exactly 80% and remains WARNING even at exactly 100% because `isExceeded()` uses strictly greater-than (Rule 3); EXCEEDED fires at any amount above 100% (Rule 4). It also verifies `getRemainingBudget()` returns `Double.MAX_VALUE` as a sentinel for unlimited budget. `ReportControllerTest` (14 tests) verifies that monthly reports ignore other months and other years, that invalid month values (0 and 13) throw `IllegalArgumentException`, and that `generateCategoryBreakdown()` includes only EXPENSE transactions and correctly accumulates multiple entries in the same category.

**Structural Tests (33 tests)**

Structural testing targeted `TransactionController.filterByDateRange(LocalDate from, LocalDate to)` as the subject method. This method was chosen because it has a well-defined control flow graph with cyclomatic complexity V(G) = 5 (three decision nodes + 2), yielding five independent basis paths and twelve documentable def-use pairs — enough to apply both path testing and data flow analysis meaningfully without the combinatorial explosion that would come from targeting a more complex method.

`PathTestingTest` (10 tests) exercises all five McCabe basis paths: (1) `from` is null → `IllegalArgumentException`; (2) `to` is null → `IllegalArgumentException`; (3) `from` is after `to` → `IllegalArgumentException`; (4) valid range, DAO returns empty list → empty result returned; (5) valid range with transactions — testing a transaction inside the range (included), outside the range (excluded), and on the exact boundary dates (inclusive bounds confirmed on both sides). `DataFlowTestingTest` (23 tests) covers all twelve def-use pairs for variables `from`, `to`, `result`, `t` (the loop iteration variable), and `d` (the date extracted from each transaction), satisfying the all-uses coverage criterion.

**Integration Tests (14 tests)**

Integration tests use the real `FileManager`, real `TransactionDAO`, and real `BudgetDAO` with temporary directories — no mocking at any layer. `TransactionIntegrationTest` (8 tests) verifies the complete pipeline from controller to file and back: a transaction added via `TransactionController` appears in the CSV file; loading a fresh `TransactionDAO` instance from the same file returns the original transaction with all fields intact; `removeTransaction` rewrites the file correctly; `filterByCategory` and `filterByDateRange` work correctly against persisted data; `searchByDescription` performs case-insensitive matching on real stored data; `exportToCSV` produces a valid, re-parseable file with the correct header and data rows; and adding an expense transaction correctly triggers budget spending updates with the expected `isOverBudget` result. `ReportIntegrationTest` (6 tests) verifies that monthly and yearly report totals match the sum of all persisted transactions, that reports correctly ignore transactions outside the requested period, that category breakdowns accumulate correctly across multiple transactions in the same category, and that the budget alert thresholds fire correctly — WARNING at 95% spending and EXCEEDED at 150% spending — against real DAO-backed state.

**Validation Tests (61 tests)**

Five formal validation techniques were applied as required by the project specification:

- **Boundary Value Testing** (`BoundaryValueTest`, 15 tests, BV-01 to BV-15): Tests transaction amount at minimum (0.01, accepted), below minimum (0.00, rejected), and negative (rejected); description at 1 character (accepted), empty string (rejected), 100 characters (accepted), and 101 characters (rejected); `generateMonthlyReport` month parameter at 1 (accepted), 12 (accepted), 0 (rejected), and 13 (rejected); budget limit at a positive value (accepted), zero (rejected), and negative (rejected).

- **Equivalence Class Testing** (`EquivalenceClassTest`, 20 tests, EC-01 to EC-20): Inputs partitioned into valid and invalid classes across seven input domains — amount, description, category, transaction type, date, search keyword, and transaction ID. Each class is represented by exactly one test case: EC1 (valid positive amount), EC2 (zero amount), EC3 (negative amount), EC5–EC8 (description valid/null/whitespace/too long), EC9–EC10 (category valid/null), EC12 (type null), EC14 (date null), EC15–EC17 (keyword valid/null/empty), EC19–EC20 (remove ID null/empty).

- **Decision Table Testing** (`DecisionTableTest`, 9 tests, DT-Rule1 to DT-Mixed): Directly tests all four rules of the budget alert decision table using a real `BudgetController` with `@TempDir`-backed DAO. Includes boundary cases at exactly 80% (Rule 3 — WARNING), exactly 100% (still Rule 3 due to strictly greater-than in `isExceeded()`), and above 100% (Rule 4 — EXCEEDED). A mixed-category test verifies that three simultaneous budget categories at different thresholds each independently trigger the correct rule.

- **State Transition Testing** (`StateTransitionTest`, 9 tests, ST-Txn1–5 and ST-Budget1–4): Tests the lifecycle states of `Transaction` objects (created → amount updated → description updated → category updated → date updated) and `Budget` objects (unconfigured → limit set → spending below threshold → spending at WARNING → spending at EXCEEDED → spending reduced back to within limit), verifying that each state transition produces the correct computed property values.

- **Use Case Testing** (`UseCaseTest`, 8 tests, UC-01 to UC-08): Tests eight end-to-end user scenarios: adding an income transaction (UC-01), adding an expense transaction (UC-02), viewing transactions filtered by category (UC-03), viewing transactions filtered by date range (UC-04), generating a monthly report (UC-05), generating a category expense breakdown (UC-06), receiving a budget alert after spending exceeds the warning threshold (UC-07), and exporting transaction history to a CSV file (UC-08).

**Test Execution and Coverage**

All 279 tests were executed via `mvn test` and passed with BUILD SUCCESS. JaCoCo code coverage analysis confirmed ≥80% instruction coverage across all core business logic classes in the model, DAO, and controller layers. No test failures, errors, or flaky tests were observed during development or at final submission.

---

#### 3.3.4 Limitations

While the Budget Management System successfully meets all eight design constraints and all 279 tests pass, the following limitations were identified during development and testing, and are worth addressing in future iterations:

- **Console-only interface** — The system uses a text-based console menu, which limits usability for non-technical users. Adding a graphical or web-based interface is achievable in a future iteration without modifying any controller or model code, since the MVC separation already cleanly decouples View from business logic.

- **No file-level encryption** — Financial data is stored as plain-text CSV files on the local filesystem. While the system satisfies the local-storage and no-data-sharing constraints, the files are readable by anyone with filesystem access to the machine. Encrypting the data directory or integrating with OS-level access controls would improve security for users on shared machines.

- **Single-user, single-device only** — The system has no account management, authentication, or data synchronization capability. All data exists on one machine and is accessible to all users of that machine. This is appropriate for personal use on a trusted device, but limits applicability in shared-device or multi-device environments.

- **No recurring transaction support** — Every transaction must be entered manually. Common real-world scenarios involve recurring income (monthly salary, weekly allowance) and recurring expenses (rent, subscriptions, utilities). Automating recurring entries would significantly reduce data entry burden for daily use.

- **Budget periods do not reset automatically** — Budget limits are defined per category with no temporal scope. The `currentSpending` field accumulates indefinitely and is never automatically reset at the start of a new month or year. Users must manually delete and recreate budget entries to reset spending, which is inconvenient for monthly budgeting workflows.

- **Limited handling of edge-case CSV content** — While the system implements RFC 4180 quoting for descriptions containing commas, descriptions with embedded newline characters or other control characters are not fully handled and could produce malformed rows in the CSV file. This is unlikely in normal use but represents a known edge case in the persistence layer.

---

## 4. Team Work

Throughout the project, the team maintained continuous communication via a dedicated WhatsApp group, which served as the primary channel between formal meetings for sharing progress updates, discussing technical decisions, coordinating task handoffs, and resolving blockers quickly. All three members contributed to both development and documentation throughout the project lifecycle.

### 4.1 Meeting 1

**Time:** January 23, 2026, 5:00 pm – 7:00 pm

**Agenda:** Project Initiation and Scope Definition


| Team Member     | Previous Task | Completion State | Next Task                                                                               |
| --------------- | ------------- | ---------------- | --------------------------------------------------------------------------------------- |
| Nyabijek Gatdet | N/A           | N/A              | Research documentation requirements; draft initial REPORT.md structure (Sections 1 & 2) |
| Aubin Chriss    | N/A           | N/A              | Set up Maven project structure (pom.xml, JUnit 5, Mockito, JaCoCo); research testing framework integration      |
| Chop Peter Kur  | N/A           | N/A              | Research budgeting domain requirements; review software testing methodologies for Java  |


### 4.2 Meeting 2

**Time:** January 30, 2026, 5:00 pm – 8:00 pm

**Agenda:** Architecture Decision and Design Approach Review


| Team Member     | Previous Task                                      | Completion State | Next Task                                                                         |
| --------------- | -------------------------------------------------- | ---------------- | --------------------------------------------------------------------------------- |
| Nyabijek Gatdet | Draft REPORT.md Sections 1 & 2                     | 80%              | Finalize Sections 1 & 2; document Solution 1 and Solution 2 analysis (§3.1, §3.2) |
| Aubin Chriss    | Maven project setup and testing framework research | 100%             | Begin Model layer implementation (Transaction, Budget, Report classes) using TDD       |
| Chop Peter Kur  | Domain research and testing methodology review     | 100%             | Plan Data Access Object (DAO) layer design; research CSV persistence and file I/O strategy           |


### 4.3 Meeting 3

**Time:** February 12, 2026, 5:00 pm – 7:00 pm

**Agenda:** Model Layer Progress Review and DAO Layer Planning


| Team Member     | Previous Task                                        | Completion State | Next Task                                                                                |
| --------------- | ---------------------------------------------------- | ---------------- | ---------------------------------------------------------------------------------------- |
| Nyabijek Gatdet | Finalize §2 and §3.1/§3.2 in REPORT.md               | 100%             | Draft final solution write-up (§3.3); maintain meeting minutes and project documentation |
| Aubin Chriss    | Model layer implementation: Transaction, Budget, Report (68 unit tests) | 100%             | Begin DAO layer implementation (FileManager, TransactionDAO, BudgetDAO)                       |
| Chop Peter Kur  | DAO layer design and planning (FileManager, TransactionDAO, BudgetDAO)  | 70%              | Complete DAO layer implementation; prepare for collaborative controller development sprint |


### 4.4 Meeting 4

**Time:** March 10, 2026, 6:00 pm – 8:00 pm

**Agenda:** DAO Layer Review and Collaborative Controller Development Sprint


| Team Member     | Previous Task                                          | Completion State | Next Task                                                                                         |
| --------------- | ------------------------------------------------------ | ---------------- | ------------------------------------------------------------------------------------------------- |
| Nyabijek Gatdet | §3.3 final solution draft                                                        | 80%              | Begin validation test planning (boundary value, equivalence class, decision table, state transition, use case); review controller design for testing strategy |
| Aubin Chriss    | DAO layer complete: FileManager, TransactionDAO, BudgetDAO (38 tests, real file I/O) | 100%             | Collaborate on controller development – implement TransactionController with Mockito unit tests                  |
| Chop Peter Kur  | DAO layer completion support                                                     | 100%             | Collaborate on controller development – implement BudgetController and ReportController; plan View layer (ConsoleView, TransactionView, ReportView) |


### 4.5 Meeting 5

**Time:** March 21, 2026, 5:00 pm – 7:00 pm

**Agenda:** View Layer, Structural and Integration Testing Review and Validation Testing Kickoff


| Team Member     | Previous Task                                                                       | Completion State | Next Task                                                                                    |
| --------------- | ----------------------------------------------------------------------------------- | ---------------- | -------------------------------------------------------------------------------------------- |
| Nyabijek Gatdet | Validation test planning (boundary value, equivalence class, decision table)                                                              | 90%              | Implement validation tests: BoundaryValueTest, EquivalenceClassTest, DecisionTableTest          |
| Aubin Chriss    | TransactionController, BudgetController, ReportController tests complete (65 unit tests)                                                  | 100%             | Begin TESTING.md – structural testing documentation (CFG, path coverage, data flow analysis) |
| Chop Peter Kur  | View layer (ConsoleView, TransactionView, ReportView, Main.java), structural testing (33 tests), and integration testing (14 tests) complete | 100%             | Assist with TESTING.md integration test documentation; verify all 171 model, DAO, and controller tests pass   |


### 4.6 Meeting 6

**Time:** March 22, 2026, 5:00 pm – 7:00 pm

**Agenda:** Pre-deadline Coordination and Documentation Finalization


| Team Member     | Previous Task                                                               | Completion State | Next Task                                                                         |
| --------------- | --------------------------------------------------------------------------- | ---------------- | --------------------------------------------------------------------------------- |
| Nyabijek Gatdet | Validation testing partial: BoundaryValueTest, EquivalenceClassTest, DecisionTableTest complete | 70%              | Complete validation testing: StateTransitionTest and UseCaseTest (61 total validation tests) |
| Aubin Chriss    | TESTING.md structural testing section (CFG, path tests, data flow)          | 80%              | Complete TESTING.md structural section; review REPORT.md for consistency          |
| Chop Peter Kur  | TESTING.md integration test section                                         | 80%              | Complete TESTING.md integration section; confirm all 279 tests pass               |


### 4.7 Meeting 7

**Time:** March 27, 2026, 5:00 pm – 10:00 pm

**Agenda:** Final Report Completion, Full Project Review, and Submission

All outstanding tasks were completed during this meeting. The team collectively reviewed the full codebase (279 passing tests), finalized all REPORT.md sections, and confirmed the project was ready for submission.


| Team Member     | Previous Task                          | Completion State | Next Task                                                                                                    |
| --------------- | -------------------------------------- | ---------------- | ------------------------------------------------------------------------------------------------------------ |
| Nyabijek Gatdet | Validation testing complete: boundary value, equivalence class, decision table, state transition, use case (61 tests) | 100%             | Finalize REPORT.md §3.3–§6; complete conclusion, limitations, and all remaining placeholders — **Completed** |
| Aubin Chriss    | TESTING.md structural content          | 100%             | Complete REPORT.md review; JaCoCo coverage verification (≥80% confirmed) — **Completed**                     |
| Chop Peter Kur  | TESTING.md complete                    | 100%             | Final code review; GitHub repository cleanup and project submission — **Completed**                          |


---

## 5. Project Management

The project spanned ten calendar weeks from January 23 to March 27, 2026. Tasks were distributed based on team roles: Aubin led project setup, model layer, and DAO layer implementation; the full team collaborated on the controller layer; Chop led the view layer, structural testing, and integration testing; and Nyabijek led validation testing and documentation. The Gantt chart below maps each task to the active week(s), and the task table details dependencies, durations, slack, and critical path.

**Week Reference:** W1 = Jan 19–23 · W2 = Jan 26–30 · W3 = Feb 2–6 · W4 = Feb 9–13 · W5 = Feb 16–20 · W6 = Feb 23–27 · W7 = Mar 2–6 · W8 = Mar 9–13 · W9 = Mar 16–20 · W10 = Mar 23–27

**Key Milestones:** ★ W2 = D2 due (Jan 30) · ★ W4 = D3 due (Feb 13) · ★ W10 = D4 due (Mar 27)

### Gantt Chart

| ID | Task | W1 | W2 | W3 | W4 | W5 | W6 | W7 | W8 | W9 | W10 |
|----|------|----|----|----|----|----|----|----|----|----|-----|
| T1 | Maven setup & pom.xml config | ■ | | | | | | | | | |
| T2 | Directory structure & .gitignore | ■ | | | | | | | | | |
| T3 | Problem definition (REPORT §2.1) | ■ | ■ | | | | | | | | |
| T4 | Design requirements – functions & objectives (§2.2.1–2.2.2) | | ■ | | | | | | | | |
| T5 | Design requirements – constraints table (§2.2.3) | | ■ | | | | | | | | |
| T6 | Solution 1 analysis – monolithic approach (§3.1) | | ■ | ■ | | | | | | | |
| T7 | Solution 2 analysis – two-tier architecture (§3.2) | | | ■ | | | | | | | |
| T8 | MVC architecture decision & formal comparison | | | ■ | ■ | | | | | | |
| T9 | Model enums: TransactionType & Category | | ■ | | | | | | | | |
| T10 | Model: Transaction class with TDD (24 tests) | | ■ | ■ | | | | | | | |
| T11 | Model: Budget class with threshold tests (26 tests) | | | ■ | | | | | | | |
| T12 | Model: Report class with edge-case tests (18 tests) | | | ■ | | | | | | | |
| T13 | DAO: FileManager CSV read/write/append (13 tests) | | | ■ | ■ | | | | | | |
| T14 | DAO: TransactionDAO with real file I/O tests (13 tests) | | | | ■ | ■ | | | | | |
| T15 | DAO: BudgetDAO with upsert logic tests (12 tests) | | | | ■ | ■ | | | | | |
| T16 | Controller: TransactionController + Mockito (24 tests) | | | | | ■ | ■ | | | | |
| T17 | Controller: BudgetController + Decision Table (27 tests) | | | | | ■ | ■ | | | | |
| T18 | Controller: ReportController + report tests (14 tests) | | | | | ■ | | | | | |
| T19 | View: ConsoleView with injected Scanner/PrintStream | | | | | | | ■ | | | |
| T20 | View: TransactionView & ReportView tabular display | | | | | | | ■ | | | |
| T21 | Main.java: MVC wiring & application loop | | | | | | | ■ | ■ | | |
| T22 | Structural: CFG diagram & cyclomatic complexity (V(G)=5) | | | | | | | ■ | ■ | | |
| T23 | Structural: path testing – PathTestingTest (10 tests) | | | | | | | | ■ | | |
| T24 | Structural: data flow DU pairs – DataFlowTestingTest (23 tests) | | | | | | | | ■ | ■ | |
| T25 | Integration: TransactionIntegrationTest (8 tests) | | | | | | | | ■ | | |
| T26 | Integration: ReportIntegrationTest (6 tests) | | | | | | | | ■ | | |
| T27 | Validation: boundary value testing (15 tests) | | | | | | | | ■ | ■ | |
| T28 | Validation: equivalence class testing (20 tests) | | | | | | | | | ■ | |
| T29 | Validation: decision table testing (9 tests) | | | | | | | | ■ | ■ | |
| T30 | Validation: state transition testing (9 tests) | | | | | | | | | ■ | |
| T31 | Validation: use case testing (8 tests) | | | | | | | | | ■ | |
| T32 | TESTING.md: structural testing documentation | | | | | | | | | ■ | ■ |
| T33 | TESTING.md: integration & validation documentation | | | | | | | | | ■ | ■ |
| T34 | REPORT.md §3.3: final solution write-up & components | | | | | | | | | ■ | ■ |
| T35 | REPORT.md §3.3.2–§3.3.3: considerations & test results | | | | | | | | | | ■ |
| T36 | REPORT.md §3.3.4 limitations & §6 conclusion | | | | | | | | | | ■ |
| T37 | JaCoCo coverage verification (≥80% confirmed) | | | | | | | | | ■ | ■ |
| T38 | Final code review & project submission preparation | | | | | | | | | | ■ |

### Task Table

| Task | Description | Predecessor | Duration (days) | Slack (days) | Critical Path? |
|------|-------------|-------------|-----------------|--------------|----------------|
| T1 | Maven project setup, pom.xml (JUnit 5.10.1, Mockito 5.7.0, JaCoCo 0.8.11) | — | 3 | 0 | Yes |
| T2 | Repository & directory structure (src/main, src/test, README, .gitignore) | T1 | 2 | 2 | No |
| T3 | Problem definition, background research, draft REPORT.md §2.1 | — | 3 | 5 | No |
| T4 | Design requirements – functions & objectives (REPORT §2.2.1–2.2.2) | T3 | 3 | 5 | No |
| T5 | Design requirements – constraints table with 8 constraints (REPORT §2.2.3) | T3 | 2 | 6 | No |
| T6 | Evaluate Solution 1: monolithic procedural approach (REPORT §3.1) | T4 | 3 | 5 | No |
| T7 | Evaluate Solution 2: two-tier layered architecture (REPORT §3.2) | T6 | 3 | 5 | No |
| T8 | MVC architecture decision – formal comparison & testing justification | T7 | 2 | 5 | No |
| T9 | Model enums: TransactionType (INCOME/EXPENSE), Category (7 values) | T1 | 2 | 0 | Yes |
| T10 | Model: Transaction class, constructor validation, UUID generation (24 tests) | T9 | 4 | 0 | Yes |
| T11 | Model: Budget class, isExceeded/isNearLimit/80% threshold logic (26 tests) | T9 | 4 | 1 | No |
| T12 | Model: Report class, getBalance/getSavingsRate/zero income guard (18 tests) | T9 | 3 | 2 | No |
| T13 | DAO: FileManager CSV read/write/append/delete, parent dir creation (13 tests) | T10, T11, T12 | 4 | 0 | Yes |
| T14 | DAO: TransactionDAO save/load/delete/findBy, RFC 4180 quoting (13 tests) | T13 | 4 | 0 | Yes |
| T15 | DAO: BudgetDAO upsert logic, findByCategory, round-trip persistence (12 tests) | T13 | 3 | 1 | No |
| T16 | Controller: TransactionController, filterByDateRange, Mockito mocks (24 tests) | T14, T15 | 5 | 0 | Yes |
| T17 | Controller: BudgetController, Decision Table Rules 1–4, alert logic (27 tests) | T15 | 5 | 1 | No |
| T18 | Controller: ReportController, monthly/yearly/category breakdown (14 tests) | T14 | 3 | 3 | No |
| T19 | View: ConsoleView, injected Scanner/PrintStream, all input-read methods | T16, T17 | 4 | 0 | Yes |
| T20 | View: TransactionView tabular display, ReportView budget status & alerts | T16, T17 | 3 | 1 | No |
| T21 | Main.java: MVC wiring, console dispatch loop, parseCategory/parseType | T19, T20 | 3 | 0 | Yes |
| T22 | Structural: CFG diagram of filterByDateRange, cyclomatic complexity V(G)=5 | T16 | 2 | 4 | No |
| T23 | Structural: path testing – 5 independent basis paths (PathTestingTest, 10 tests) | T22 | 3 | 3 | No |
| T24 | Structural: data flow analysis – 12 DU pairs, all-uses criterion (23 tests) | T23 | 4 | 2 | No |
| T25 | Integration: TransactionController ↔ TransactionDAO ↔ FileManager (8 tests) | T14, T16 | 3 | 3 | No |
| T26 | Integration: ReportController ↔ BudgetController ↔ DAOs (6 tests) | T17, T18 | 3 | 3 | No |
| T27 | Validation: boundary value testing – amount, description, month ranges (15 tests) | T16, T17 | 3 | 2 | No |
| T28 | Validation: equivalence class testing – valid/invalid partitions (20 tests) | T27 | 3 | 2 | No |
| T29 | Validation: decision table testing – BudgetController Rules 1–4 (9 tests) | T17 | 2 | 4 | No |
| T30 | Validation: state transition testing – Transaction & Budget lifecycles (9 tests) | T27 | 2 | 3 | No |
| T31 | Validation: use case testing – UC-01 to UC-08 end-to-end scenarios (8 tests) | T25, T26 | 2 | 3 | No |
| T32 | TESTING.md: structural testing section (CFG, paths, DU pair tables) | T24 | 2 | 3 | No |
| T33 | TESTING.md: integration & validation sections (scenario tables, state diagrams) | T31 | 2 | 2 | No |
| T34 | REPORT.md §3.3: final solution write-up, components table, block diagram | T8, T21 | 3 | 0 | Yes |
| T35 | REPORT.md §3.3.2–§3.3.3: considerations & test suite results summary | T34, T33 | 2 | 0 | Yes |
| T36 | REPORT.md §3.3.4 limitations & §6 conclusion and future work | T35 | 1 | 0 | Yes |
| T37 | JaCoCo coverage verification, 279/279 tests confirmed passing | T31 | 2 | 2 | No |
| T38 | Final code review, report proofreading & GitHub submission preparation | T36, T37, T32 | 1 | 0 | Yes |

**Critical Path: T1 → T9 → T10 → T13 → T14 → T16 → T19 → T21 → T34 → T35 → T36 → T38**
Total critical path duration: 36 working days


---

## 6. Conclusion and Future Work

### 6.1 Conclusion

This project built and tested a Budget Management System applying the testing methods covered in ENSE 375. The biggest lesson was that testability has to be part of the design from the start, not added at the end.

Using a four-layer MVC architecture made testing much easier. Each layer could be tested in isolation: models needed no mocking, DAOs were tested with real files in temporary folders, and controllers were tested with Mockito mocks that kept the file system out of unit tests entirely. This separation was only possible because of constructor-based dependency injection.

For structural testing, we picked `TransactionController.filterByDateRange()` and built a control flow graph, calculated cyclomatic complexity (V(G) = 5), ran five basis path tests, and traced twelve def-use pairs. The data flow analysis caught edge cases in boundary date matching that black-box testing alone would likely have missed.

The five validation techniques (Boundary Value, Equivalence Class, Decision Table, State Transition, and Use Case) each checked the system from a different angle. The Decision Table was particularly useful for the budget alert logic, where mapping the four rules to a truth table revealed a key detail: `isExceeded()` uses strictly greater-than, so spending at exactly 100% is a warning, not exceeded.

All 279 tests passed, all eight design constraints were satisfied, and JaCoCo confirmed over 80% instruction coverage across the model, DAO, and controller layers. The project finished on schedule over ten weeks.

### 6.2 Future Work

A few areas stand out for improvement:

**Graphical interface.** The console works, but a JavaFX or web-based UI would make the system usable for non-technical users. Because the View layer is already separate in MVC, only new View code would be needed.

**Data encryption.** Storing financial data as plain text CSV is a risk on shared machines. Encrypting the data folder with AES-256 or using the OS keychain would be a practical improvement.

**Multi-user support.** Right now the system is single-user. Adding accounts and optional data sync would make it useful for households. The DAO layer could switch to SQLite without changing any controller or model code.

**Recurring transactions.** Monthly rent, weekly groceries, and similar fixed expenses currently need to be entered by hand every time. A template-based recurring transaction feature would reduce that burden.

**Monthly budget reset.** Spending never resets. A real budgeting tool should reset to zero at the start of each month, either automatically or on user request.

**Better CSV handling.** The current parser handles commas but not newlines inside field values. Switching to Apache Commons CSV would cover the full CSV spec and remove the manual quoting code in `TransactionDAO`.

**More export formats.** Adding PDF and Excel export alongside the existing CSV option would let users share reports more easily without touching any business logic.

---

## 7. References

[1] T. J. McCabe, "A complexity measure," *IEEE Transactions on Software Engineering*, vol. SE-2, no. 4, pp. 308–320, Dec. 1976. doi: 10.1109/TSE.1976.233837

[2] Y. Shafranovich, "Common Format and MIME Type for Comma-Separated Values (CSV) Files," IETF RFC 4180, Internet Engineering Task Force, Oct. 2005. [Online]. Available: https://www.rfc-editor.org/rfc/rfc4180

[3] S. Bechtold, B. Stein, M. Phillipps, et al., *JUnit 5 User Guide*, version 5.10.1, JUnit Team, 2023. [Online]. Available: https://junit.org/junit5/docs/current/user-guide/

[4] Mockito Contributors, *Mockito Framework Site*, version 5.7.0, 2023. [Online]. Available: https://site.mockito.org/

[5] JaCoCo Team, *JaCoCo Java Code Coverage Library*, version 0.8.11, Mountainminds GmbH & Co. KG, 2023. [Online]. Available: https://www.jacoco.org/jacoco/

---

## 8. Appendix

This appendix provides supplementary structural and behavioral diagrams for the Budget Management System. Full test case tables, DU-pair listings, decision table rule matrices, and use case scenario flows are documented in [TESTING.md](TESTING.md).

---

### A.1 Control Flow Graph — `filterByDateRange(LocalDate from, LocalDate to)`

*Fig. A.1 — Control Flow Graph for `TransactionController.filterByDateRange()`. This method was selected as the structural testing target due to its well-bounded scope and a cyclomatic complexity of V(G) = 5, derived from four predicate nodes. See TESTING.md §3.1 for the full basis path table and all 10 path test cases.*

```
N1: ENTRY — receive (from, to)
     │
N2: DECISION — from == null || to == null?
     │ TRUE ──────────────────────────────► N3: throw IllegalArgumentException
     │ FALSE
N4: DECISION — from.isAfter(to)?
     │ TRUE ──────────────────────────────► N5: throw IllegalArgumentException
     │ FALSE
N6: result = new ArrayList<>()
     │
N7: DECISION — more elements in dao.loadAll()?  ◄─────────────────┐
     │ FALSE ─────────────────────────────► N10: return result     │
     │ TRUE                                                         │
N8: DECISION — !d.isBefore(from) && !d.isAfter(to)?               │
     │ FALSE ──────────────────────────────────────────────────────┘
     │ TRUE
N9: result.add(t) ──────────────────────────────────────────────────┘
```

**Cyclomatic Complexity:** V(G) = 4 predicate nodes + 1 = **5**

**Five Basis Paths:**
| Path | Route | Trigger |
|------|-------|---------|
| P1 | N1→N2(T)→N3 | `from` is null → `IllegalArgumentException` |
| P2 | N1→N2(T)→N3 | `to` is null → `IllegalArgumentException` |
| P3 | N1→N2(F)→N4(T)→N5 | both non-null; `from` is after `to` → `IllegalArgumentException` |
| P4 | N1→N2(F)→N4(F)→N6→N7(F)→N10 | valid range; DAO returns empty list → empty list |
| P5 | N1→N2(F)→N4(F)→N6→N7(T)→N8(T)→N9→N7→N10 | valid range; transaction in range → included in result |

*Note: P1 and P2 traverse the same structural route (N2 TRUE branch) because both null inputs are handled by a single compound `||` guard. They are listed separately to ensure both sub-conditions of the compound predicate are explicitly exercised.*

---

### A.2 State Transition Diagrams

#### Transaction Object Lifecycle

*Fig. A.2 — State diagram for a `Transaction` object. All state transitions are triggered by validated setter calls. The object cannot enter an invalid state because each setter throws `IllegalArgumentException` for invalid inputs before modifying any field.*

```
                        ┌────────────────────────────────────────┐
                        │           [CREATED]                    │
                        │  id: UUID auto-generated               │
                        │  amount, date, description,            │
                        │  category, type: validated in ctor     │
                        └────────────────────────────────────────┘
                          │           │           │           │
               setAmount()│  setDate()│  setDesc()│  setCat() │
                  (≥0.01) │  (non-null│  (1-100ch)│  (non-null│
                          ▼  )        ▼  )        ▼  )        ▼
              ┌───────────┐ ┌─────────┐ ┌────────┐ ┌──────────┐
              │  AMOUNT   │ │  DATE   │ │  DESC  │ │ CATEGORY │
              │  UPDATED  │ │ UPDATED │ │UPDATED │ │  UPDATED │
              └───────────┘ └─────────┘ └────────┘ └──────────┘
                                │
                           (object remains in CREATED state;
                            setters update individual fields,
                            not overall lifecycle state)
```

**Note:** `Transaction` is a mutable value object. All setter operations return to the same `CREATED` state with updated field values. The only terminal state is garbage collection after all references are dropped. The ID field is immutable after construction (no setter exists).

#### Budget Object Lifecycle

*Fig. A.3 — State diagram for a `Budget` object. State is determined by `currentSpending` relative to `limit`. Transitions occur whenever `setCurrentSpending()` is called.*

```
              ┌─────────────────────────────────────────────────────────────┐
              │                  [UNCONFIGURED]                             │
              │  No budget entry exists for this category in BudgetDAO     │
              └─────────────────────────────────────────────────────────────┘
                                        │
                              setBudgetLimit() called
                              (limit > 0, spending = 0)
                                        │
                                        ▼
              ┌─────────────────────────────────────────────────────────────┐
              │                    [NORMAL]                                 │
              │  currentSpending < 0.80 × limit                            │
              │  isNearLimit() = false                                      │
              │  isExceeded() = false                                       │
              └─────────────────────────────────────────────────────────────┘
                    │                                    ▲
        spending ≥ 80% limit                 spending reduced below 80%
                    │                                    │
                    ▼                                    │
              ┌─────────────────────────────────────────────────────────────┐
              │                  [WARNING]                                  │
              │  currentSpending ≥ 0.80 × limit                            │
              │  AND currentSpending ≤ limit                                │
              │  isNearLimit() = true                                       │
              │  isExceeded() = false                                       │
              │  Alert: "Budget WARNING for <category>"                     │
              └─────────────────────────────────────────────────────────────┘
                    │                                    ▲
          spending > 100% limit              spending reduced to ≤ limit
                    │                                    │
                    ▼                                    │
              ┌─────────────────────────────────────────────────────────────┐
              │                  [EXCEEDED]                                 │
              │  currentSpending > limit  (strictly greater-than)          │
              │  isNearLimit() = true                                       │
              │  isExceeded() = true                                        │
              │  Alert: "Budget EXCEEDED for <category>"                   │
              └─────────────────────────────────────────────────────────────┘
```

**Key Design Note:** The transition from WARNING to EXCEEDED requires `currentSpending > limit` (strictly greater-than), not `≥`. This means spending of exactly 100% of the limit remains in WARNING state. This boundary was explicitly tested in `BudgetControllerTest` and `DecisionTableTest` (DT-Rule3-100-percent).

---

### A.3 Budget Alert Decision Table

*The four-rule decision table governing `BudgetController.updateSpending()` alert behavior. This table was used directly to derive the nine test cases in `DecisionTableTest`.*

| | Rule 1 | Rule 2 | Rule 3 | Rule 4 |
|---|--------|--------|--------|--------|
| **Condition: Budget exists for category** | No | Yes | Yes | Yes |
| **Condition: currentSpending ≥ 80% of limit** | — | No | Yes | Yes |
| **Condition: currentSpending > 100% of limit** | — | No | No | Yes |
| **Action: Update spending** | No-op | Yes | Yes | Yes |
| **Action: Fire WARNING alert** | No | No | Yes | No |
| **Action: Fire EXCEEDED alert** | No | No | No | Yes |

**Test Cases Derived:**
- DT-Rule1: No budget → update ignored, no alert
- DT-Rule2: Budget exists, 50% spending → updated, no alert
- DT-Rule3-below: Budget exists, 79% spending → WARNING not fired
- DT-Rule3-at80: Budget exists, exactly 80% spending → WARNING fired
- DT-Rule3-at100: Budget exists, exactly 100% spending → WARNING (not EXCEEDED — strictly greater-than)
- DT-Rule4: Budget exists, 101%+ spending → EXCEEDED fired
- DT-Mixed: Three categories simultaneously at Rules 1, 3, and 4 respectively — each triggers independently

---

*ENSE 375 – Software Testing and Validation | University of Regina*