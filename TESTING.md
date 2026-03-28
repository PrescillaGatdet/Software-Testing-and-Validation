# ENSE 375 – Software Testing and Validation

# Budget Management System — Test Plan

**Team:** Nyabijek Gatdet · Chop Peter Kur · Aubin Chriss Izere
**Course:** ENSE 375 – Software Testing and Validation, University of Regina

---

## Table of Contents

1. [Overview](#1-overview)
2. [Technical Requirements Coverage](#2-technical-requirements-coverage)
3. [Structural Testing](#3-structural-testing)
   - 3.1 [Path Testing](#31-path-testing)
   - 3.2 [Data Flow Testing](#32-data-flow-testing)
4. [Integration Testing](#4-integration-testing)
5. [Validation Testing](#5-validation-testing)
   - 5.1 [Boundary Value Testing](#51-boundary-value-testing)
   - 5.2 [Equivalence Class Testing](#52-equivalence-class-testing)
   - 5.3 [Decision Table Testing](#53-decision-table-testing)
   - 5.4 [State Transition Testing](#54-state-transition-testing)
   - 5.5 [Use Case Testing](#55-use-case-testing)
6. [Test Execution Results](#6-test-execution-results)

---

## 1. Overview

The Budget Management System was developed using **Test-Driven Development (TDD)**: test cases were written before production code at every stage. The **MVC architecture** enforces strict layer separation — Model, DAO, and Controller classes have no cross-layer dependencies — which enables each layer to be unit tested in complete isolation. Unit tests for the Controller layer use **Mockito** to mock DAO dependencies, eliminating any file I/O during unit test execution. Integration tests use JUnit 5's `@TempDir` to perform real file I/O against temporary directories, verifying that layers interact correctly without side effects. **JaCoCo** is configured to enforce ≥ 80% code coverage.

**Test suite summary:**

| Category | Test File(s) | Tests |
|---|---|---|
| Model Unit Tests | `TransactionTest`, `BudgetTest`, `ReportTest` | 68 |
| DAO Unit Tests | `FileManagerTest`, `TransactionDAOTest`, `BudgetDAOTest` | 38 |
| Controller Unit Tests | `TransactionControllerTest`, `BudgetControllerTest`, `ReportControllerTest` | 65 |
| Structural Testing | `PathTestingTest`, `DataFlowTestingTest` | 33 |
| Integration Testing | `TransactionIntegrationTest`, `ReportIntegrationTest` | 14 |
| Validation Testing | `BoundaryValueTest`, `EquivalenceClassTest`, `DecisionTableTest`, `StateTransitionTest`, `UseCaseTest` | 61 |
| **Total** | | **279** |

**Toolchain:** Java 17 · Maven 3.x · JUnit Jupiter 5.10.1 · Mockito 5.7.0 · JaCoCo 0.8.11
**Run all tests:** `mvn test`

---

## 2. Technical Requirements Coverage

Each functional requirement from §2.2.1 of REPORT.md is covered by at least one test class:

| Requirement | Testing Method(s) | Test Class(es) |
|---|---|---|
| Record income/expense transactions | Unit (Model + Controller) | `TransactionTest`, `TransactionControllerTest` |
| Categorize transactions | Unit | `TransactionTest`, `TransactionControllerTest` |
| Set budget limits | Unit, Decision Table, State Transition | `BudgetTest`, `BudgetControllerTest`, `DecisionTableTest`, `StateTransitionTest` |
| Calculate budget summaries | Unit, Integration | `ReportTest`, `ReportControllerTest`, `ReportIntegrationTest` |
| Generate financial reports | Unit, Integration | `ReportControllerTest`, `ReportIntegrationTest` |
| Alert on budget exceedance | Unit, Decision Table, State Transition | `BudgetControllerTest`, `DecisionTableTest`, `StateTransitionTest` |
| Filter and search transactions | Structural, Integration | `PathTestingTest`, `DataFlowTestingTest`, `TransactionIntegrationTest` |
| Persist data to local storage | DAO Unit, Integration | `TransactionDAOTest`, `BudgetDAOTest`, `TransactionIntegrationTest` |
| Export data to CSV | Unit, Integration | `TransactionControllerTest`, `TransactionIntegrationTest` |

---

## 3. Structural Testing

**Function under test:** `TransactionController.filterByDateRange(LocalDate from, LocalDate to)`

This method was selected because it contains four independent decision points — null checks on both parameters, a date-order validation, a loop termination condition, and a date-range membership predicate — yielding a cyclomatic complexity of V(G) = 5, which is rich enough to require systematic basis-path analysis without being intractable.

### 3.1 Path Testing

#### Control Flow Graph

```
N1:  ENTRY — receive (from, to)
      │
N2:  DECISION — from == null || to == null?
      │ TRUE ──────────────────────────────► N3: throw IllegalArgumentException("null")
      │ FALSE
N4:  DECISION — from.isAfter(to)?
      │ TRUE ──────────────────────────────► N5: throw IllegalArgumentException("Start date cannot be after end date")
      │ FALSE
N6:  result = new ArrayList<>()
      │
N7:  DECISION — more elements in dao.loadAll()?  ◄─────────────────┐
      │ FALSE ─────────────────────────────► N10: return result     │
      │ TRUE                                                         │
N8:  DECISION — !d.isBefore(from) && !d.isAfter(to)?               │
      │ FALSE ──────────────────────────────────────────────────────┘
      │ TRUE
N9:  result.add(t) ──────────────────────────────────────────────────┘
```

**Cyclomatic Complexity:** V(G) = 4 predicate nodes + 1 = **5**

#### Basis Paths

| Path | Route | Trigger Condition | Expected Outcome |
|---|---|---|---|
| P1 | N1→N2→N3 | `from` is null | throws `IllegalArgumentException` |
| P2 | N1→N2→N3 | `to` is null | throws `IllegalArgumentException` |
| P3 | N1→N2→N4→N5 | both non-null; `from.isAfter(to)` is true | throws `IllegalArgumentException` ("Start date cannot be after end date") |
| P4 | N1→N2→N4→N6→N7→N10 | valid date range; DAO returns empty list | returns empty `ArrayList` |
| P5 | N1→N2→N4→N6→N7→N8→N9→N7→N10 | valid date range; transaction date within range | transaction added to result and returned |
| P5b | N1→N2→N4→N6→N7→N8→N7→N10 | valid date range; transaction date outside range | transaction skipped; empty list returned |

#### Path Test Cases — `PathTestingTest` (10 tests)

| Test Method | Path | Input | Expected Output |
|---|---|---|---|
| `path1_fromIsNull_throwsException` | P1 | from=null, to=2026-03-31 | `IllegalArgumentException` |
| `path2_toIsNull_throwsException` | P2 | from=2026-03-01, to=null | `IllegalArgumentException` |
| `path3_fromAfterTo_throwsException` | P3 | from=2026-03-31, to=2026-03-01 | `IllegalArgumentException` ("Start date cannot be after end date") |
| `path4_emptyList_noTransactions` | P4 | range=[Mar 1, Mar 31], DAO empty | empty list |
| `path5_transactionInRange_included` | P5 | transaction on Mar 15; range=[Mar 1, Mar 31] | list with 1 transaction |
| `path5b_transactionOutsideRange_excluded` | P5b | transaction on Apr 1; range=[Mar 1, Mar 31] | empty list |
| `path5c_transactionOnFromBoundary_included` | P5 | transaction on Mar 1 (lower boundary) | list with 1 transaction (inclusive) |
| `path5d_transactionOnToBoundary_included` | P5 | transaction on Mar 31 (upper boundary) | list with 1 transaction (inclusive) |
| `path5e_sameDayRange_transactionIncluded` | P5 | range=[Mar 15, Mar 15]; transaction on Mar 15 | list with 1 transaction |
| `path5f_multipleTransactions_mixedResults` | P5 looped | 5 transactions: 1 before, 3 in range, 1 after | list with 3 transactions |

---

### 3.2 Data Flow Testing

**Function:** `TransactionController.filterByDateRange(LocalDate from, LocalDate to)`
**Coverage criterion:** All-Uses — every def-use pair for every variable is exercised.

#### Def-Use Pairs

| Pair ID | Variable | Defined At | Used At | Semantics |
|---|---|---|---|---|
| DU1 | `from` | parameter entry | null check (N2) | `from` reaches null guard |
| DU2 | `from` | parameter entry | `from.isAfter(to)` (N4) | `from` reaches order validation |
| DU3 | `from` | parameter entry | `!d.isBefore(from)` (N8) | `from` reaches lower-bound comparison in loop |
| DU4 | `to` | parameter entry | null check (N2) | `to` reaches null guard |
| DU5 | `to` | parameter entry | `from.isAfter(to)` (N4) | `to` reaches order validation |
| DU6 | `to` | parameter entry | `!d.isAfter(to)` (N8) | `to` reaches upper-bound comparison in loop |
| DU7 | `result` | `new ArrayList<>()` (N6) | `result.add(t)` (N9) | `result` reaches the add call |
| DU8 | `result` | `new ArrayList<>()` (N6) | `return result` (N10) | `result` reaches the return statement |
| DU9 | `t` | for-each binding (N7) | `t.getDate()` (N8) | `t` reaches getDate extraction |
| DU10 | `t` | for-each binding (N7) | `result.add(t)` (N9) | `t` reaches insertion into result |
| DU11 | `d` | `t.getDate()` (N8 entry) | `!d.isBefore(from)` (N8) | `d` reaches lower-bound check |
| DU12 | `d` | `t.getDate()` (N8 entry) | `!d.isAfter(to)` (N8) | `d` reaches upper-bound check |

#### Data Flow Test Cases — `DataFlowTestingTest` (23 tests)

Each test method is named `du[N]_description()` and explicitly states which pair(s) it exercises. The final test `allUsesCoverage_comprehensiveTest` exercises all 12 pairs in a single scenario (one transaction in range with valid dates). Selected tests:

| Test Method | Pair(s) | Scenario | Expected |
|---|---|---|---|
| `du1_fromDefinitionReachesNullCheck_fromIsNull` | DU1 | from=null | `IllegalArgumentException` |
| `du4_toDefinitionReachesNullCheck_toIsNull` | DU4 | to=null | `IllegalArgumentException` |
| `du2_du5_fromAndToReachIsAfterCheck_fromAfterTo` | DU2, DU5 | from=Mar 31, to=Mar 1 | `IllegalArgumentException` |
| `du3_du6_fromAndToReachLoopComparison_dateInRange` | DU3, DU6 | transaction on Mar 15; range=[Mar 1, Mar 31] | 1 result |
| `du3_fromUsedInIsBeforeCheck_dateOnFromBoundary` | DU3 | transaction on Mar 1 (from boundary) | 1 result |
| `du6_toUsedInIsAfterCheck_dateOnToBoundary` | DU6 | transaction on Mar 31 (to boundary) | 1 result |
| `du3_du6_dateBeforeFrom_excluded` | DU3, DU6 | transaction on Feb 28; range=[Mar 1, Mar 31] | empty list |
| `du3_du6_dateAfterTo_excluded` | DU3, DU6 | transaction on Apr 1; range=[Mar 1, Mar 31] | empty list |
| `du7_resultDefinitionReachesAdd_transactionAdded` | DU7 | transaction in range | `result.add()` called; size=1 |
| `du8_resultDefinitionReachesReturn_emptyList` | DU8 | DAO returns empty | empty list returned |
| `du9_tDefinitionReachesGetDate` | DU9 | 1 transaction; `t.getDate()` implicitly called | 1 result |
| `du10_tDefinitionReachesAdd` | DU10 | transaction in range | same object reference in result |
| `du11_dDefinitionReachesIsBefore` | DU11 | transaction on Mar 15; `!d.isBefore(Mar 1)` = TRUE | included |
| `du12_dDefinitionReachesIsAfter` | DU12 | transaction on Mar 15; `!d.isAfter(Mar 31)` = TRUE | included |
| `allUsesCoverage_comprehensiveTest` | DU1–DU12 | valid dates; 1 in-range transaction | all 12 pairs exercised |

---

## 4. Integration Testing

Integration tests use **real file I/O** (no mocks) and JUnit 5's `@TempDir` annotation to create isolated temporary directories per test. Two integration boundaries are tested:

| Boundary | Classes Integrated |
|---|---|
| Transaction subsystem | `TransactionController` ↔ `TransactionDAO` ↔ `FileManager` |
| Report/Budget subsystem | `ReportController` + `BudgetController` ↔ `TransactionDAO` + `BudgetDAO` ↔ `FileManager` |

### Transaction Integration Tests — `TransactionIntegrationTest` (8 tests)

| Test ID | Scenario | Layers Exercised | Expected Outcome |
|---|---|---|---|
| INT-T1 | `addTransaction` persists to CSV file | Controller → DAO → FileManager | File exists; header row + 1 data row with correct id, amount, category |
| INT-T2 | Transaction survives full round-trip (new DAO instance) | Write then fresh DAO reads | All 7 fields (id, amount, date, description, category, type) match original |
| INT-T3 | `removeTransaction` removes entry from file | Controller → DAO → FileManager | Fresh DAO load confirms only remaining transaction present |
| INT-T4 | `filterByCategory` works on persisted data | Controller → DAO | FOOD filter returns 2 results; TRANSPORT filter returns 1 result |
| INT-T5 | `filterByDateRange` across real file | Controller → DAO | February range filter returns exactly the 2 February transactions |
| INT-T6 | `searchByDescription` is case-insensitive on persisted data | Controller → DAO | "grocery" (lowercase) matches 2; "WALMART" (uppercase) matches 1 |
| INT-T7 | `exportToCSV` creates readable file with correct format | Controller → DAO → FileManager | File has header `id,amount,date,description,category,type` + 3 data rows |
| INT-T8 | Adding expense updates budget spending across controllers | TransactionController + BudgetController + both DAOs | After 150 FOOD expense on budget=500: no alert; after 300 more: WARNING alert |

### Report/Budget Integration Tests — `ReportIntegrationTest` (6 tests)

| Test ID | Scenario | Expected Outcome |
|---|---|---|
| INT-R1 | Monthly report calculates correct income and expense totals | income=3500, expense=600, balance=2900, period="March 2026" |
| INT-R2 | Monthly report ignores transactions from other months | March report: income=2000, expense=200; January report: income=1000, expense=100 |
| INT-R3 | Yearly report aggregates all months | income=4500, expense=600, balance=3900, period="2026" |
| INT-R4 | Category breakdown counts only EXPENSE transactions (INCOME excluded) | FOOD=150.0, TRANSPORT=200.0; SALARY key absent |
| INT-R5 | WARNING alert triggered when spending reaches 80% of budget | 1 WARNING alert; `isOverBudget()`=false |
| INT-R6 | EXCEEDED alert triggered when spending surpasses budget limit | 1 EXCEEDED alert; `isOverBudget()`=true; `getRemainingBudget()`=−50.0 |

---

## 5. Validation Testing

### 5.1 Boundary Value Testing

Four inputs with finite or meaningful boundaries were selected. For each, the minimum, just-above-minimum, maximum, just-above-maximum, below-minimum, and negative values are tested where applicable.

**Boundary definitions:**

| Input | Minimum | Maximum | Below Min | Above Max |
|---|---|---|---|---|
| Transaction amount | 0.01 | ∞ | 0.00, −1.00 | — |
| Description length (chars) | 1 | 100 | 0 (empty string) | 101 |
| Report month | 1 (January) | 12 (December) | 0 | 13 |
| Budget limit | 0.01 (> 0) | ∞ | 0.00, −100.00 | — |

**Test cases — `BoundaryValueTest` (15 tests):**

| Test ID | Input | Test Value | Expected |
|---|---|---|---|
| BV-01 | Amount | 0.01 (minimum) | accepted — transaction created |
| BV-02 | Amount | 0.00 (below minimum) | rejected — `IllegalArgumentException` |
| BV-03 | Amount | −1.00 (negative) | rejected — `IllegalArgumentException` |
| BV-04 | Amount | 0.02 (just above minimum) | accepted — transaction created |
| BV-05 | Description length | 1 character | accepted — transaction created |
| BV-06 | Description length | 0 characters (empty) | rejected — `IllegalArgumentException` |
| BV-07 | Description length | 100 characters (maximum) | accepted — transaction created |
| BV-08 | Description length | 101 characters (above maximum) | rejected — `IllegalArgumentException` |
| BV-09 | Month | 1 (January) | accepted — report generated |
| BV-10 | Month | 12 (December) | accepted — report generated |
| BV-11 | Month | 0 (below minimum) | rejected — `IllegalArgumentException` |
| BV-12 | Month | 13 (above maximum) | rejected — `IllegalArgumentException` |
| BV-13 | Budget limit | 0.01 (just above zero) | accepted — budget created |
| BV-14 | Budget limit | 0.00 (zero) | rejected — `IllegalArgumentException` |
| BV-15 | Budget limit | −100.00 (negative) | rejected — `IllegalArgumentException` |

---

### 5.2 Equivalence Class Testing

Inputs are partitioned into valid and invalid equivalence classes. One representative from each class is tested. Class IDs match test method annotations.

#### Equivalence Classes

**Transaction Amount:**

| Class ID | Description | Representative | Valid/Invalid |
|---|---|---|---|
| EC1 | Positive decimal ≥ 0.01 | 50.00 | Valid |
| EC2 | Zero | 0.00 | Invalid |
| EC3 | Negative number | −10.00 | Invalid |

**Transaction Description:**

| Class ID | Description | Representative | Valid/Invalid |
|---|---|---|---|
| EC5 | Non-blank string, 1–100 chars | "Lunch" | Valid |
| EC6 | null reference | null | Invalid |
| EC7 | Empty string or whitespace only | "   " | Invalid |
| EC8 | String longer than 100 chars | 101-character string | Invalid |

**Category:**

| Class ID | Description | Representative | Valid/Invalid |
|---|---|---|---|
| EC9 | Any valid `Category` enum value | `FOOD` | Valid |
| EC10 | null | null | Invalid |

**TransactionType:**

| Class ID | Description | Representative | Valid/Invalid |
|---|---|---|---|
| EC11 | Valid `TransactionType` enum | `EXPENSE`, `INCOME` | Valid |
| EC12 | null | null | Invalid |

**Date:**

| Class ID | Description | Representative | Valid/Invalid |
|---|---|---|---|
| EC13 | Valid `LocalDate` instance | 2026-03-15 | Valid |
| EC14 | null | null | Invalid |

**Search Keyword:**

| Class ID | Description | Representative | Valid/Invalid |
|---|---|---|---|
| EC15 | Non-empty, non-blank string | "grocery" | Valid |
| EC16 | null | null | Invalid |
| EC17 | Empty string or whitespace only | `""` / `"  "` | Invalid |

**Transaction ID (for `removeTransaction`):**

| Class ID | Description | Representative | Valid/Invalid |
|---|---|---|---|
| EC18 | Non-empty, non-blank string | "t1" | Valid |
| EC19 | null | null | Invalid |
| EC20 | Empty string or whitespace only | `""` / `"  "` | Invalid |

#### Equivalence Class Test Cases — `EquivalenceClassTest` (20 tests)

| Test ID | Class(es) | Input | Expected |
|---|---|---|---|
| EC-01 | EC1 | amount=50.00 | transaction accepted |
| EC-02 | EC2 | amount=0.00 | `IllegalArgumentException` |
| EC-03 | EC3 | amount=−10.00 | `IllegalArgumentException` |
| EC-05 | EC5 | description="Lunch" | transaction accepted |
| EC-06 | EC6 | description=null | `IllegalArgumentException` |
| EC-07 | EC7 | description="   " | `IllegalArgumentException` |
| EC-08 | EC8 | description=101-char string | `IllegalArgumentException` |
| EC-09 | EC9 | category=FOOD | transaction accepted |
| EC-10 | EC10 | category=null | `IllegalArgumentException` |
| EC-11 | EC11 | type=EXPENSE | transaction accepted |
| EC-11b | EC11 | type=INCOME | transaction accepted |
| EC-12 | EC12 | type=null | `IllegalArgumentException` |
| EC-13 | EC13 | date=2026-03-15 | transaction accepted |
| EC-14 | EC14 | date=null | `IllegalArgumentException` |
| EC-15 | EC15 | keyword="grocery" | returns matching transactions |
| EC-16 | EC16 | keyword=null | `IllegalArgumentException` |
| EC-17 | EC17 | keyword="" | `IllegalArgumentException` |
| EC-17b | EC17 | keyword="  " | `IllegalArgumentException` |
| EC-19 | EC19 | id=null | `IllegalArgumentException` |
| EC-20 | EC20 | id="" | `IllegalArgumentException` |

---

### 5.3 Decision Table Testing

**System under test:** `BudgetController.checkAllAlerts()` — maps budget state to alert message output.

#### Decision Table

| | Rule 1 | Rule 2 | Rule 3 | Rule 4 |
|---|:---:|:---:|:---:|:---:|
| **C1: Budget limit set for category?** | No | Yes | Yes | Yes |
| **C2: Spending > 100% of limit?** | — | No | No | Yes |
| **C3: Spending ≥ 80% of limit?** | — | No | Yes | Yes |
| **A1: No alert produced** | ✓ | ✓ | | |
| **A2: WARNING alert produced** | | | ✓ | |
| **A3: EXCEEDED alert produced** | | | | ✓ |

> **Boundary note:** `Budget.isExceeded()` uses strictly-greater (`currentSpending > limit`). Spending exactly equal to the limit satisfies `isNearLimit()` (≥ 80%) but does NOT satisfy `isExceeded()`. Therefore spending at exactly 100% of the limit triggers a **WARNING**, not EXCEEDED.

#### Decision Table Test Cases — `DecisionTableTest` (9 tests)

| Test ID | Rule | Conditions | Expected |
|---|---|---|---|
| DT-Rule1 | Rule 1 | No budget configured for category | no alerts returned |
| DT-Rule2a | Rule 2 | budget=100, spending=0 (0%) | no alert |
| DT-Rule2b | Rule 2 | budget=100, spending=79 (79%) | no alert |
| DT-Rule3a | Rule 3 | budget=100, spending=80 (exactly 80%) | WARNING alert |
| DT-Rule3b | Rule 3 | budget=100, spending=99 (99%) | WARNING alert |
| DT-Rule3c | Rule 3 | budget=100, spending=100 (exactly 100%) | WARNING alert (boundary — not EXCEEDED) |
| DT-Rule4a | Rule 4 | budget=100, spending=101 (101%) | EXCEEDED alert |
| DT-Rule4b | Rule 4 | budget=100, spending=150 (150%) | EXCEEDED alert |
| DT-Mixed | Rules 2, 3, 4 | FOOD 50% (Rule 2), TRANSPORT 85% (Rule 3), UTILITIES 110% (Rule 4) | 2 alerts: WARNING for Transport, EXCEEDED for Utilities |

---

### 5.4 State Transition Testing

Two objects with well-defined lifecycle states are modeled.

#### Transaction Lifecycle

```
                      addTransaction()
  [NON-EXISTENT] ─────────────────────► [SAVED]
        │                                   │
        │ deleteById()                       │ deleteById()
        │ (no-op — no exception)             ▼
        ▼                            [DELETED / NON-EXISTENT]
  [NON-EXISTENT]
```

States: `NON-EXISTENT`, `SAVED`, `DELETED` (indistinguishable from NON-EXISTENT after deletion)

#### Budget Lifecycle

```
                       setBudgetLimit()
    [NO_BUDGET] ─────────────────────────► [BUDGET_SET]
         │                                      │  ▲
         │ updateSpending()                     │  │ setBudgetLimit()
         │ (Rule 1 no-op — silent ignore)       │  │ (preserves currentSpending)
         ▼                               updateSpending() (accumulates)
    [NO_BUDGET]                                 │
                                           [BUDGET_SET]
```

States: `NO_BUDGET`, `BUDGET_SET`

#### State Transition Test Cases — `StateTransitionTest` (9 tests)

| Test ID | Object | Transition | Trigger | Expected |
|---|---|---|---|---|
| ST-Txn1 | Transaction | NON-EXISTENT → SAVED | `addTransaction()` | transaction retrievable via `getAll()` |
| ST-Txn2 | Transaction | SAVED → DELETED | `removeTransaction()` | transaction absent from `getAll()` |
| ST-Txn3 | Transaction | NON-EXISTENT → NON-EXISTENT | `deleteById()` on non-existent ID | no exception; no-op |
| ST-Txn4 | Transaction | DELETED → SAVED (new) | `addTransaction()` after delete | new transaction with different ID retrievable |
| ST-Txn5 | Transaction | Multiple: remove one of many | `removeTransaction()` on one ID | other transactions remain intact |
| ST-Budget1 | Budget | NO_BUDGET → BUDGET_SET | `setBudgetLimit()` | budget exists with `currentSpending`=0 |
| ST-Budget2 | Budget | BUDGET_SET → BUDGET_SET | `setBudgetLimit()` on existing budget | limit updated; prior `currentSpending` preserved |
| ST-Budget3 | Budget | NO_BUDGET → NO_BUDGET | `updateSpending()` when no budget set | silent no-op; no exception (Decision Table Rule 1) |
| ST-Budget4 | Budget | BUDGET_SET → BUDGET_SET | multiple `updateSpending()` calls | spending accumulates correctly across calls |

---

### 5.5 Use Case Testing

**Actor:** User
**System:** Budget Management System

#### Use Cases

**UC-01 — Record an expense and verify budget tracking**
- **Goal:** User records an expense and confirms the budget tracking updates correctly.
- **Preconditions:** FOOD budget limit set to 200.00.
- **Steps:** (1) Add FOOD expense of 50.00. (2) Call `updateSpending()`. (3) Call `checkAllAlerts()`.
- **Expected:** No alert returned (spending 25% < 80% threshold); remaining budget = 150.00.

**UC-02 — View monthly report with correct totals**
- **Goal:** User generates a monthly report and sees accurate income and expense aggregation.
- **Preconditions:** None.
- **Steps:** (1) Add salary income of 2000.00 in March 2026. (2) Add rent expense of 800.00 in March 2026. (3) Call `generateMonthlyReport(2026, 3)`.
- **Expected:** `totalIncome`=2000.00, `totalExpense`=800.00, `getBalance()`=1200.00, `getPeriod()`="March 2026".

**UC-03 — Budget exceeded triggers EXCEEDED alert**
- **Goal:** User overspends a budget category and sees an EXCEEDED alert.
- **Preconditions:** FOOD budget limit set to 100.00.
- **Steps:** (1) Add FOOD expense of 150.00. (2) Call `updateSpending()`. (3) Call `checkAllAlerts()`.
- **Expected:** 1 alert returned; alert message contains "EXCEEDED" and "Food"; `isOverBudget()`=true.

**UC-04 — Filter transactions by date range**
- **Goal:** User retrieves only the transactions that fall within a specific date range.
- **Preconditions:** Transactions added in January, February (×2), and March.
- **Steps:** (1) Add transactions in Jan 15, Feb 10, Feb 20, Mar 5. (2) Call `filterByDateRange(2026-02-01, 2026-02-28)`.
- **Expected:** Returns exactly 2 transactions; all returned transactions have dates in February 2026.

**UC-05 — Export transactions to CSV file**
- **Goal:** User exports all transactions to a CSV file for external use.
- **Preconditions:** 2 transactions exist: 100.00 FOOD EXPENSE and 2500.00 SALARY INCOME.
- **Steps:** (1) Call `exportToCSV(path)`. (2) Read the file at the given path.
- **Expected:** File exists; contains 3 lines (1 header + 2 data); header = `id,amount,date,description,category,type`.

**UC-06 — Record income and expense shows correct positive balance**
- **Goal:** User verifies the monthly report shows the correct positive balance after recording income and expenses.
- **Preconditions:** None.
- **Steps:** (1) Add income of 3000.00. (2) Add expense of 1500.00. (3) Generate monthly report.
- **Expected:** `totalIncome`=3000.00, `totalExpense`=1500.00, `getBalance()`=1500.00, `getSavingsRate()`=0.50 (50%).

**UC-07 — Budget at 80% triggers WARNING alert**
- **Goal:** User is warned when spending approaches the configured budget limit.
- **Preconditions:** FOOD budget limit set to 1000.00.
- **Steps:** (1) Add FOOD expense of 800.00 (exactly 80% of limit). (2) Call `checkAllAlerts()`.
- **Expected:** 1 WARNING alert returned; alert does NOT contain "EXCEEDED"; `isOverBudget()`=false.

**UC-08 — Search by description (case-insensitive)**
- **Goal:** User searches for transactions using a keyword and finds matches regardless of case.
- **Preconditions:** 2 transactions added: "Grocery Shopping at Walmart" (FOOD) and "Bus ticket" (TRANSPORT).
- **Steps:** (1) Call `searchByDescription("grocery")`.
- **Expected:** Returns 1 transaction; returned transaction's description contains "grocery" (case-insensitive match).

#### Use Case Test Cases — `UseCaseTest` (8 tests)

| Test ID | Use Case | Test Method | Expected Outcome |
|---|---|---|---|
| UC-01 | Record expense, verify budget tracking | `uc01_recordExpense_budgetUpdated` | no alert; remaining=150.00 |
| UC-02 | View monthly report | `uc02_monthlyReport_correctTotals` | income=2000, expense=800, balance=1200, period="March 2026" |
| UC-03 | Budget exceeded | `uc03_budgetExceeded_alertShown` | 1 EXCEEDED alert; `isOverBudget()`=true |
| UC-04 | Filter by date range | `uc04_filterByDateRange_correctResults` | 2 February transactions returned |
| UC-05 | Export to CSV | `uc05_exportToCSV_fileCreated` | file exists; 3 lines; correct header |
| UC-06 | Income and expense balance | `uc06_incomeAndExpense_positiveBalance` | balance=1500, savings rate=50% |
| UC-07 | Near-limit WARNING | `uc07_nearLimitWarning_at80Percent` | 1 WARNING alert; not EXCEEDED |
| UC-08 | Case-insensitive search | `uc08_searchByDescription_caseInsensitive` | 1 result matching "grocery" |

---

## 6. Test Execution Results

Run command: `mvn test`

All 279 tests pass with **BUILD SUCCESS**.

| Test Class | Layer | Tests | Result |
|---|---|---|---|
| `TransactionTest` | Model | 24 | PASS |
| `BudgetTest` | Model | 26 | PASS |
| `ReportTest` | Model | 18 | PASS |
| `FileManagerTest` | DAO | 13 | PASS |
| `TransactionDAOTest` | DAO | 13 | PASS |
| `BudgetDAOTest` | DAO | 12 | PASS |
| `TransactionControllerTest` | Controller | 24 | PASS |
| `BudgetControllerTest` | Controller | 27 | PASS |
| `ReportControllerTest` | Controller | 14 | PASS |
| `PathTestingTest` | Structural | 10 | PASS |
| `DataFlowTestingTest` | Structural | 23 | PASS |
| `TransactionIntegrationTest` | Integration | 8 | PASS |
| `ReportIntegrationTest` | Integration | 6 | PASS |
| `BoundaryValueTest` | Validation | 15 | PASS |
| `EquivalenceClassTest` | Validation | 20 | PASS |
| `DecisionTableTest` | Validation | 9 | PASS |
| `StateTransitionTest` | Validation | 9 | PASS |
| `UseCaseTest` | Validation | 8 | PASS |
| **TOTAL** | | **279** | **BUILD SUCCESS** |

---

*ENSE 375 – Software Testing and Validation*
