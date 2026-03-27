# ENSE 375 – Software Testing and Validation
## Project Description

---

## 1. Project Overview

In **groups/teams of 3** (self-selected), teams will design, develop, and comprehensively test a software application of their choice using:

- **Test-Driven Development (TDD)** methodology
- **Model-View-Controller (MVC)** architecture for code management
- **JUnit** for test cases (where applicable)

> **Main Objective:** Design the optimal test suite for the application.

### Required Testing Techniques

**Structural Testing** — Choose any function from the application and test it with:
- Path testing
- Data flow testing

**Integration Testing** — Choose a subset of units to perform integration testing.

**Validation Testing** — Perform validation of the application using:
- Boundary value testing
- Equivalence class testing
- Decision tables testing
- State transition testing
- Use case testing

### Application Scope

- Must be **approved by the instructor**
- Adequate scope for an approximate **2-month design/development timebox**
- Think **small-to-medium** project scope

**Example project ideas:**
- Howitzer firing simulator
- Text file encryption/decryption tool
- File-based library management system
- Student grade analyzer
- Personal expense tracker
- Task scheduler

> **Hint:** Follow along with what the Lab Instructor has in store. The labs will greatly assist while developing the application and writing the test cases.

---

## 2. Design and Architecture

Teams must go through the **structured engineering design process** (see Figure 1 below) while considering different factors that can impact testing of the final product.

Teams are encouraged to use the **V-shape software development lifecycle**.

### Engineering Design Process (Figure 1)

```
Define the Problem
        ↓
Do Background Research
        ↓
Specify Requirements
        ↓
Brainstorm, Evaluate, and Choose Solution  ←──────────────┐
        ↓                                                  │
Develop and Prototype Solution             ←──────────────┤
        ↓                                                  │
    Test Solution                          ←──────────────┤
        ↓               ↓                                  │
Solution Meets    Solution Meets Partially            Based on results
Requirements      or Not at All  ──────────────────→  and data, make
        ↓               ↓                             design changes,
    Communicate Results                               prototype, test
                                                      again, and review
                                                      new data.
```

---

## 3. Design Constraints

The solution must consider **at least 4** of the following design constraints:

| # | Constraint |
|---|---|
| a | Economic factors |
| b | Regulatory compliance (Security and Access) |
| c | Reliability |
| d | Sustainability and Environmental Factors |
| e | Ethics |
| f | Societal Impacts |

---

## 4. Teamwork

Each group must uphold the best teamwork strategy, including:

- Team formation
- Time management
- Conflict resolution

---

## 5. Presentation Requirements

A separate written report is **NOT required**, but the entire GitHub project should be **self-documenting**.

### Required Files

#### `REPORT.md`
Describes the use instructions of the application and provides other details according to the **Project File Template** (`Project_File_Template.pdf` on UR Courses).

#### `TESTING.md`
Describes the test plan. Must include:
- Minimum necessary information about all technical requirements
- Equivalence classes for equivalence class testing
- Use cases for use case testing
- Any other information needed to understand the test cases

> This should **not** be a long document but must contain minimum information about all technical requirements.

### GitHub Requirements

- The team's GitHub repository must remain **public** for the duration of ENSE 375 (at minimum)
- All code and test suites must be **well commented**

---

## 6. Project Deliverables

### Deliverable Items
- **a.** A functional and systematically tested prototype
- **b.** Technical report (`REPORT.md`)
- **c.** Testing report (`TESTING.md`)

### Delivery Schedule

| # | Deliverable | REPORT.md Section | Timeline | Date | Weight |
|---|---|---|---|---|---|
| 1 | Problem Definition | Section 2.1 | 3rd Week | Jan. 23 | 10% |
| 2 | Design Constraints and Requirements | Section 2.2 | 4th Week | Jan. 30 | 10% |
| 3 | Iterative Engineering Design Process (Solution 1 and 2) | Section 3.1 – 3.2 | 6th Week | Feb. 13 | 10% |
| 4 | Final Design (Solution 3), Implementation and Testing | Section 3.3 | 12th Week | March 27 | 60% |
| 5 | Collaborative Teamwork and Communication Skills | Section 4 – 5 | — | — | 10% |

### Important Notes

- **Teamwork and Project Management** sections must be filled simultaneously with every report component and updated regularly.
- **Weekly meetings** will be held to monitor progress.
- **Late penalty:** 10% per day for every late submission.
- All deadlines are **hard deadlines**. All artifacts must be submitted before **23:59:59**.

---

## 7. Submission Instructions

| Field | Details |
|---|---|
| **Final Due Date** | April 10, 2026, 23:59:59 |
| **Submission Method** | GitHub (commit often) |
| **Email Required** | Email GitHub project name and commit hashes to instructor via URCourses |
| **Marking** | Based on the rubric available on UR Courses |

> Be sure to commit often. The instructor will use GitHub to review your solution. Email the GitHub project name and the hashes of the commits being submitted to ensure the same code is reviewed.

---

*ENSE 375 – Software Testing and Validation*