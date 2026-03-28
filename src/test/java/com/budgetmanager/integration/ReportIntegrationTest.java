package com.budgetmanager.integration;

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
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("Stage 6 — Report Integration Tests")
class ReportIntegrationTest {

    @TempDir
    Path tempDir;

    private String transactionsPath;
    private String budgetsPath;
    private FileManager fileManager;
    private TransactionDAO transactionDAO;
    private BudgetDAO budgetDAO;
    private TransactionController transactionController;
    private ReportController reportController;
    private BudgetController budgetController;

    @BeforeEach
    void setUp() {
        transactionsPath = tempDir.resolve("transactions.csv").toString();
        budgetsPath = tempDir.resolve("budgets.csv").toString();
        fileManager = new FileManager();
        transactionDAO = new TransactionDAO(transactionsPath, fileManager);
        budgetDAO = new BudgetDAO(budgetsPath, fileManager);
        transactionController = new TransactionController(transactionDAO, fileManager);
        reportController = new ReportController(transactionDAO);
        budgetController = new BudgetController(budgetDAO);
    }

    // =========================================================================
    // Test 1: monthlyReport_correctTotals
    // =========================================================================

    @Test
    @DisplayName("Monthly report calculates correct income and expense totals")
    void monthlyReport_correctTotals() throws IOException {
        // Arrange: Add 2 income + 3 expense transactions in March 2026
        transactionController.addTransaction(
            3000.00, LocalDate.of(2026, 3, 1), "Monthly salary",
            Category.SALARY, TransactionType.INCOME
        );
        transactionController.addTransaction(
            500.00, LocalDate.of(2026, 3, 15), "Freelance payment",
            Category.INVESTMENT, TransactionType.INCOME
        );
        transactionController.addTransaction(
            400.00, LocalDate.of(2026, 3, 5), "Rent payment",
            Category.UTILITIES, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            150.00, LocalDate.of(2026, 3, 10), "Groceries",
            Category.FOOD, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            50.00, LocalDate.of(2026, 3, 20), "Entertainment",
            Category.ENTERTAINMENT, TransactionType.EXPENSE
        );

        // Act: Generate monthly report for March 2026
        Report report = reportController.generateMonthlyReport(2026, 3);

        // Assert: Totals should match
        assertEquals(3500.00, report.getTotalIncome(), 0.001,
            "Total income should be 3000 + 500 = 3500");
        assertEquals(600.00, report.getTotalExpense(), 0.001,
            "Total expense should be 400 + 150 + 50 = 600");
        assertEquals(2900.00, report.getBalance(), 0.001,
            "Balance should be 3500 - 600 = 2900");
        assertEquals("March 2026", report.getPeriod(),
            "Period should be 'March 2026'");
    }

    // =========================================================================
    // Test 2: monthlyReport_ignoresOtherMonths
    // =========================================================================

    @Test
    @DisplayName("Monthly report ignores transactions from other months")
    void monthlyReport_ignoresOtherMonths() throws IOException {
        // Arrange: Add transactions in January and March
        transactionController.addTransaction(
            1000.00, LocalDate.of(2026, 1, 15), "January salary",
            Category.SALARY, TransactionType.INCOME
        );
        transactionController.addTransaction(
            100.00, LocalDate.of(2026, 1, 20), "January expense",
            Category.FOOD, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            2000.00, LocalDate.of(2026, 3, 1), "March salary",
            Category.SALARY, TransactionType.INCOME
        );
        transactionController.addTransaction(
            200.00, LocalDate.of(2026, 3, 10), "March expense",
            Category.FOOD, TransactionType.EXPENSE
        );

        // Act: Generate report for March only
        Report marchReport = reportController.generateMonthlyReport(2026, 3);

        // Assert: Only March transactions should be included
        assertEquals(2000.00, marchReport.getTotalIncome(), 0.001,
            "Should only include March income (2000)");
        assertEquals(200.00, marchReport.getTotalExpense(), 0.001,
            "Should only include March expense (200)");
        assertEquals("March 2026", marchReport.getPeriod());

        // Verify January report separately
        Report janReport = reportController.generateMonthlyReport(2026, 1);
        assertEquals(1000.00, janReport.getTotalIncome(), 0.001);
        assertEquals(100.00, janReport.getTotalExpense(), 0.001);
    }

    // =========================================================================
    // Test 3: yearlyReport_aggregatesAllMonths
    // =========================================================================

    @Test
    @DisplayName("Yearly report aggregates transactions from all months")
    void yearlyReport_aggregatesAllMonths() throws IOException {
        // Arrange: Add transactions across Jan, Jun, and Dec 2026
        transactionController.addTransaction(
            1000.00, LocalDate.of(2026, 1, 1), "January income",
            Category.SALARY, TransactionType.INCOME
        );
        transactionController.addTransaction(
            100.00, LocalDate.of(2026, 1, 15), "January expense",
            Category.FOOD, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            2000.00, LocalDate.of(2026, 6, 1), "June income",
            Category.SALARY, TransactionType.INCOME
        );
        transactionController.addTransaction(
            200.00, LocalDate.of(2026, 6, 15), "June expense",
            Category.TRANSPORT, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            1500.00, LocalDate.of(2026, 12, 1), "December income",
            Category.INVESTMENT, TransactionType.INCOME
        );
        transactionController.addTransaction(
            300.00, LocalDate.of(2026, 12, 25), "December expense",
            Category.ENTERTAINMENT, TransactionType.EXPENSE
        );

        // Act: Generate yearly report for 2026
        Report report = reportController.generateYearlyReport(2026);

        // Assert: All months should be aggregated
        assertEquals(4500.00, report.getTotalIncome(), 0.001,
            "Total income should be 1000 + 2000 + 1500 = 4500");
        assertEquals(600.00, report.getTotalExpense(), 0.001,
            "Total expense should be 100 + 200 + 300 = 600");
        assertEquals(3900.00, report.getBalance(), 0.001,
            "Balance should be 4500 - 600 = 3900");
        assertEquals("2026", report.getPeriod(),
            "Period should be '2026'");
    }

    // =========================================================================
    // Test 4: categoryBreakdown_matchesExpenses
    // =========================================================================

    @Test
    @DisplayName("Category breakdown correctly sums expenses per category")
    void categoryBreakdown_matchesExpenses() throws IOException {
        // Arrange: Add multiple EXPENSE transactions (INCOME should be excluded)
        transactionController.addTransaction(
            100.00, LocalDate.of(2026, 3, 1), "Food expense 1",
            Category.FOOD, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            50.00, LocalDate.of(2026, 3, 5), "Food expense 2",
            Category.FOOD, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            200.00, LocalDate.of(2026, 3, 10), "Transport expense",
            Category.TRANSPORT, TransactionType.EXPENSE
        );
        // This INCOME transaction should NOT appear in breakdown
        transactionController.addTransaction(
            3000.00, LocalDate.of(2026, 3, 1), "Salary",
            Category.SALARY, TransactionType.INCOME
        );

        // Act: Generate category breakdown
        Map<Category, Double> breakdown = reportController.generateCategoryBreakdown();

        // Assert: Only EXPENSE categories should be present with correct totals
        assertEquals(150.00, breakdown.get(Category.FOOD), 0.001,
            "FOOD should be 100 + 50 = 150");
        assertEquals(200.00, breakdown.get(Category.TRANSPORT), 0.001,
            "TRANSPORT should be 200");
        assertNull(breakdown.get(Category.SALARY),
            "SALARY (INCOME type) should not appear in breakdown");
        assertEquals(2, breakdown.size(),
            "Only 2 categories should have expenses");
    }

    // =========================================================================
    // Test 5: budgetAlert_triggeredAfterExpense
    // =========================================================================

    @Test
    @DisplayName("WARNING alert is triggered when spending reaches 80% of budget")
    void budgetAlert_triggeredAfterExpense() throws IOException {
        // Arrange: Set FOOD budget to 100
        budgetController.setBudgetLimit(Category.FOOD, 100.00);

        // Act: Add expense that brings spending to 95% (warning threshold is 80%)
        Transaction t = transactionController.addTransaction(
            95.00, LocalDate.of(2026, 3, 15), "Grocery shopping",
            Category.FOOD, TransactionType.EXPENSE
        );

        // Simulate Main.java behavior: update budget on expense
        if (t.getType() == TransactionType.EXPENSE) {
            budgetController.updateSpending(t.getCategory(), t.getAmount());
        }

        // Assert: Should trigger WARNING (not EXCEEDED since 95 <= 100)
        List<String> alerts = budgetController.checkAllAlerts();
        assertEquals(1, alerts.size(), "Should have exactly 1 alert");
        assertTrue(alerts.get(0).contains("WARNING"),
            "Alert should be a WARNING (not EXCEEDED)");
        assertTrue(alerts.get(0).contains("Food"),
            "Alert should mention Food category");

        // Verify budget state
        assertFalse(budgetController.isOverBudget(Category.FOOD),
            "Should not be over budget (95 <= 100)");
    }

    // =========================================================================
    // Test 6: budgetExceeded_triggeredAfterExpense
    // =========================================================================

    @Test
    @DisplayName("EXCEEDED alert is triggered when spending exceeds budget limit")
    void budgetExceeded_triggeredAfterExpense() throws IOException {
        // Arrange: Set FOOD budget to 100
        budgetController.setBudgetLimit(Category.FOOD, 100.00);

        // Act: Add expense that exceeds the budget (150 > 100)
        Transaction t = transactionController.addTransaction(
            150.00, LocalDate.of(2026, 3, 15), "Large grocery haul",
            Category.FOOD, TransactionType.EXPENSE
        );

        // Simulate Main.java behavior: update budget on expense
        if (t.getType() == TransactionType.EXPENSE) {
            budgetController.updateSpending(t.getCategory(), t.getAmount());
        }

        // Assert: Should trigger EXCEEDED alert
        List<String> alerts = budgetController.checkAllAlerts();
        assertEquals(1, alerts.size(), "Should have exactly 1 alert");
        assertTrue(alerts.get(0).contains("EXCEEDED"),
            "Alert should be EXCEEDED (spending > limit)");
        assertTrue(alerts.get(0).contains("Food"),
            "Alert should mention Food category");
        assertTrue(alerts.get(0).contains("150"),
            "Alert should mention current spending amount");

        // Verify budget state
        assertTrue(budgetController.isOverBudget(Category.FOOD),
            "Should be over budget (150 > 100)");
        assertEquals(-50.00, budgetController.getRemainingBudget(Category.FOOD), 0.001,
            "Remaining budget should be -50 (100 - 150)");
    }
}
