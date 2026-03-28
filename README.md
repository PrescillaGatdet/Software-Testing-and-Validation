# Budget Management System

A console-based personal finance app built for **ENSE 375 : Software Testing and Validation** at the University of Regina. The app lets you record income and expenses, set category budgets, generate monthly and yearly reports, and get alerts when you are getting close to or have exceeded a budget limit. Everything is saved locally to CSV files so your data persists between sessions.

The main point of this project was to build and design an optimal test suite for it. We applied ten different testing techniques across 279 test cases, all passing.

**Team:** Nyabijek Gatdet · Chop Peter Kur · Aubin Chriss Izere

---

## What the app does

- Add income or expense transactions with an amount, date, description, and category
- Organize transactions under seven categories: Food, Transport, Entertainment, Utilities, Salary, Investment, Other
- Set a monthly spending limit per category
- Get a WARNING when you hit 80% of a budget limit, and an EXCEEDED alert when you go over
- View a monthly or yearly financial report (income, expenses, balance, savings rate)
- Filter transactions by category or date range, or search by description keyword
- Export your transaction history to a CSV file

---

## Tech stack


| Tool           | Version | Purpose                                 |
| -------------- | ------- | --------------------------------------- |
| Java (OpenJDK) | 17      | Application language                    |
| Apache Maven   | 3.x     | Build and dependency management         |
| JUnit Jupiter  | 5.10.1  | Test framework                          |
| Mockito        | 5.7.0   | Mocking for unit tests                  |
| JaCoCo         | 0.8.11  | Code coverage reporting and enforcement |
| CSV (RFC 4180) | —       | Local data persistence                  |


---

## Project objectives

We had two goals running in parallel throughout the project:

1. Build a functional budget management application that handles real user workflows recording transactions, tracking budgets, generating reports with proper input validation and reliable file storage.
2. Design a comprehensive test suite that covers the application at every level: unit tests for each layer in isolation, structural tests with control flow graphs and data flow analysis, integration tests using real file I/O, and five formal validation techniques (boundary value, equivalence class, decision table, state transition, and use case testing).

The design of the architecture was driven by the testing requirements, not the other way around.

---

## How it works

The application follows a four-layer MVC architecture. Each layer has a single responsibility and no cross-layer dependencies, which is what makes the testing strategy work.

```
┌─────────────────────────────────────────────────────┐
│  VIEW LAYER — ConsoleView, TransactionView,         │
│               ReportView                            │
│  Handles all console output and user input          │
├─────────────────────────────────────────────────────┤
│  CONTROLLER LAYER — TransactionController,          │
│                     BudgetController,               │
│                     ReportController                │
│  All business logic lives here                      │
├─────────────────────────────────────────────────────┤
│  DAO LAYER — TransactionDAO, BudgetDAO,             │
│              FileManager                            │
│  Reads and writes CSV files                         │
├─────────────────────────────────────────────────────┤
│  MODEL LAYER — Transaction, Budget, Report,         │
│                Category, TransactionType            │
│  Plain data classes with validation                 │
└─────────────────────────────────────────────────────┘
              ↓  stored in  ↓
         data/transactions.csv
         data/budgets.csv
```

Controllers receive their DAO dependencies through the constructor (dependency injection), which lets unit tests swap in Mockito mocks instead of hitting the real file system. Integration tests use JUnit's `@TempDir` to run against real temporary files that get cleaned up automatically.

---

## Folder structure

```
Software-Testing-and-Validation/
│
├── data/                              # Runtime data folder (auto-created on first run)
│   └── .gitkeep                       # Keeps the folder in git; actual CSVs are gitignored
│
├── src/
│   ├── main/java/com/budgetmanager/
│   │   │
│   │   ├── model/
│   │   │   ├── Transaction.java       # A single income or expense record (UUID, amount, date, description, category, type)
│   │   │   ├── Budget.java            # A spending limit for one category; tracks current spending and alert thresholds
│   │   │   ├── Report.java            # Immutable summary of income/expenses for a time period
│   │   │   ├── Category.java          # Enum: FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, SALARY, INVESTMENT, OTHER
│   │   │   └── TransactionType.java   # Enum: INCOME or EXPENSE
│   │   │
│   │   ├── dao/
│   │   │   ├── FileManager.java       # Low-level file I/O — reads lines, writes lines, appends lines
│   │   │   ├── TransactionDAO.java    # Converts Transaction objects to/from CSV rows (RFC 4180 compliant)
│   │   │   └── BudgetDAO.java         # Converts Budget objects to/from CSV rows
│   │   │
│   │   ├── controller/
│   │   │   ├── TransactionController.java  # Add, remove, filter, search, and export transactions
│   │   │   ├── BudgetController.java       # Set limits, track spending, generate WARNING/EXCEEDED alerts
│   │   │   └── ReportController.java       # Generate monthly/yearly reports and category breakdowns
│   │   │
│   │   ├── view/
│   │   │   ├── ConsoleView.java       # Main menu, input prompts, and user feedback messages
│   │   │   ├── TransactionView.java   # Formats and prints transaction tables and detail views
│   │   │   └── ReportView.java        # Prints financial reports, budget progress bars, and alerts
│   │   │
│   │   └── Main.java                  # Entry point — wires all layers together and runs the menu loop
│   │
│   └── test/java/com/budgetmanager/
│       │
│       ├── model/
│       │   ├── TransactionTest.java        # 24 unit tests — constructor validation, getters, setters, equals
│       │   ├── BudgetTest.java             # 26 unit tests — alert thresholds, usage percentage, remaining budget
│       │   └── ReportTest.java             # 18 unit tests — balance calculation, savings rate, period label
│       │
│       ├── dao/
│       │   ├── FileManagerTest.java        # 13 unit tests — read/write/append/delete with real temp files
│       │   ├── TransactionDAOTest.java     # 13 unit tests — CSV save/load/delete, round-trip integrity
│       │   └── BudgetDAOTest.java          # 12 unit tests — budget persistence, upsert behaviour
│       │
│       ├── controller/
│       │   ├── TransactionControllerTest.java  # 24 unit tests — Mockito mocks, no file I/O
│       │   ├── BudgetControllerTest.java       # 27 unit tests — alert logic, spending accumulation
│       │   └── ReportControllerTest.java       # 14 unit tests — report aggregation, category breakdown
│       │
│       ├── structural/
│       │   ├── PathTestingTest.java        # 10 tests — basis path coverage (V(G)=5) of filterByDateRange()
│       │   └── DataFlowTestingTest.java    # 23 tests — all-uses coverage of 12 def-use pairs
│       │
│       ├── integration/
│       │   ├── TransactionIntegrationTest.java # 8 tests — full stack with real file I/O via @TempDir
│       │   └── ReportIntegrationTest.java      # 6 tests — report and budget subsystems end-to-end
│       │
│       └── validation/
│           ├── BoundaryValueTest.java      # 15 tests — min/max/just-outside for amount, description, month, budget
│           ├── EquivalenceClassTest.java   # 20 tests — valid and invalid partitions for every input parameter
│           ├── DecisionTableTest.java      # 9 tests  — four-rule alert decision table for BudgetController
│           ├── StateTransitionTest.java    # 9 tests  — transaction and budget lifecycle state machines
│           └── UseCaseTest.java            # 8 tests  — eight end-to-end user scenarios (UC-01 to UC-08)
│
├── pom.xml                            # Maven build config — dependencies, JaCoCo coverage enforcement
├── .gitignore                         # Excludes data/*.csv, target/, and IDE files from version control
├── README.md                          # This file
├── REPORT.md                          # Full project report (design process, architecture, conclusions)
└── TESTING.md                         # Test plan (CFG diagrams, decision tables, use cases, full test index)
```

---

## Getting started

### Prerequisites

You need Java 17 or newer and Maven 3.x installed.

**Check if you already have them:**

```bash
java -version
mvn -version
```

**If not:**

- Java 17: [https://adoptium.net](https://adoptium.net) ; download the Temurin 17 LTS installer for your OS
- Maven: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)  ; or use your package manager (`brew install maven` on Mac, `sudo apt install maven` on Linux)

### Clone the repo

```bash
git clone https://github.com/your-username/Software-Testing-and-Validation.git
cd Software-Testing-and-Validation
```

### Build and run the app

```bash
mvn package -DskipTests
java -jar target/budget-management-system-1.0-SNAPSHOT.jar
```

You will see a menu like this:

```
========================================
     Budget Management System
========================================
1.  Add Transaction
2.  View All Transactions
3.  Filter by Category
4.  Filter by Date Range
5.  Search by Description
6.  Set Budget Limit
7.  View Budget Status
8.  Check Budget Alerts
9.  Generate Monthly Report
10. Generate Yearly Report
11. Category Expense Breakdown
0.  Exit
```

Your data is saved automatically to `data/transactions.csv` and `data/budgets.csv` in the project folder. The `data/` directory is created on the first run if it does not exist.

---

## Running the tests

```bash
mvn test
```

That runs all 279 tests. A successful run looks like this:

```
[INFO] Results:

[INFO] Tests run: 279, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
```

### Run a specific test class

```bash
mvn test -Dtest=BudgetControllerTest
```

### Get the code coverage report

```bash
mvn test
# then open: target/site/jacoco/index.html
```

JaCoCo is configured to enforce at least 70% instruction coverage. The core business layers (model, DAO, controller) exceed 80%.

---

## Test suite overview


| Category                | Tests   | Technique                         |
| ----------------------- | ------- | --------------------------------- |
| Model unit tests        | 68      | JUnit 5, TDD                      |
| DAO unit tests          | 38      | JUnit 5, real file I/O            |
| Controller unit tests   | 65      | JUnit 5 + Mockito                 |
| Structural tests        | 33      | Path testing, data flow analysis  |
| Integration tests       | 14      | Full stack with @TempDir          |
| Boundary value tests    | 15      | Min/max/just-outside boundaries   |
| Equivalence class tests | 20      | Valid/invalid input partitions    |
| Decision table tests    | 9       | Four-rule budget alert table      |
| State transition tests  | 9       | Transaction and budget lifecycles |
| Use case tests          | 8       | End-to-end user scenarios         |
| **Total**               | **279** |                                   |


For the full test plan including CFG diagrams, data flow tables, decision tables, and use case definitions, see [TESTING.md](TESTING.md).

---

## Further reading

- [REPORT.md](REPORT.md) — Full project report covering the design problem, three architecture iterations, final MVC design, team work, and conclusions.
- [TESTING.md](TESTING.md) — Complete test plan with all technical details: control flow graph, cyclomatic complexity, def-use pairs, equivalence classes, decision table, state diagrams, and use cases.

