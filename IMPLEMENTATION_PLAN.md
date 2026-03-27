# ENSE 375 – Budget Management System
## Implementation Plan — Progress Tracker & Next Steps

**Team:** Nyabijek Gatdet · Chop Peter Kur · Aubin Chriss
**Course:** ENSE 375 – Software Testing and Validation, University of Regina
**Final Due Date:** April 10, 2026, 23:59:59

---

## Current Status at a Glance

| Stage | Description | Status | Tests |
|-------|-------------|--------|-------|
| Stage 0 | Project Setup (Maven, JUnit 5, Mockito, JaCoCo) | ✅ DONE | — |
| Stage 1 | Model Layer (Transaction, Budget, Report, enums) | ✅ DONE | 68 tests |
| Stage 2 | DAO Layer (FileManager, TransactionDAO, BudgetDAO) | ✅ DONE | 38 tests |
| Stage 3 | Controller Layer (business logic + Mockito unit tests) | ✅ DONE | 65 tests |
| Stage 4 | View Layer (ConsoleView, TransactionView, ReportView) + Main.java | ✅ DONE | tested via Stage 3 |
| Stage 5 | Structural Testing (path testing + data flow testing) | ✅ DONE | 33 tests |
| Stage 6 | Integration Testing (real file I/O across layers) | ✅ DONE | 14 tests |
| Stage 7 | Validation Testing (all 5 techniques) | ⏳ TODO | 0 tests |
| Stage 8 | Documentation (REPORT.md §3.3 + TESTING.md) | ⏳ TODO | — |
| Stage 9 | Coverage ≥ 80%, comments, final commit | ⏳ TODO | — |

**Total tests passing right now: 218 / 218 — BUILD SUCCESS**

---

## Deliverable Status

| Deliverable | Section | Due | Weight | Status |
|-------------|---------|-----|--------|--------|
| D1 — Problem Definition | REPORT.md §2.1 | Jan 23 | 10% | ✅ submitted |
| D2 — Constraints & Requirements | REPORT.md §2.2 | Jan 30 | 10% | ✅ submitted |
| D3 — Iterative Design (Solutions 1 & 2) | REPORT.md §3.1–3.2 | Feb 13 | 10% | ✅ submitted |
| D4 — Final Design, Implementation & Testing | REPORT.md §3.3 | March 27 | 60% | ⏳ in progress |
| D5 — Teamwork & Communication | REPORT.md §4–5 | April 10 | 10% | ⏳ TODO |

---

## Tech Stack

| Tool | Version | Why |
|------|---------|-----|
| Java | 17 | Cross-platform, JUnit-native, MVC-idiomatic |
| Maven | 3.x | Dependency management, single `mvn test` command |
| JUnit 5 | 5.10.1 | Required by professor; JUnit Jupiter annotations |
| Mockito | 5.7.0 | Mock injection for controller unit tests |
| JaCoCo | 0.8.11 | Code coverage reports (target: ≥ 80%) |
| CSV files | — | Local storage only — satisfies Constraint C2 |

---

## Full Folder Structure

```
Software-Testing-and-Validation/
│
├── pom.xml                              ← Maven build (JUnit 5, Mockito, JaCoCo)
├── README.md
├── REPORT.md                            ← Main report (§3.3 still needs filling)
├── TESTING.md                           ← Test plan document (needs filling)
├── IMPLEMENTATION_PLAN.md               ← This file
├── .gitignore
│
├── data/                                ← Runtime CSV storage (created at run time)
│   ├── transactions.csv
│   └── budgets.csv
│
└── src/
    ├── main/java/com/budgetmanager/
    │   ├── Main.java                    ← Entry point; wires all MVC layers
    │   ├── model/
    │   │   ├── TransactionType.java     ← enum: INCOME, EXPENSE
    │   │   ├── Category.java            ← enum: FOOD TRANSPORT ENTERTAINMENT UTILITIES SALARY INVESTMENT OTHER
    │   │   ├── Transaction.java         ← id, amount, date, description, category, type
    │   │   ├── Budget.java              ← category, limit, currentSpending; isExceeded(), isNearLimit()
    │   │   └── Report.java              ← totalIncome, totalExpense, period; getBalance(), getSavingsRate()
    │   ├── dao/
    │   │   ├── FileManager.java         ← Low-level CSV read/write/append/delete
    │   │   ├── TransactionDAO.java      ← save/loadAll/deleteById/findByCategory/findById
    │   │   └── BudgetDAO.java           ← save(upsert)/loadAll/findByCategory/deleteByCategory
    │   ├── controller/
    │   │   ├── TransactionController.java ← add/remove/getAll/filter/search/export business logic
    │   │   ├── BudgetController.java      ← setBudgetLimit/updateSpending/alerts/Decision Table
    │   │   └── ReportController.java      ← monthly/yearly/category-breakdown reports
    │   └── view/
    │       ├── ConsoleView.java          ← All console I/O: menus, prompts, readDouble/readDate
    │       ├── TransactionView.java      ← Tabular transaction list and single-transaction detail
    │       └── ReportView.java           ← Budget status, report display, category breakdown
    │
    └── test/java/com/budgetmanager/
        ├── model/
        │   ├── TransactionTest.java      ← 24 tests — constructor validation, setters, equality
        │   ├── BudgetTest.java           ← 26 tests — isExceeded, isNearLimit, thresholds
        │   └── ReportTest.java           ← 18 tests — getBalance, getSavingsRate, edge cases
        ├── dao/
        │   ├── FileManagerTest.java      ← 13 tests — read/write/append/delete with temp files
        │   ├── TransactionDAOTest.java   ← 13 tests — save/reload round-trip, delete, filter
        │   └── BudgetDAOTest.java        ← 12 tests — upsert, round-trip, findByCategory
        ├── controller/
        │   ├── TransactionControllerTest.java ← 24 tests — Mockito mocks, all methods
        │   ├── BudgetControllerTest.java      ← 27 tests — Decision Table rules 1–4
        │   └── ReportControllerTest.java      ← 14 tests — monthly/yearly/category reports
        ├── structural/                   ← ⏳ Stage 5 — CFG paths + def-use pairs
        │   ├── PathTestingTest.java
        │   └── DataFlowTestingTest.java
        ├── integration/                  ← ⏳ Stage 6 — real file I/O integration
        │   ├── TransactionIntegrationTest.java
        │   └── ReportIntegrationTest.java
        └── validation/                   ← ⏳ Stage 7 — all 5 validation techniques
            ├── BoundaryValueTest.java
            ├── EquivalenceClassTest.java
            ├── DecisionTableTest.java
            ├── StateTransitionTest.java
            └── UseCaseTest.java
```

---

---

# COMPLETED STAGES

---

## STAGE 0 — Project Setup ✅

**What was done:** Created the Maven project with all required dependencies and directory structure.

**Files created:**
- `pom.xml` — JUnit Jupiter 5.10.1, Mockito 5.7.0, JaCoCo 0.8.11 plugin, compiler set to Java 17
- `.gitignore` — ignores `target/`, `data/*.csv`, IDE files
- Full `src/` directory tree

**How it was verified:** `mvn test` ran with 0 failures (no tests yet = passes). Maven resolved all dependencies without errors.

---

## STAGE 1 — Model Layer ✅

**What was done:** Defined all data objects using TDD — tests written first, then code. No I/O, no business logic in any model class. All validation lives in constructors and setters.

### Files

**`TransactionType.java`**
- Enum with two values: `INCOME`, `EXPENSE`
- Used by: Transaction, TransactionController, ReportController

**`Category.java`**
- Enum: `FOOD`, `TRANSPORT`, `ENTERTAINMENT`, `UTILITIES`, `SALARY`, `INVESTMENT`, `OTHER`
- Each value has a `getDisplayName()` method returning a human-readable string (e.g., "Entertainment")
- Longest name is "Entertainment" (13 chars) — used to size console table columns

**`Transaction.java`**
- Fields: `id` (String UUID), `amount` (double), `date` (LocalDate), `description` (String), `category` (Category), `type` (TransactionType)
- Two constructors: auto-generates UUID, or accepts explicit ID (for CSV reload)
- Validation: `amount >= 0.01` (MIN_AMOUNT constant), description 1–100 chars (MAX_DESCRIPTION_LENGTH constant), null guards on all fields
- Equality based on `id` only (not field values)

**`Budget.java`**
- Fields: `category`, `limit`, `currentSpending` (initialised to 0.0)
- Constant: `WARNING_THRESHOLD = 0.80`
- Computed: `getRemainingBudget()` = limit − spending; `isExceeded()` = spending > limit (strictly greater); `isNearLimit()` = usage ≥ 0.80; `getUsagePercentage()` = spending / limit

**`Report.java`**
- Immutable: set at construction, no setters
- Fields: `totalIncome`, `totalExpense`, `period` (e.g., "March 2026" or "2026")
- Computed: `getBalance()` = income − expense; `getSavingsRate()` = balance / income (returns 0.0 when income is zero)

### Tests written (68 total)

| Test class | Tests | What is covered |
|------------|-------|----------------|
| `TransactionTest.java` | 24 | Constructor validation (amount, date, description, category, type), setters, ID-based equality, toString |
| `BudgetTest.java` | 26 | isExceeded() at/above/below limit, isNearLimit() at exactly 80%, getUsagePercentage(), setter validation |
| `ReportTest.java` | 18 | getBalance() positive/zero/negative, getSavingsRate() with zero income guard, period trimming |

**How it was verified:** `mvn test` — all 68 tests pass.

---

## STAGE 2 — DAO Layer ✅

**What was done:** Implemented file I/O using CSV. Each DAO delegates low-level file operations to `FileManager`. No business logic — pure read/write.

### CSV formats

**transactions.csv:**
```
id,amount,date,description,category,type
550e8400-e29b-...,150.00,2026-03-01,Grocery shopping,FOOD,EXPENSE
```

**budgets.csv:**
```
category,limit,currentSpending
FOOD,500.00,150.00
```

### Files

**`FileManager.java`**
- `readLines(filePath)` — returns all non-empty lines; returns empty list if file does not exist
- `writeLines(filePath, lines)` — overwrites file; creates parent directories automatically
- `appendLine(filePath, line)` — appends one line; creates file and parents if missing
- `fileExists(filePath)` — boolean check
- `deleteFile(filePath)` — deletes if exists; no-op if missing

**`TransactionDAO.java`**
- `save(transaction)` — appends CSV row; writes header row on first save
- `loadAll()` — reads all rows, skips header and malformed rows, returns `List<Transaction>`
- `deleteById(id)` — rewrites file excluding the matching ID
- `findByCategory(category)` — returns filtered list
- `findById(id)` — returns `Optional<Transaction>`
- CSV parsing handles RFC 4180 quoted fields (descriptions with commas)

**`BudgetDAO.java`**
- `save(budget)` — upsert: if category already exists, replaces it; otherwise appends
- `loadAll()` — reads all rows, skips header and malformed rows
- `findByCategory(category)` — returns `Optional<Budget>`
- `deleteByCategory(category)` — rewrites file excluding the matching category
- Complete file rewrite used on every save/delete to maintain consistency

### Tests written (38 total)

| Test class | Tests | What is covered |
|------------|-------|----------------|
| `FileManagerTest.java` | 13 | Read/write/append round-trips, overwriting, parent directory creation, special characters, fileExists, deleteFile |
| `TransactionDAOTest.java` | 13 | Save/reload round-trip, header skipped, deleteById, findByCategory, findById, description with commas |
| `BudgetDAOTest.java` | 12 | Save/reload round-trip, upsert (same category replaces), deleteByCategory, findByCategory, isExceeded surviving round-trip |

All DAO tests use **real temporary files** (Java `@TempDir`) — no mocks. This tests actual file I/O.

**How it was verified:** `mvn test` — all 106 tests pass (68 Stage 1 + 38 Stage 2).

---

## STAGE 3 — Controller Layer ✅

**What was done:** Implemented all business logic. Controllers receive input, validate it, apply rules, and delegate persistence to DAOs. No console I/O anywhere in this layer.

All three controllers use **constructor injection** — DAOs are passed in, enabling Mockito mocks in tests.

### Files

**`TransactionController.java`**
- `addTransaction(amount, date, description, category, type)` — delegates validation to Transaction constructor, calls `dao.save()`
- `removeTransaction(id)` — null/empty guard, trims id, calls `dao.deleteById()`
- `getAll()` — delegates to `dao.loadAll()`
- `filterByCategory(category)` — null guard, delegates to `dao.findByCategory()`
- `filterByDateRange(from, to)` — null guards, validates from ≤ to, returns transactions where `!d.isBefore(from) && !d.isAfter(to)` (inclusive bounds)
- `searchByDescription(keyword)` — null/empty guard, case-insensitive substring match
- `exportToCSV(path)` — writes header + all transactions; uses RFC 4180 quoting for descriptions with commas
- Two constructors: `(dao, fileManager)` full, `(dao)` convenience with default FileManager

**`BudgetController.java`**
- Decision Table implemented (from TESTING.md):
  - Rule 1: No budget set → no alert, `updateSpending` is a no-op (no phantom entries)
  - Rule 2: Spending < 80% → no alert
  - Rule 3: 80% ≤ spending ≤ 100% → WARNING alert
  - Rule 4: Spending > 100% → EXCEEDED alert
- `setBudgetLimit(category, limit)` — upsert: preserves currentSpending if budget exists, creates new if not
- `updateSpending(category, amount)` — silently no-op if no budget set (Rule 1)
- `isOverBudget(category)` — returns `dao.findByCategory(category).map(Budget::isExceeded).orElse(false)`
- `getRemainingBudget(category)` — returns `Double.MAX_VALUE` when no budget set (sentinel for "unlimited")
- `getBudget(category)` — returns `Optional<Budget>`
- `getAllBudgets()` — delegates to `dao.loadAll()`
- `checkAllAlerts()` — checks `isExceeded()` BEFORE `isNearLimit()` to prevent double-reporting at >100%

**`ReportController.java`**
- `generateMonthlyReport(year, month)` — validates month 1–12 (throws `IllegalArgumentException` otherwise), iterates all transactions, sums income and expense for matching year+month, period label = `"March 2026"` format
- `generateYearlyReport(year)` — same but all months for the year, period label = year as String
- `generateCategoryBreakdown()` — returns `EnumMap<Category, Double>` of EXPENSE totals using `breakdown.merge(t.getCategory(), t.getAmount(), Double::sum)`; INCOME transactions excluded

### Tests written (65 total)

| Test class | Tests | What is covered |
|------------|-------|----------------|
| `TransactionControllerTest.java` | 24 | addTransaction (save called, exception propagation), removeTransaction (null/empty guard, id trimming), getAll, filterByCategory (null guard), filterByDateRange (boundary inclusive, null guards, from>to), searchByDescription (case-insensitive, null guard), exportToCSV (header + rows, ArgumentCaptor to verify exact CSV lines) |
| `BudgetControllerTest.java` | 27 | setBudgetLimit (new + update + limit validation), updateSpending (Rule 1 no-op, adds correctly), isOverBudget, getRemainingBudget (Double.MAX_VALUE sentinel), getBudget, getAllBudgets, checkAllAlerts (Rules 1, 2, 3 at exactly 80%, Rule 3 at 99%, Rule 4 at 150%, mixed multiple budgets) |
| `ReportControllerTest.java` | 14 | generateMonthlyReport (income/expense sums, ignores other months, invalid month throws, period label format), generateYearlyReport (aggregates all months), generateCategoryBreakdown (EXPENSE only, INCOME excluded, multiple categories, empty) |

All controller tests use **Mockito** (`@ExtendWith(MockitoExtension.class)`) — DAOs are mocked, zero file I/O.

**How it was verified:** `mvn test` — all 171 tests pass. No compiler warnings (stale Javadoc comment mentioning BudgetDAO was fixed during review).

---

## STAGE 4 — View Layer + Main.java ✅

**What was done:** Built the three view classes (pure output, no logic) and the Main.java entry point that wires all MVC layers together.

### Files

**`ConsoleView.java`**
- Injected via `ConsoleView(Scanner, PrintStream)` for testability; default `ConsoleView()` uses stdin/stdout
- `showMainMenu()` — prints 12 numbered options (0 = Exit, 1–11 = features)
- `showCategoryMenu()` — prints 7 categories (1=Food … 7=Other)
- `showTypeMenu()` — prints 1=Expense, 2=Income
- `showMessage(msg)` / `showError(msg)` / `showSuccess(msg)` — output helpers
- `readInput()` — trims; returns "" on EOF
- `readDouble(prompt)` — loops until positive double entered; error on ≤ 0
- `readInt(prompt)` — loops until integer entered
- `readDate(prompt)` — loops until valid YYYY-MM-DD; blank Enter defaults to today
- `confirmAction(prompt)` — returns true for "y"/"yes" (case-insensitive)

**`TransactionView.java`**
- Injected via `TransactionView(PrintStream)` for testability
- `displayTransactionList(List<Transaction>)` — tabular output with fixed-width columns (ID 36, Amount 8, Date 10, Description 20, Category 13, Type 8); truncates description to 17+"..." if >20 chars; "No transactions found." when empty
- `displayTransactionDetails(Transaction)` — labelled single-transaction view
- `showAddTransactionPrompt()` — prints banner header

**`ReportView.java`**
- Injected via `ReportView(PrintStream)` for testability
- `displayReport(Report)` — shows income/expense/net balance/savings rate (multiplies `getSavingsRate()` ratio × 100 for %)
- `displayBudgetStatus(List<Budget>)` — shows category/spent/limit/remaining/progress bar/[WARNING] or [EXCEEDED] flags; 20-char `#` progress bar capped at 100%
- `displayCategoryBreakdown(Map<Category,Double>)` — shows amount and % share per category; TOTAL row at bottom
- `displayAlerts(List<String>)` — shows "[OK] All budgets are within limits." or lists each alert with "! " prefix

**`Main.java`**
- Creates `FileManager` → `TransactionDAO` + `BudgetDAO` → `TransactionController(dao, fileManager)` + `BudgetController(budgetDAO)` + `ReportController(txnDAO)` → views
- Console loop: reads choice → dispatches to private handler methods → catches `IOException` and `IllegalArgumentException`
- `parseCategory(input)` maps "1"–"7" to Category enum (matches `showCategoryMenu()` order exactly)
- `parseType(input)` maps "1"→EXPENSE, "2"→INCOME (matches `showTypeMenu()` order)
- On EXPENSE transaction add: calls `budgetCtrl.updateSpending(category, amount)` to track spending

**How it was verified:** Stage 4 reviewed line by line:
- All view method calls cross-checked against actual model API (all methods exist)
- Menu number ↔ switch case ↔ parse helper alignment confirmed
- MVC constraint verified: no business logic in any view, no I/O in any controller
- Application runs end-to-end: `mvn package` then `java -jar target/budget-management-system-1.0-SNAPSHOT.jar`

---

---

# REMAINING STAGES

---

## STAGE 5 — Structural Testing ⏳

**Rubric relevance:** Required by `project_description.md` — "Structural Testing: path testing + data flow testing". Directly contributes to the 60-point Implementation & Testing section.

**Target function chosen:** `TransactionController.filterByDateRange(LocalDate from, LocalDate to)`

**Why this function:** It has a clear, documentable CFG with 5 distinct paths (null checks, from>to check, empty loop, transaction matches, transaction skipped), making it ideal for both path testing and def-use pair documentation.

---

### 5.1 Path Testing — `PathTestingTest.java`

**File location:** `src/test/java/com/budgetmanager/structural/PathTestingTest.java`

**What to do:**
1. Draw the Control Flow Graph (CFG) for `filterByDateRange` and include it as comments in the test file and in TESTING.md.
2. Identify all independent basis paths using McCabe's cyclomatic complexity formula: `V(G) = E − N + 2P`
3. Write one JUnit test per path.

**CFG for `filterByDateRange`:**
```
Node 1: entry (from, to received)
Node 2: if (from == null || to == null) → TRUE → Node 3 (throw IAE)
                                        → FALSE → Node 4
Node 3: throw IllegalArgumentException("Date range boundaries cannot be null")
Node 4: if (from.isAfter(to)) → TRUE → Node 5 (throw IAE)
                               → FALSE → Node 6
Node 5: throw IllegalArgumentException("Start date cannot be after end date...")
Node 6: result = new ArrayList<>()
Node 7: for loop condition: hasNext()? → FALSE → Node 10 (return result)
                                      → TRUE  → Node 8
Node 8: if (!d.isBefore(from) && !d.isAfter(to)) → TRUE → Node 9 (result.add(t))
                                                  → FALSE → Node 7 (next iteration)
Node 9: result.add(t) → Node 7
Node 10: return result
```

**Cyclomatic complexity:** V(G) = 5 (5 independent paths)

**Paths and test cases to write:**

| Path | Name | Input setup | Expected result |
|------|------|-------------|----------------|
| Path 1 | `path1_fromIsNull_throwsException` | from=null, to=any date | `IllegalArgumentException` |
| Path 2 | `path2_toIsNull_throwsException` | from=any date, to=null | `IllegalArgumentException` |
| Path 3 | `path3_fromAfterTo_throwsException` | from=2026-03-31, to=2026-03-01 | `IllegalArgumentException` |
| Path 4 | `path4_emptyList_noTransactions` | from=2026-01-01, to=2026-12-31, dao returns empty list | empty list returned |
| Path 5 | `path5_transactionInRange_included` | transaction date 2026-03-15, from=2026-03-01, to=2026-03-31 | list with 1 transaction |
| Path 5b | `path5b_transactionOutsideRange_excluded` | transaction date 2026-04-01, from=2026-03-01, to=2026-03-31 | empty list |
| Path 5c | `path5c_transactionOnBoundary_included` | dates exactly equal from and to | list with 1 transaction (inclusive bounds) |

**Test class setup:**
```java
@ExtendWith(MockitoExtension.class)
class PathTestingTest {
    @Mock private TransactionDAO dao;
    private TransactionController controller;

    @BeforeEach void setUp() {
        controller = new TransactionController(dao);
    }
    // one @Test method per path above
}
```

---

### 5.2 Data Flow Testing — `DataFlowTestingTest.java`

**File location:** `src/test/java/com/budgetmanager/structural/DataFlowTestingTest.java`

**What to do:**
1. Identify all def-use pairs for each local variable in `filterByDateRange`.
2. Write tests covering all-defs criterion and all-uses criterion.
3. Document each pair in comments in the test file and in TESTING.md.

**Variables and their def-use pairs:**

| Variable | Defined at | Used at | Pair label |
|----------|-----------|---------|-----------|
| `from` | parameter entry | null check (Node 2) | DU1 |
| `from` | parameter entry | `from.isAfter(to)` (Node 4) | DU2 |
| `from` | parameter entry | `!d.isBefore(from)` (Node 8) | DU3 |
| `to` | parameter entry | null check (Node 2) | DU4 |
| `to` | parameter entry | `from.isAfter(to)` (Node 4) | DU5 |
| `to` | parameter entry | `!d.isAfter(to)` (Node 8) | DU6 |
| `result` | Node 6 (new ArrayList) | `result.add(t)` (Node 9) | DU7 |
| `result` | Node 6 (new ArrayList) | `return result` (Node 10) | DU8 |
| `t` (loop var) | Node 7 (iterator.next()) | `t.getDate()` (Node 8) | DU9 |
| `t` (loop var) | Node 7 (iterator.next()) | `result.add(t)` (Node 9) | DU10 |
| `d` | Node 8 (`t.getDate()`) | `!d.isBefore(from)` (Node 8) | DU11 |
| `d` | Node 8 (`t.getDate()`) | `!d.isAfter(to)` (Node 8) | DU12 |

**Tests to write (one per def-use pair or combined for closely related pairs):**

| Test method | Pairs covered | What it does |
|-------------|--------------|-------------|
| `du1_du4_fromNullReachedNullCheck` | DU1, DU4 | from=null → IAE thrown |
| `du2_du5_fromAfterToReachedRangeCheck` | DU2, DU5 | from=Mar 31, to=Mar 1 → IAE thrown |
| `du3_du6_fromAndToReachedLoopComparison` | DU3, DU6 | valid range, transaction in range → included |
| `du7_resultAddedWhenInRange` | DU7 | transaction in range → result.add called |
| `du8_resultReturnedWhenEmpty` | DU8 | empty DAO → empty list returned |
| `du9_tDateReachedComparison` | DU9 | transaction date extracted and compared |
| `du10_tAddedToResult` | DU10 | transaction within range added to result |
| `du11_du12_dUsedInBothBoundaryChecks` | DU11, DU12 | transaction exactly on from boundary; exactly on to boundary |

---

## STAGE 6 — Integration Testing ⏳

**Rubric relevance:** Required by `project_description.md` — "Integration Testing: choose a subset of units to perform integration testing." Tests that components work correctly together across layer boundaries with real file I/O (no mocks).

**File location:** `src/test/java/com/budgetmanager/integration/`

---

### 6.1 `TransactionIntegrationTest.java`

**Integration boundary:** `TransactionController ↔ TransactionDAO ↔ FileManager`

**Setup:** Use `@TempDir` to create a real temporary directory. Use a real `FileManager` and real `TransactionDAO` pointing at the temp file. No mocks.

**Test scenarios to write:**

| Test method | What it tests |
|-------------|--------------|
| `addTransaction_persistsToFile` | Add a transaction → load file directly → verify row exists |
| `addAndReload_transactionSurvivesRoundTrip` | Add transaction → create new DAO instance → loadAll() → verify same fields |
| `removeTransaction_removedFromFile` | Add 2 transactions → remove 1 → loadAll() returns only 1 |
| `filterByCategory_afterPersist` | Add FOOD and TRANSPORT transactions → filterByCategory(FOOD) → returns only FOOD |
| `filterByDateRange_acrossRealFile` | Add transactions in Jan, Feb, Mar → filterByDateRange(Feb 1, Feb 28) → returns only Feb |
| `searchByDescription_caseInsensitive` | Add "Grocery shopping" → searchByDescription("grocery") → found |
| `exportToCSV_createsReadableFile` | Add 3 transactions → exportToCSV(path) → read file → verify header + 3 data rows |
| `addExpense_updatesBudgetSpending` | Add FOOD expense → budgetCtrl.updateSpending → isOverBudget correct |

---

### 6.2 `ReportIntegrationTest.java`

**Integration boundary:** `ReportController ↔ TransactionDAO ↔ BudgetController ↔ BudgetDAO`

**Setup:** Same `@TempDir` approach — real DAOs, real files, real controllers.

**Test scenarios to write:**

| Test method | What it tests |
|-------------|--------------|
| `monthlyReport_correctTotals` | Add 2 income + 3 expense txns in March → generateMonthlyReport(2026,3) → totals match sum |
| `monthlyReport_ignoresOtherMonths` | Add txns in Jan and March → report for March shows only March txns |
| `yearlyReport_aggregatesAllMonths` | Add txns across Jan/Jun/Dec → generateYearlyReport(2026) → total is sum of all |
| `categoryBreakdown_matchesExpenses` | Add FOOD 100 + FOOD 50 + TRANSPORT 200 → breakdown: FOOD=150, TRANSPORT=200 |
| `budgetAlert_triggeredAfterExpense` | Set FOOD budget 100 → add FOOD expense 95 → checkAllAlerts() returns WARNING |
| `budgetExceeded_triggeredAfterExpense` | Set FOOD budget 100 → add FOOD expense 150 → checkAllAlerts() returns EXCEEDED |

---

## STAGE 7 — Validation Testing ⏳

**Rubric relevance:** Required by `project_description.md` — all 5 validation techniques must be demonstrated. This is the most visible testing section for the professor.

**File location:** `src/test/java/com/budgetmanager/validation/`

---

### 7.1 `BoundaryValueTest.java`

**What it is:** Tests the edges of valid input ranges. For each input: minimum, minimum+1, nominal, maximum−1, maximum, below minimum, above maximum.

**Inputs to test:**

| Input | Min | Min+1 | Nominal | Max-1 | Max | Below Min | Above Max |
|-------|-----|-------|---------|-------|-----|-----------|-----------|
| `amount` | 0.01 | 0.02 | 100.00 | 999998.99 | 999999.99 | 0.00 | none (double max) |
| `description length` | 1 char | 2 chars | 50 chars | 99 chars | 100 chars | 0 chars (empty) | 101 chars |
| `month (generateMonthlyReport)` | 1 | 2 | 6 | 11 | 12 | 0 | 13 |
| `budget limit` | 0.01 | 0.02 | 500.00 | — | — | 0.00 | negative |

**Tests to write:**

| Test method | Input | Expected |
|-------------|-------|---------|
| `amount_atMinimum_accepted` | 0.01 | Transaction created |
| `amount_belowMinimum_rejected` | 0.00 | `IllegalArgumentException` |
| `amount_negative_rejected` | −1.00 | `IllegalArgumentException` |
| `description_oneChar_accepted` | "X" (1 char) | Transaction created |
| `description_empty_rejected` | "" | `IllegalArgumentException` |
| `description_100chars_accepted` | 100-char string | Transaction created |
| `description_101chars_rejected` | 101-char string | `IllegalArgumentException` |
| `month_1_accepted` | month=1 | Report with period "January YYYY" |
| `month_12_accepted` | month=12 | Report with period "December YYYY" |
| `month_0_rejected` | month=0 | `IllegalArgumentException` |
| `month_13_rejected` | month=13 | `IllegalArgumentException` |
| `budgetLimit_positive_accepted` | 0.01 | Budget created |
| `budgetLimit_zero_rejected` | 0.00 | `IllegalArgumentException` |
| `budgetLimit_negative_rejected` | −100 | `IllegalArgumentException` |

**Setup:** These tests use real `Transaction`, `Budget`, `Report` model objects and real controllers with `@TempDir` DAOs. No mocks needed — these are end-to-end validation tests.

---

### 7.2 `EquivalenceClassTest.java`

**What it is:** Partition inputs into valid and invalid equivalence classes. Test one representative per class.

**Partitions:**

| Input | Valid Classes | Invalid Classes |
|-------|--------------|----------------|
| `amount` | EC1: positive decimal ≥ 0.01 | EC2: zero; EC3: negative; EC4: non-numeric (handled by ConsoleView) |
| `description` | EC5: 1–100 non-blank chars | EC6: null; EC7: empty/whitespace; EC8: >100 chars |
| `category` | EC9: any valid Category enum | EC10: null |
| `type` | EC11: INCOME or EXPENSE | EC12: null |
| `date` | EC13: valid LocalDate | EC14: null |
| `keyword (search)` | EC15: non-empty string | EC16: null; EC17: empty/whitespace |
| `id (remove)` | EC18: non-empty string | EC19: null; EC20: empty/whitespace |

**Tests to write (one per class):**

| Test method | Class | Expected |
|-------------|-------|---------|
| `amount_validPositive_EC1_accepted` | EC1: 50.00 | Transaction created |
| `amount_zero_EC2_rejected` | EC2: 0.00 | `IllegalArgumentException` |
| `amount_negative_EC3_rejected` | EC3: −10.00 | `IllegalArgumentException` |
| `description_valid_EC5_accepted` | EC5: "Lunch" | Transaction created |
| `description_null_EC6_rejected` | EC6: null | `IllegalArgumentException` |
| `description_whitespace_EC7_rejected` | EC7: "   " | `IllegalArgumentException` |
| `description_tooLong_EC8_rejected` | EC8: 101-char string | `IllegalArgumentException` |
| `category_valid_EC9_accepted` | EC9: FOOD | Transaction created |
| `category_null_EC10_rejected` | EC10: null | `IllegalArgumentException` |
| `type_null_EC12_rejected` | EC12: null | `IllegalArgumentException` |
| `date_null_EC14_rejected` | EC14: null | `IllegalArgumentException` |
| `keyword_valid_EC15_returnsResults` | EC15: "lunch" | List returned |
| `keyword_null_EC16_rejected` | EC16: null | `IllegalArgumentException` |
| `keyword_empty_EC17_rejected` | EC17: "" | `IllegalArgumentException` |
| `removeId_null_EC19_rejected` | EC19: null | `IllegalArgumentException` |
| `removeId_empty_EC20_rejected` | EC20: " " | `IllegalArgumentException` |

---

### 7.3 `DecisionTableTest.java`

**What it is:** Tests all combinations of conditions that drive system behavior. The Decision Table maps directly to `BudgetController.checkAllAlerts()`.

**Decision Table:**

| Condition | Rule 1 | Rule 2 | Rule 3 | Rule 4 |
|-----------|--------|--------|--------|--------|
| Budget limit set? | No | Yes | Yes | Yes |
| Spending > 100% of limit? | — | No | No | Yes |
| Spending ≥ 80% of limit? | — | No | Yes | Yes |
| **Action: No alert** | ✓ | ✓ | — | — |
| **Action: WARNING alert** | — | — | ✓ | — |
| **Action: EXCEEDED alert** | — | — | — | ✓ |

**Tests to write:**

| Test method | Rule | Conditions | Expected |
|-------------|------|------------|---------|
| `rule1_noBudgetSet_noAlert` | Rule 1 | No budget for FOOD | empty alerts list |
| `rule2_spendingAt0Percent_noAlert` | Rule 2 | Budget=100, spending=0 | empty alerts list |
| `rule2_spendingAt79Percent_noAlert` | Rule 2 | Budget=100, spending=79 | empty alerts list |
| `rule3_spendingAtExactly80Percent_warningAlert` | Rule 3 | Budget=100, spending=80 | WARNING in list |
| `rule3_spendingAt99Percent_warningAlert` | Rule 3 | Budget=100, spending=99 | WARNING in list |
| `rule3_spendingAtExactly100Percent_warningNotExceeded` | Rule 3 | Budget=100, spending=100 | WARNING (not EXCEEDED, because isExceeded is strictly >) |
| `rule4_spendingOver100Percent_exceededAlert` | Rule 4 | Budget=100, spending=101 | EXCEEDED in list |
| `rule4_spendingAt150Percent_exceededAlert` | Rule 4 | Budget=100, spending=150 | EXCEEDED in list |
| `mixed_threeCategories_allRules` | Rules 2+3+4 | FOOD<80%, TRANSPORT=85%, UTILITIES=110% | 0+WARNING+EXCEEDED |

**Setup:** Use real `BudgetController` + real `BudgetDAO` + `@TempDir` — these are end-to-end Decision Table tests.

---

### 7.4 `StateTransitionTest.java`

**What it is:** Tests the lifecycle states of a transaction and budget object. Every valid transition and every invalid/boundary transition.

**Transaction State Diagram:**
```
[NON-EXISTENT] --addTransaction()--> [SAVED]
[SAVED]        --deleteById()------> [DELETED/NON-EXISTENT]
[SAVED]        --setAmount()-------> [SAVED]  (state unchanged, field updated)
[SAVED]        --setCategory()-----> [SAVED]  (state unchanged, field updated)
[NON-EXISTENT] --deleteById()------> [NON-EXISTENT]  (no-op, no error)
```

**Budget State Diagram:**
```
[NO_BUDGET]  --setBudgetLimit()--> [BUDGET_SET]
[BUDGET_SET] --updateSpending()--> [BUDGET_SET]  (spending accumulates)
[BUDGET_SET] --setBudgetLimit()--> [BUDGET_SET]  (limit updated, spending preserved)
[NO_BUDGET]  --updateSpending()--> [NO_BUDGET]   (no-op, Rule 1)
[BUDGET_SET] --checkAllAlerts()--> produces alert or not (not a state change)
```

**Tests to write:**

| Test method | Transition | Expected |
|-------------|-----------|---------|
| `txn_nonExistentToSaved_onAdd` | NON-EXISTENT→SAVED | loadAll() contains the transaction |
| `txn_savedToDeleted_onRemove` | SAVED→DELETED | loadAll() does not contain the transaction |
| `txn_deleteNonExistent_noOp` | NON-EXISTENT→NON-EXISTENT | no exception, loadAll() still same |
| `txn_savedToSaved_onFieldUpdate` | SAVED→SAVED | setAmount then save then reload — new amount persisted |
| `txn_deletedThenReSaved` | DELETED→SAVED | same ID deleted then new transaction added; new one present |
| `budget_noBudgetToSet_onSetLimit` | NO_BUDGET→BUDGET_SET | getBudget returns present |
| `budget_setBudgetPreservesSpending_onLimitUpdate` | BUDGET_SET→BUDGET_SET | spending unchanged after setLimit |
| `budget_updateSpending_noOp_whenNoBudget` | NO_BUDGET→NO_BUDGET | updateSpending does not create a budget entry |
| `budget_spendingAccumulates_acrossMultipleUpdates` | BUDGET_SET→BUDGET_SET | two updateSpending calls sum correctly |

---

### 7.5 `UseCaseTest.java`

**What it is:** Tests complete end-to-end user scenarios from the user's perspective. Each test represents a real use case the professor would evaluate.

**Use Cases:**

| ID | Use Case | Actor | Goal |
|----|----------|-------|------|
| UC-01 | Record an expense | User | Add expense transaction, budget updated |
| UC-02 | View monthly report | User | See correct income/expense totals for a month |
| UC-03 | Budget limit exceeded | User | See EXCEEDED alert after over-spending |
| UC-04 | Filter transactions by date | User | See only transactions in selected range |
| UC-05 | Export transactions to CSV | User | Get a CSV file with all transaction data |
| UC-06 | Record income and check balance | User | Monthly report shows correct positive balance |
| UC-07 | Set budget then check near-limit warning | User | See WARNING at 80% spending |
| UC-08 | Search by description finds correct transaction | User | Keyword search returns matching results |

**Tests to write:**

| Test method | UC | Steps | Expected |
|-------------|-----|-------|---------|
| `uc01_recordExpense_budgetUpdated` | UC-01 | Set FOOD budget 200 → add FOOD expense 50 → checkAlerts | no alert yet |
| `uc02_monthlyReport_correctTotals` | UC-02 | Add salary 2000 (INCOME) + rent 800 (EXPENSE) in March → generateMonthlyReport(2026,3) | income=2000, expense=800, balance=1200 |
| `uc03_budgetExceeded_alertShown` | UC-03 | Set FOOD budget 100 → add FOOD expense 150 → checkAllAlerts | list contains EXCEEDED message |
| `uc04_filterByDateRange_correctResults` | UC-04 | Add txns in Jan, Feb, Mar → filter Feb 1–Feb 28 | only Feb transactions returned |
| `uc05_exportToCSV_fileCreated` | UC-05 | Add 2 transactions → exportToCSV(path) → read file | header row + 2 data rows |
| `uc06_incomeAndExpense_positiveBalance` | UC-06 | Add income 3000 + expense 1500 → monthlyReport | balance=1500, savingsRate=50% |
| `uc07_nearLimitWarning_at80Percent` | UC-07 | Set budget 1000 → add expense 800 → checkAllAlerts | WARNING message returned |
| `uc08_searchByDescription_caseInsensitive` | UC-08 | Add transaction "Grocery Shopping" → search "grocery" | transaction found |

**Setup for all validation tests:** Use `@TempDir` with real `FileManager`, real DAOs, real controllers — full stack integration, no mocks.

---

## STAGE 8 — Documentation ⏳

**Rubric relevance:** Documentation accounts for quality in Sections 1, 2, 3, and 4. The professor uses the GitHub repo directly. Well-organized, error-free documentation is required for "Meet Expectations" and above.

### REPORT.md — What Needs to Be Written

| Section | Content Required |
|---------|----------------|
| §3.3 | Intro: why MVC was chosen over Solutions 1 & 2; how it satisfies testability requirements; comparison table |
| §3.3.1 | Every component listed (Transaction, Budget, Report, FileManager, TransactionDAO, BudgetDAO, TransactionController, BudgetController, ReportController, ConsoleView, TransactionView, ReportView, Main), its purpose, which testing method applies. ASCII block diagram of component relationships |
| §3.3.2 | Map each constraint (economic/regulatory/reliability/sustainability/ethics/societal) to how the MVC design satisfies it |
| §3.3.3 | Full test suite summary: how many tests (171+), what types, pass/fail results; link to TESTING.md |
| §3.3.4 | Limitations: console-only UI, no multi-user, no encryption, no cloud, no date-based budget reset |
| §4.3, §4.4 | Team meeting 3 and 4 logs |
| §5 | Gantt chart (markdown table or ASCII) |
| §6 | Conclusion: what was built and tested, future work (GUI, cloud, multi-user) |
| §7 | IEEE-format references |

### TESTING.md — What Needs to Be Written

```
## 1. Structural Testing
- Target function: TransactionController.filterByDateRange(from, to)
- CFG diagram (ASCII)
- Cyclomatic complexity: V(G) = 5
- All basis paths enumerated
- Test case table (path, input, expected output)
- Data flow: all def-use pairs listed in table

## 2. Integration Testing
- Units under test: TransactionController ↔ TransactionDAO ↔ FileManager
- Units under test: ReportController ↔ TransactionController ↔ BudgetController
- Test scenario table (scenario, preconditions, steps, expected)

## 3. Validation Testing
### 3.1 Boundary Value
- Input table: min/min+1/nominal/max-1/max/below-min/above-max for each input
### 3.2 Equivalence Classes
- Partition table with valid and invalid classes per input
### 3.3 Decision Tables
- Full decision table for BudgetController.checkAllAlerts()
### 3.4 State Transitions
- State diagram (ASCII) for Transaction lifecycle and Budget lifecycle
- Transition table: from-state, event, to-state, expected behavior
### 3.5 Use Cases
- Use case table (ID, actor, goal, steps, expected result)

## 4. Test Results Summary
- Total tests written: [number]
- Tests passing: [number]
- JaCoCo coverage: [%]
- Test run output attached
```

---

## STAGE 9 — Coverage & Final Polish ⏳

**Goal:** Achieve ≥ 80% line/branch coverage measured by JaCoCo (Constraint C7). Ensure all code is well-commented (professor requirement). Final commit.

**Steps:**
1. Run `mvn jacoco:report` — opens HTML report in `target/site/jacoco/`
2. Identify uncovered lines/branches using the HTML report
3. Add targeted tests until ≥ 80% reached
4. Review all source files: every class needs a Javadoc class comment; every public method needs a Javadoc method comment (all Stage 3-4 files already have this)
5. Commit frequently — professor tracks via GitHub commit history
6. Email GitHub project name + commit hash to instructor via URCourses

**JaCoCo minimum configuration in pom.xml to enforce at build time:**
```xml
<configuration>
    <rules>
        <rule>
            <limits>
                <limit>
                    <counter>LINE</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.80</minimum>
                </limit>
            </limits>
        </rule>
    </rules>
</configuration>
```

---

---

## Grading Alignment — Where Each Stage Maps to the Rubric

| Rubric Section | Points | What satisfies "Exceed Expectations" | Stages that contribute |
|----------------|--------|--------------------------------------|----------------------|
| Problem Definition & Design Requirements | 10 | Constraints explicitly addressed with focus on environmental/social/ethical aspects | REPORT.md §2.1, §2.2 (done), §3.3.2 (Stage 8) |
| Iterative Design Process & Design Selection | 20 | 3 solutions with formal decision-making method; novel final solution; justified metrics | REPORT.md §3.1–3.2 (done), §3.3 intro (Stage 8) |
| Final Design Implementation & Testing | 60 | All constraints satisfied; ALL testing methods implemented; all test cases executed; outputs compared to expected | Stages 0–4 (implementation), Stages 5–7 (testing), Stage 9 (coverage) |
| Collaborative Teamwork & Communication | 10 | Well-organized, well-written, all necessary info present | REPORT.md §4–5 (Stage 8 + D5) |

---

## Required Testing Techniques Checklist

| Technique | Required | Status | File |
|-----------|---------|--------|------|
| Path testing | ✅ YES | ✅ DONE | `structural/PathTestingTest.java` |
| Data flow testing | ✅ YES | ✅ DONE | `structural/DataFlowTestingTest.java` |
| Integration testing | ✅ YES | ✅ DONE | `integration/TransactionIntegrationTest.java`, `ReportIntegrationTest.java` |
| Boundary value testing | ✅ YES | ⏳ TODO | `validation/BoundaryValueTest.java` |
| Equivalence class testing | ✅ YES | ⏳ TODO | `validation/EquivalenceClassTest.java` |
| Decision table testing | ✅ YES | ⏳ TODO | `validation/DecisionTableTest.java` |
| State transition testing | ✅ YES | ⏳ TODO | `validation/StateTransitionTest.java` |
| Use case testing | ✅ YES | ⏳ TODO | `validation/UseCaseTest.java` |
| Unit testing (model) | ✅ present | ✅ DONE | `model/TransactionTest.java`, `BudgetTest.java`, `ReportTest.java` |
| Unit testing (DAO) | ✅ present | ✅ DONE | `dao/FileManagerTest.java`, `TransactionDAOTest.java`, `BudgetDAOTest.java` |
| Unit testing (controller with mocks) | ✅ present | ✅ DONE | `controller/TransactionControllerTest.java`, `BudgetControllerTest.java`, `ReportControllerTest.java` |

---

## Test Count Summary (As of Stage 6 Completion)

| Package | Test class | Tests |
|---------|-----------|-------|
| model | TransactionTest | 24 |
| model | BudgetTest | 26 |
| model | ReportTest | 18 |
| dao | FileManagerTest | 13 |
| dao | TransactionDAOTest | 13 |
| dao | BudgetDAOTest | 12 |
| controller | TransactionControllerTest | 24 |
| controller | BudgetControllerTest | 27 |
| controller | ReportControllerTest | 14 |
| structural | PathTestingTest | 7 |
| structural | DataFlowTestingTest | 26 |
| integration | TransactionIntegrationTest | 8 |
| integration | ReportIntegrationTest | 6 |
| **Total** | | **218** |

All 218 tests pass — `mvn test` → BUILD SUCCESS.

---

*ENSE 375 – Software Testing and Validation | University of Regina*
*Team: Nyabijek Gatdet · Chop Peter Kur · Aubin Chriss*
