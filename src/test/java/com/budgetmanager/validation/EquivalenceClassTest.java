package com.budgetmanager.validation;

import com.budgetmanager.controller.TransactionController;
import com.budgetmanager.dao.FileManager;
import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Category;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Equivalence Class Testing for the Budget Management System.
 *
 * Partitions inputs into valid and invalid equivalence classes,
 * then tests one representative value from each class.
 *
 * Stage 7 of IMPLEMENTATION_PLAN.md — Validation Testing (Technique 2 of 5)
 *
 * Equivalence Classes:
 *   Amount:      EC1 (valid: >=0.01), EC2 (zero), EC3 (negative)
 *   Description: EC5 (valid: 1-100 chars), EC6 (null), EC7 (whitespace), EC8 (>100)
 *   Category:    EC9 (valid enum), EC10 (null)
 *   Type:        EC11 (valid enum), EC12 (null)
 *   Date:        EC13 (valid LocalDate), EC14 (null)
 *   Keyword:     EC15 (valid string), EC16 (null), EC17 (empty/whitespace)
 *   ID:          EC18 (valid string), EC19 (null), EC20 (empty/whitespace)
 *
 * Rubric relevance: Required by project_description.md —
 * "Validation Testing: Equivalence class testing"
 */
@DisplayName("Stage 7 — Equivalence Class Tests")
class EquivalenceClassTest {

    @TempDir
    Path tempDir;

    private TransactionController transactionController;
    private TransactionDAO transactionDAO;

    @BeforeEach
    void setUp() {
        String transactionsPath = tempDir.resolve("transactions.csv").toString();
        FileManager fileManager = new FileManager();
        transactionDAO = new TransactionDAO(transactionsPath, fileManager);
        transactionController = new TransactionController(transactionDAO, fileManager);
    }

    // =========================================================================
    // Amount Equivalence Classes
    // =========================================================================

    @Test
    @DisplayName("EC-01: Valid positive amount (EC1) is accepted")
    void amount_validPositive_EC1_accepted() throws IOException {
        // EC1: Valid positive decimal >= 0.01
        Transaction t = transactionController.addTransaction(
            50.00,  // Representative of valid class
            LocalDate.of(2026, 3, 15),
            "Valid amount test",
            Category.FOOD,
            TransactionType.EXPENSE
        );

        assertNotNull(t);
        assertEquals(50.00, t.getAmount(), 0.001);
    }

    @Test
    @DisplayName("EC-02: Zero amount (EC2) is rejected")
    void amount_zero_EC2_rejected() {
        // EC2: Zero
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                0.00,
                LocalDate.of(2026, 3, 15),
                "Zero test",
                Category.FOOD,
                TransactionType.EXPENSE
            )
        );
    }

    @Test
    @DisplayName("EC-03: Negative amount (EC3) is rejected")
    void amount_negative_EC3_rejected() {
        // EC3: Negative
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                -10.00,
                LocalDate.of(2026, 3, 15),
                "Negative test",
                Category.FOOD,
                TransactionType.EXPENSE
            )
        );
    }

    // =========================================================================
    // Description Equivalence Classes
    // =========================================================================

    @Test
    @DisplayName("EC-05: Valid description (EC5) is accepted")
    void description_valid_EC5_accepted() throws IOException {
        // EC5: 1-100 non-blank characters
        Transaction t = transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),
            "Lunch",  // Representative valid description
            Category.FOOD,
            TransactionType.EXPENSE
        );

        assertNotNull(t);
        assertEquals("Lunch", t.getDescription());
    }

    @Test
    @DisplayName("EC-06: Null description (EC6) is rejected")
    void description_null_EC6_rejected() {
        // EC6: null
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                100.00,
                LocalDate.of(2026, 3, 15),
                null,
                Category.FOOD,
                TransactionType.EXPENSE
            )
        );
    }

    @Test
    @DisplayName("EC-07: Whitespace-only description (EC7) is rejected")
    void description_whitespace_EC7_rejected() {
        // EC7: Empty/whitespace only
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                100.00,
                LocalDate.of(2026, 3, 15),
                "   ",  // Whitespace only
                Category.FOOD,
                TransactionType.EXPENSE
            )
        );
    }

    @Test
    @DisplayName("EC-08: Description over 100 chars (EC8) is rejected")
    void description_tooLong_EC8_rejected() {
        // EC8: >100 characters
        String longDesc = "A".repeat(101);
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                100.00,
                LocalDate.of(2026, 3, 15),
                longDesc,
                Category.FOOD,
                TransactionType.EXPENSE
            )
        );
    }

    // =========================================================================
    // Category Equivalence Classes
    // =========================================================================

    @Test
    @DisplayName("EC-09: Valid category (EC9) is accepted")
    void category_valid_EC9_accepted() throws IOException {
        // EC9: Any valid Category enum value
        Transaction t = transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),
            "Food expense",
            Category.FOOD,  // Valid enum
            TransactionType.EXPENSE
        );

        assertNotNull(t);
        assertEquals(Category.FOOD, t.getCategory());
    }

    @Test
    @DisplayName("EC-10: Null category (EC10) is rejected")
    void category_null_EC10_rejected() {
        // EC10: null category
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                100.00,
                LocalDate.of(2026, 3, 15),
                "Test",
                null,  // Null category
                TransactionType.EXPENSE
            )
        );
    }

    // =========================================================================
    // Type Equivalence Classes
    // =========================================================================

    @Test
    @DisplayName("EC-11: Valid type EXPENSE (EC11) is accepted")
    void type_validExpense_EC11_accepted() throws IOException {
        // EC11: Valid TransactionType (EXPENSE)
        Transaction t = transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),
            "Test expense",
            Category.FOOD,
            TransactionType.EXPENSE
        );

        assertNotNull(t);
        assertEquals(TransactionType.EXPENSE, t.getType());
    }

    @Test
    @DisplayName("EC-11b: Valid type INCOME (EC11) is accepted")
    void type_validIncome_EC11_accepted() throws IOException {
        // EC11: Valid TransactionType (INCOME)
        Transaction t = transactionController.addTransaction(
            2000.00,
            LocalDate.of(2026, 3, 15),
            "Salary",
            Category.SALARY,
            TransactionType.INCOME
        );

        assertNotNull(t);
        assertEquals(TransactionType.INCOME, t.getType());
    }

    @Test
    @DisplayName("EC-12: Null type (EC12) is rejected")
    void type_null_EC12_rejected() {
        // EC12: null type
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                100.00,
                LocalDate.of(2026, 3, 15),
                "Test",
                Category.FOOD,
                null  // Null type
            )
        );
    }

    // =========================================================================
    // Date Equivalence Classes
    // =========================================================================

    @Test
    @DisplayName("EC-13: Valid date (EC13) is accepted")
    void date_valid_EC13_accepted() throws IOException {
        // EC13: Valid LocalDate
        Transaction t = transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),  // Valid date
            "Test",
            Category.FOOD,
            TransactionType.EXPENSE
        );

        assertNotNull(t);
        assertEquals(LocalDate.of(2026, 3, 15), t.getDate());
    }

    @Test
    @DisplayName("EC-14: Null date (EC14) is rejected")
    void date_null_EC14_rejected() {
        // EC14: null date
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.addTransaction(
                100.00,
                null,  // Null date
                "Test",
                Category.FOOD,
                TransactionType.EXPENSE
            )
        );
    }

    // =========================================================================
    // Search Keyword Equivalence Classes
    // =========================================================================

    @Test
    @DisplayName("EC-15: Valid search keyword (EC15) returns results")
    void keyword_valid_EC15_returnsResults() throws IOException {
        // Setup: Add a transaction to search for
        transactionController.addTransaction(
            50.00,
            LocalDate.of(2026, 3, 15),
            "Lunch at restaurant",
            Category.FOOD,
            TransactionType.EXPENSE
        );

        // EC15: Non-empty string keyword
        List<Transaction> results = transactionController.searchByDescription("lunch");

        assertEquals(1, results.size());
        assertTrue(results.get(0).getDescription().toLowerCase().contains("lunch"));
    }

    @Test
    @DisplayName("EC-16: Null search keyword (EC16) is rejected")
    void keyword_null_EC16_rejected() {
        // EC16: null keyword
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.searchByDescription(null)
        );
    }

    @Test
    @DisplayName("EC-17: Empty search keyword (EC17) is rejected")
    void keyword_empty_EC17_rejected() {
        // EC17: Empty/whitespace keyword
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.searchByDescription("")
        );
    }

    @Test
    @DisplayName("EC-17b: Whitespace-only search keyword (EC17) is rejected")
    void keyword_whitespace_EC17_rejected() {
        // EC17: Whitespace-only keyword
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.searchByDescription("   ")
        );
    }

    // =========================================================================
    // Remove ID Equivalence Classes
    // =========================================================================

    @Test
    @DisplayName("EC-19: Null ID for remove (EC19) is rejected")
    void removeId_null_EC19_rejected() {
        // EC19: null ID
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.removeTransaction(null)
        );
    }

    @Test
    @DisplayName("EC-20: Empty ID for remove (EC20) is rejected")
    void removeId_empty_EC20_rejected() {
        // EC20: Empty/whitespace ID
        assertThrows(
            IllegalArgumentException.class,
            () -> transactionController.removeTransaction(" ")
        );
    }
}
