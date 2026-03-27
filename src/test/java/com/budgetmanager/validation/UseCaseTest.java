package com.budgetmanager.validation;

import com.budgetmanager.controller.BudgetController;
import com.budgetmanager.controller.ReportController;
import com.budgetmanager.controller.TransactionController;
import com.budgetmanager.dao.BudgetDAO;
import com.budgetmanager.dao.FileManager;
import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Category;
import com.budgetmanager.model.Report;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Use Case Testing for the Budget Management System.
 *
 * Tests complete end-to-end user scenarios from the user's perspective.
 * Each test represents a real use case that would be evaluated by the professor.
 *
 * Stage 7 of IMPLEMENTATION_PLAN.md — Validation Testing (Technique 5 of 5)
 *
 * Use Cases Tested:
 *   UC-01: Record an expense and verify budget tracking
 *   UC-02: View monthly report with correct totals
 *   UC-03: Budget exceeded triggers EXCEEDED alert
 *   UC-04: Filter transactions by date range
 *   UC-05: Export transactions to CSV file
 *   UC-06: Record income and check positive balance
 *   UC-07: Set budget and check near-limit WARNING
 *   UC-08: Search transactions by description
 *
 * Rubric relevance: Required by project_description.md —
 * "Validation Testing: Use case testing"
 */
@DisplayName("Stage 7 — Use Case Tests")
class UseCaseTest {

    @TempDir
    Path tempDir;

    private TransactionController transactionController;
    private ReportController reportController;
    private BudgetController budgetController;
    private FileManager fileManager;

    @BeforeEach
    void setUp() {
        String transactionsPath = tempDir.resolve("transactions.csv").toString();
        String budgetsPath = tempDir.resolve("budgets.csv").toString();
        fileManager = new FileManager();
        TransactionDAO transactionDAO = new TransactionDAO(transactionsPath, fileManager);
        BudgetDAO budgetDAO = new BudgetDAO(budgetsPath, fileManager);
        transactionController = new TransactionController(transactionDAO, fileManager);
        reportController = new ReportController(transactionDAO);
        budgetController = new BudgetController(budgetDAO);
    }

    // =========================================================================
    // UC-01: Record an expense, budget updated
    // =========================================================================

    @Test
    @DisplayName("UC-01: Record expense transaction and verify budget tracking")
    void uc01_recordExpense_budgetUpdated() throws IOException {
        // Scenario: User sets a budget, adds an expense, budget spending is updated
        // Actor: User
        // Goal: Add expense transaction and see budget updated

        // Step 1: Set FOOD budget to 200
        budgetController.setBudgetLimit(Category.FOOD, 200.00);

        // Step 2: Add FOOD expense of 50
        Transaction expense = transactionController.addTransaction(
            50.00,
            LocalDate.of(2026, 3, 15),
            "Lunch at restaurant",
            Category.FOOD,
            TransactionType.EXPENSE
        );

        // Step 3: Update budget spending (as Main.java does)
        budgetController.updateSpending(Category.FOOD, expense.getAmount());

        // Step 4: Check alerts - should be no alert (50/200 = 25% < 80%)
        List<String> alerts = budgetController.checkAllAlerts();

        // Verification
        assertNotNull(expense, "Expense should be created");
        assertTrue(alerts.isEmpty(), "No alert expected at 25% spending");
        assertEquals(150.00, budgetController.getRemainingBudget(Category.FOOD), 0.001,
            "Remaining budget should be 200 - 50 = 150");
    }

    // =========================================================================
    // UC-02: View monthly report with correct totals
    // =========================================================================

    @Test
    @DisplayName("UC-02: Monthly report shows correct income/expense totals")
    void uc02_monthlyReport_correctTotals() throws IOException {
        // Scenario: User adds income and expenses, views monthly report
        // Actor: User
        // Goal: See correct income/expense totals for a month

        // Step 1: Add salary income (INCOME) in March 2026
        transactionController.addTransaction(
            2000.00,
            LocalDate.of(2026, 3, 1),
            "Monthly salary",
            Category.SALARY,
            TransactionType.INCOME
        );

        // Step 2: Add rent expense (EXPENSE) in March 2026
        transactionController.addTransaction(
            800.00,
            LocalDate.of(2026, 3, 5),
            "Monthly rent",
            Category.UTILITIES,
            TransactionType.EXPENSE
        );

        // Step 3: Generate monthly report for March 2026
        Report report = reportController.generateMonthlyReport(2026, 3);

        // Verification
        assertEquals(2000.00, report.getTotalIncome(), 0.001,
            "Total income should be 2000");
        assertEquals(800.00, report.getTotalExpense(), 0.001,
            "Total expense should be 800");
        assertEquals(1200.00, report.getBalance(), 0.001,
            "Balance should be 2000 - 800 = 1200");
        assertEquals("March 2026", report.getPeriod(),
            "Period should be 'March 2026'");
    }

    // =========================================================================
    // UC-03: Budget exceeded triggers alert
    // =========================================================================

    @Test
    @DisplayName("UC-03: Budget exceeded shows EXCEEDED alert")
    void uc03_budgetExceeded_alertShown() throws IOException {
        // Scenario: User exceeds budget, sees EXCEEDED alert
        // Actor: User
        // Goal: See EXCEEDED alert after over-spending

        // Step 1: Set FOOD budget to 100
        budgetController.setBudgetLimit(Category.FOOD, 100.00);

        // Step 2: Add FOOD expense of 150 (exceeds budget)
        Transaction expense = transactionController.addTransaction(
            150.00,
            LocalDate.of(2026, 3, 15),
            "Large grocery haul",
            Category.FOOD,
            TransactionType.EXPENSE
        );
        budgetController.updateSpending(Category.FOOD, expense.getAmount());

        // Step 3: Check alerts
        List<String> alerts = budgetController.checkAllAlerts();

        // Verification
        assertEquals(1, alerts.size(), "Should have 1 alert");
        assertTrue(alerts.get(0).contains("EXCEEDED"),
            "Alert should be EXCEEDED");
        assertTrue(alerts.get(0).contains("Food"),
            "Alert should mention Food category");
        assertTrue(budgetController.isOverBudget(Category.FOOD),
            "isOverBudget should return true");
    }

    // =========================================================================
    // UC-04: Filter transactions by date range
    // =========================================================================

    @Test
    @DisplayName("UC-04: Filter transactions by date range returns correct results")
    void uc04_filterByDateRange_correctResults() throws IOException {
        // Scenario: User filters transactions to see only February transactions
        // Actor: User
        // Goal: See only transactions in selected date range

        // Step 1: Add transactions in January, February, and March
        transactionController.addTransaction(
            100.00, LocalDate.of(2026, 1, 15), "January expense",
            Category.FOOD, TransactionType.EXPENSE
        );
        Transaction feb1 = transactionController.addTransaction(
            200.00, LocalDate.of(2026, 2, 10), "February expense 1",
            Category.FOOD, TransactionType.EXPENSE
        );
        Transaction feb2 = transactionController.addTransaction(
            150.00, LocalDate.of(2026, 2, 20), "February expense 2",
            Category.TRANSPORT, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            300.00, LocalDate.of(2026, 3, 5), "March expense",
            Category.ENTERTAINMENT, TransactionType.EXPENSE
        );

        // Step 2: Filter by February date range
        List<Transaction> febTransactions = transactionController.filterByDateRange(
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28)
        );

        // Verification
        assertEquals(2, febTransactions.size(),
            "Should return only 2 February transactions");
        assertTrue(febTransactions.stream().anyMatch(t -> t.getId().equals(feb1.getId())));
        assertTrue(febTransactions.stream().anyMatch(t -> t.getId().equals(feb2.getId())));
        for (Transaction t : febTransactions) {
            assertEquals(2, t.getDate().getMonthValue(),
                "All transactions should be in February");
        }
    }

    // =========================================================================
    // UC-05: Export transactions to CSV
    // =========================================================================

    @Test
    @DisplayName("UC-05: Export transactions creates CSV file with correct content")
    void uc05_exportToCSV_fileCreated() throws IOException {
        // Scenario: User exports transactions to a CSV file
        // Actor: User
        // Goal: Get a CSV file with all transaction data

        // Step 1: Add 2 transactions
        transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),
            "Food expense",
            Category.FOOD,
            TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            2500.00,
            LocalDate.of(2026, 3, 1),
            "Monthly salary",
            Category.SALARY,
            TransactionType.INCOME
        );

        // Step 2: Export to CSV
        String exportPath = tempDir.resolve("export.csv").toString();
        transactionController.exportToCSV(exportPath);

        // Step 3: Read and verify file content
        assertTrue(Files.exists(Path.of(exportPath)),
            "Export file should exist");
        List<String> lines = Files.readAllLines(Path.of(exportPath));

        // Verification
        assertEquals(3, lines.size(),
            "File should have header + 2 data rows");
        assertEquals("id,amount,date,description,category,type", lines.get(0),
            "First line should be CSV header");
        assertTrue(lines.stream().anyMatch(l -> l.contains("FOOD") && l.contains("EXPENSE")),
            "Should contain FOOD expense");
        assertTrue(lines.stream().anyMatch(l -> l.contains("SALARY") && l.contains("INCOME")),
            "Should contain SALARY income");
    }

    // =========================================================================
    // UC-06: Record income and check positive balance
    // =========================================================================

    @Test
    @DisplayName("UC-06: Record income and expense shows correct positive balance and savings rate")
    void uc06_incomeAndExpense_positiveBalance() throws IOException {
        // Scenario: User records income and expenses, checks balance
        // Actor: User
        // Goal: Monthly report shows correct positive balance

        // Step 1: Add income of 3000
        transactionController.addTransaction(
            3000.00,
            LocalDate.of(2026, 3, 1),
            "Salary",
            Category.SALARY,
            TransactionType.INCOME
        );

        // Step 2: Add expense of 1500
        transactionController.addTransaction(
            1500.00,
            LocalDate.of(2026, 3, 15),
            "Various expenses",
            Category.OTHER,
            TransactionType.EXPENSE
        );

        // Step 3: Generate monthly report
        Report report = reportController.generateMonthlyReport(2026, 3);

        // Verification
        assertEquals(3000.00, report.getTotalIncome(), 0.001);
        assertEquals(1500.00, report.getTotalExpense(), 0.001);
        assertEquals(1500.00, report.getBalance(), 0.001,
            "Balance should be 3000 - 1500 = 1500");
        assertEquals(0.50, report.getSavingsRate(), 0.001,
            "Savings rate should be 1500/3000 = 0.50 (50%)");
    }

    // =========================================================================
    // UC-07: Near-limit WARNING at 80%
    // =========================================================================

    @Test
    @DisplayName("UC-07: Budget at 80% triggers WARNING alert")
    void uc07_nearLimitWarning_at80Percent() throws IOException {
        // Scenario: User approaches budget limit, sees WARNING
        // Actor: User
        // Goal: See WARNING at 80% spending

        // Step 1: Set budget to 1000
        budgetController.setBudgetLimit(Category.FOOD, 1000.00);

        // Step 2: Add expense of 800 (exactly 80%)
        Transaction expense = transactionController.addTransaction(
            800.00,
            LocalDate.of(2026, 3, 15),
            "Large grocery shopping",
            Category.FOOD,
            TransactionType.EXPENSE
        );
        budgetController.updateSpending(Category.FOOD, expense.getAmount());

        // Step 3: Check alerts
        List<String> alerts = budgetController.checkAllAlerts();

        // Verification
        assertEquals(1, alerts.size(), "Should have 1 alert");
        assertTrue(alerts.get(0).contains("WARNING"),
            "Alert should be WARNING at exactly 80%");
        assertFalse(alerts.get(0).contains("EXCEEDED"),
            "Should NOT be EXCEEDED at 80%");
        assertFalse(budgetController.isOverBudget(Category.FOOD),
            "isOverBudget should be false at 80%");
    }

    // =========================================================================
    // UC-08: Search by description
    // =========================================================================

    @Test
    @DisplayName("UC-08: Search by description finds transaction (case-insensitive)")
    void uc08_searchByDescription_caseInsensitive() throws IOException {
        // Scenario: User searches for transactions by keyword
        // Actor: User
        // Goal: Keyword search returns matching results

        // Step 1: Add transaction with specific description
        Transaction t = transactionController.addTransaction(
            75.00,
            LocalDate.of(2026, 3, 15),
            "Grocery Shopping at Walmart",
            Category.FOOD,
            TransactionType.EXPENSE
        );

        // Add another transaction that shouldn't match
        transactionController.addTransaction(
            50.00,
            LocalDate.of(2026, 3, 16),
            "Bus ticket",
            Category.TRANSPORT,
            TransactionType.EXPENSE
        );

        // Step 2: Search with lowercase keyword
        List<Transaction> results = transactionController.searchByDescription("grocery");

        // Verification
        assertEquals(1, results.size(),
            "Should find exactly 1 transaction matching 'grocery'");
        assertEquals(t.getId(), results.get(0).getId(),
            "Found transaction should match the grocery shopping one");
        assertTrue(results.get(0).getDescription().toLowerCase().contains("grocery"),
            "Description should contain 'grocery' (case-insensitive)");
    }
}
