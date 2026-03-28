package com.budgetmanager.validation;

import com.budgetmanager.controller.BudgetController;
import com.budgetmanager.controller.ReportController;
import com.budgetmanager.controller.TransactionController;
import com.budgetmanager.dao.BudgetDAO;
import com.budgetmanager.dao.FileManager;
import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Budget;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Boundary Value Testing for the Budget Management System.
 *
 * Tests the edges of valid input ranges for each input parameter.
 * For each boundary: minimum, minimum+1, nominal, maximum-1, maximum,
 * below minimum, and above maximum values are tested.
 *
 * Inputs tested:
 *   - Transaction amount: min=0.01, below=0.00, negative
 *   - Description length: min=1, max=100, below=0 (empty), above=101
 *   - Month for reports: min=1, max=12, below=0, above=13
 *   - Budget limit: min>0, below=0, negative
 */
@DisplayName("Stage 7 — Boundary Value Tests")
class BoundaryValueTest {

    @TempDir
    Path tempDir;

    private TransactionController transactionController;
    private ReportController reportController;
    private BudgetController budgetController;

    @BeforeEach
    void setUp() {
        String transactionsPath = tempDir.resolve("transactions.csv").toString();
        String budgetsPath = tempDir.resolve("budgets.csv").toString();
        FileManager fileManager = new FileManager();
        TransactionDAO transactionDAO = new TransactionDAO(transactionsPath, fileManager);
        BudgetDAO budgetDAO = new BudgetDAO(budgetsPath, fileManager);
        transactionController = new TransactionController(transactionDAO, fileManager);
        reportController = new ReportController(transactionDAO);
        budgetController = new BudgetController(budgetDAO);
    }

    // =========================================================================
    // Amount Boundary Tests
    // =========================================================================

    @Test
    @DisplayName("BV-01: Amount at minimum (0.01) is accepted")
    void amount_atMinimum_accepted() throws IOException {
        // Boundary: MIN_AMOUNT = 0.01
        Transaction t = transactionController.addTransaction(
            0.01,  // Exactly at minimum
            LocalDate.of(2026, 3, 15),
            "Minimum amount test",
            Category.OTHER,
            TransactionType.EXPENSE
        );

        assertNotNull(t, "Transaction should be created");
        assertEquals(0.01, t.getAmount(), 0.001, "Amount should be 0.01");
    }

    @Test
    @DisplayName("BV-02: Amount below minimum (0.00) is rejected")
    void amount_belowMinimum_rejected() {
        // Below boundary: 0.00 < MIN_AMOUNT (0.01)
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                0.00,  // Below minimum
                LocalDate.of(2026, 3, 15),
                "Zero amount test",
                Category.OTHER,
                TransactionType.EXPENSE
            )
        );
        assertTrue(ex.getMessage().contains("0.01"),
            "Error message should mention minimum amount");
    }

    @Test
    @DisplayName("BV-03: Negative amount (-1.00) is rejected")
    void amount_negative_rejected() {
        // Invalid: negative amount
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                -1.00,  // Negative
                LocalDate.of(2026, 3, 15),
                "Negative amount test",
                Category.OTHER,
                TransactionType.EXPENSE
            )
        );
        assertTrue(ex.getMessage().contains("0.01"),
            "Error message should mention minimum amount");
    }

    @Test
    @DisplayName("BV-04: Amount just above minimum (0.02) is accepted")
    void amount_justAboveMinimum_accepted() throws IOException {
        // Min + 1 penny
        Transaction t = transactionController.addTransaction(
            0.02,
            LocalDate.of(2026, 3, 15),
            "Just above minimum",
            Category.OTHER,
            TransactionType.EXPENSE
        );

        assertNotNull(t);
        assertEquals(0.02, t.getAmount(), 0.001);
    }

    // =========================================================================
    // Description Length Boundary Tests
    // =========================================================================

    @Test
    @DisplayName("BV-05: Description with 1 character (minimum) is accepted")
    void description_oneChar_accepted() throws IOException {
        // Boundary: minimum length = 1
        Transaction t = transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),
            "X",  // 1 character
            Category.OTHER,
            TransactionType.EXPENSE
        );

        assertNotNull(t);
        assertEquals("X", t.getDescription());
    }

    @Test
    @DisplayName("BV-06: Empty description is rejected")
    void description_empty_rejected() {
        // Below boundary: 0 characters
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                100.00,
                LocalDate.of(2026, 3, 15),
                "",  // Empty
                Category.OTHER,
                TransactionType.EXPENSE
            )
        );
        assertTrue(ex.getMessage().toLowerCase().contains("description"),
            "Error message should mention description");
    }

    @Test
    @DisplayName("BV-07: Description with 100 characters (maximum) is accepted")
    void description_100chars_accepted() throws IOException {
        // Boundary: MAX_DESCRIPTION_LENGTH = 100
        String desc100 = "A".repeat(100);  // Exactly 100 characters
        Transaction t = transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),
            desc100,
            Category.OTHER,
            TransactionType.EXPENSE
        );

        assertNotNull(t);
        assertEquals(100, t.getDescription().length());
    }

    @Test
    @DisplayName("BV-08: Description with 101 characters (above maximum) is rejected")
    void description_101chars_rejected() {
        // Above boundary: 101 > MAX_DESCRIPTION_LENGTH (100)
        String desc101 = "A".repeat(101);  // 101 characters
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                100.00,
                LocalDate.of(2026, 3, 15),
                desc101,
                Category.OTHER,
                TransactionType.EXPENSE
            )
        );
        assertTrue(ex.getMessage().contains("100"),
            "Error message should mention maximum length");
    }

    // =========================================================================
    // Month Boundary Tests (for generateMonthlyReport)
    // =========================================================================

    @Test
    @DisplayName("BV-09: Month 1 (January, minimum) is accepted")
    void month_1_accepted() throws IOException {
        // Boundary: minimum valid month = 1
        Report report = reportController.generateMonthlyReport(2026, 1);

        assertNotNull(report);
        assertEquals("January 2026", report.getPeriod());
    }

    @Test
    @DisplayName("BV-10: Month 12 (December, maximum) is accepted")
    void month_12_accepted() throws IOException {
        // Boundary: maximum valid month = 12
        Report report = reportController.generateMonthlyReport(2026, 12);

        assertNotNull(report);
        assertEquals("December 2026", report.getPeriod());
    }

    @Test
    @DisplayName("BV-11: Month 0 (below minimum) is rejected")
    void month_0_rejected() {
        // Below boundary: 0 < 1
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> reportController.generateMonthlyReport(2026, 0)
        );
        assertTrue(ex.getMessage().contains("1") && ex.getMessage().contains("12"),
            "Error message should mention valid range 1-12");
    }

    @Test
    @DisplayName("BV-12: Month 13 (above maximum) is rejected")
    void month_13_rejected() {
        // Above boundary: 13 > 12
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> reportController.generateMonthlyReport(2026, 13)
        );
        assertTrue(ex.getMessage().contains("1") && ex.getMessage().contains("12"),
            "Error message should mention valid range 1-12");
    }

    // =========================================================================
    // Budget Limit Boundary Tests
    // =========================================================================

    @Test
    @DisplayName("BV-13: Budget limit just above zero (0.01) is accepted")
    void budgetLimit_positive_accepted() throws IOException {
        // Boundary: limit must be > 0
        budgetController.setBudgetLimit(Category.FOOD, 0.01);

        assertTrue(budgetController.getBudget(Category.FOOD).isPresent());
        assertEquals(0.01, budgetController.getBudget(Category.FOOD).get().getLimit(), 0.001);
    }

    @Test
    @DisplayName("BV-14: Budget limit of zero is rejected")
    void budgetLimit_zero_rejected() {
        // Boundary: 0 is not > 0
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> budgetController.setBudgetLimit(Category.FOOD, 0.00)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("limit") ||
                   ex.getMessage().contains("0"),
            "Error message should mention limit requirement");
    }

    @Test
    @DisplayName("BV-15: Negative budget limit is rejected")
    void budgetLimit_negative_rejected() {
        // Invalid: negative limit
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> budgetController.setBudgetLimit(Category.FOOD, -100.00)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("limit") ||
                   ex.getMessage().contains("0"),
            "Error message should mention limit requirement");
    }
}
