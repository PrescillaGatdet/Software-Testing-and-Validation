# ENSE 375 – Software Testing and Validation

# Budget Management System

**Team Members:**
- Nyabijek Gatdet (200479720)
- Chop Peter Kur (200497265)
- Aubin Chriss (200490675)

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
5. [Project Management](#5-project-management)
6. [Conclusion and Future Work](#6-conclusion-and-future-work)
7. [References](#7-references)
8. [Appendix](#8-appendix)

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

| Constraint | Category | Description |
|------------|----------|-------------|
| **C1: No External Paid Services** | Economic | The system must be developed using only free and open-source tools and libraries, with zero cost for third-party APIs or services |
| **C2: Local Data Storage Only** | Security & Privacy | All user financial data must be stored locally on the user's machine; no data transmission to external servers is permitted |
| **C3: Data Integrity Preservation** | Reliability | The system must ensure no data loss during normal operations, including proper handling of unexpected shutdowns or invalid inputs |
| **C4: Cross-Platform Compatibility** | Sustainability | The application must run on major operating systems (Windows, macOS, Linux) without modification to the core codebase |
| **C5: No Personal Data Sharing** | Ethics | The system must not collect, transmit, or share any user personal or financial information with third parties |
| **C6: Accessible Interface** | Societal Impact | The system must provide clear text-based feedback and support keyboard navigation for users with varying technical abilities |
| **C7: JUnit Test Coverage** | Technical | All core business logic must have corresponding JUnit test cases with minimum 80% code coverage |
| **C8: MVC Architecture Compliance** | Technical | The implementation must strictly follow Model-View-Controller separation with no direct coupling between View and Model components |

---

## 3. Solution

*[Due: Feb 13 for Solutions 1 & 2, March 27 for Final Solution]*

This section describes the iterative design process followed to develop the Budget Management System. Multiple solutions were considered and evaluated based on their testability, maintainability, and ability to meet the design requirements and constraints.

### 3.1 Solution 1

*Description of first solution approach and reasons for not selecting it from a testing perspective.*

Monolithic Procedural Approach
Description
The first solution considered was a simple procedural approach where all functionality would be contained within a single Java class file. This design would use static methods for all operations including transaction recording, category management, budget calculations, and report generation. Data would be stored in simple arrays or ArrayLists as class-level static variables, and the user interface would consist of System.out.println() statements mixed directly with the business logic.
The single class (BudgetManager.java) would contain all variables (transactions, categories, budget amounts) and all methods (main, addTransaction, calculateTotal, printReport, saveToFile, displayMenu) in one place with no separation between components.
Reasons for Not Selecting (Testing Perspective)
This solution was rejected primarily due to significant testing limitations:

Tight Coupling – Business logic, data storage, and user interface code are intermingled in the same class. This makes it impossible to test calculation logic without also triggering console output, violating the principle of separation of concerns.
Static Method Testing Difficulty – Static methods cannot be easily mocked or overridden, making it difficult to isolate units for testing. JUnit tests would need to test the entire application flow rather than individual components.
No Dependency Injection – With hardcoded dependencies, we cannot substitute test doubles (mocks, stubs) for components like file I/O during testing. This prevents effective unit testing of business logic in isolation.
State Management Issues – Static variables maintain state across test executions, causing tests to interfere with each other. Each test would need extensive setup and teardown to reset the global state.
Path Testing Complexity – With all logic in one class, the control flow graph would be extremely complex, making it difficult to identify and test all paths systematically.
Limited Integration Testing – Since there are no separate modules, integration testing is not applicable, missing an important validation layer.
### 3.2 Solution 2

*Description of improved solution and its testing attributes.*

Layered Architecture (Two-Tier)
Description
The second solution introduced a layered architecture separating the application into two tiers: a Data Layer and an Application Layer.
The Data Layer would handle all data storage and retrieval operations using dedicated classes for file management, including FileManager.java (for reading, writing, and parsing CSV files) and TransactionDAO.java (for save, load, and delete operations).
The Application Layer would contain BudgetApplication.java with TransactionService for business logic, ReportService for calculations, and UserInterface for console I/O. However, the UI methods would still call service methods directly, keeping them partially coupled.
Improvements Over Solution 1

Separate Data Layer allows testing of file operations independently
Service classes can be instantiated (non-static), enabling better unit testing
Data Access Objects (DAO) pattern allows mocking of data operations

Reasons for Not Selecting (Testing Perspective)
While this solution improved testability, it was still rejected due to the following testing concerns:

View-Logic Coupling – The user interface code remains coupled with business logic in the Application Layer. Testing business rules still requires dealing with console I/O, complicating automated testing.
Incomplete Separation – Without a dedicated Controller layer, the flow of data between user input and business logic is not clearly defined. This makes it difficult to apply state transition testing effectively.
Limited Mock Injection – While the Data Layer can be mocked, the tight coupling in the Application Layer means we cannot easily inject mock services for testing the UI flow without triggering actual business logic.
Decision Table Testing Challenges – User input handling and business rule processing are combined, making it difficult to create clean decision tables that map inputs to outputs without considering UI state.
Integration Test Gaps – The two-tier structure provides only one integration boundary (Application-to-Data). A three-tier MVC architecture would provide more integration points, allowing more thorough integration testing.
Does Not Meet Constraint C8 – This solution does not comply with the MVC Architecture Compliance constraint, which requires strict separation with no direct coupling between View and Model components.

### 3.3 Final Solution
The final solution will implement a complete Model-View-Controller (MVC) architecture that addresses all the testing limitations identified in Solutions 1 and 2. This design will enable:

Independent unit testing of Model, View, and Controller components
Easy mock injection for isolated testing
Clear integration boundaries for integration testing
Proper separation supporting all validation testing techniques (boundary value, equivalence class, decision table, state transition, and use case testing)

#### 3.3.1 Components

*Component descriptions, purposes, testing methods, and block diagram.*

#### 3.3.2 Environmental, Societal, Safety, and Economic Considerations

*Discussion of how the design addresses these constraints.*

#### 3.3.3 Test Cases and Results

*Test suite design and execution results. See also [TESTING.md](TESTING.md).*

#### 3.3.4 Limitations

*Known limitations of the solution.*

---

## 4. Team Work

### 4.1 Meeting 1

**Time:** Month Date, Year, hour:minutes am/pm to hour:minutes am/pm  
**Agenda:** Distribution of Project Tasks

| Team Member | Previous Task | Completion State | Next Task |
|-------------|---------------|------------------|-----------|
| Team member 1 | N/A | N/A | Task 1 |
| Team member 2 | N/A | N/A | Task 2 |
| Team member 3 | N/A | N/A | Task 3 |

### 4.2 Meeting 2

**Time:** Month Date, Year, hour:minutes am/pm to hour:minutes am/pm  
**Agenda:** Review of Individual Progress

| Team Member | Previous Task | Completion State | Next Task |
|-------------|---------------|------------------|-----------|
| Team member 1 | Task 1 | 80% | Task 1, Task 5 |
| Team member 2 | Task 2 | 50% | Task 2 |
| Team member 3 | Task 3 | 100% | Task 6 |

### 4.3 Meeting 3

*[To be completed]*

### 4.4 Meeting 4

*[To be completed]*

---

## 5. Project Management

*[Gantt chart showing project timeline, tasks, predecessors, slack time, and critical path to be added]*

---

## 6. Conclusion and Future Work

*[To be completed]*

---

## 7. References

*[IEEE reference style - to be added as sources are used]*

---

## 8. Appendix

*[Additional supporting information if needed]*
