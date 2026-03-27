package com.budgetmanager.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Transaction model class.
 *
 * Covers:
 *   - Constructor validation (all invalid inputs)
 *   - Successful construction with valid data
 *   - Getter correctness
 *   - Description trimming
 *   - ID-based equality
 *   - Boundary values for amount and description length
 */
@DisplayName("Transaction Model — Unit Tests")
class TransactionTest {

    /** A fixed date used across tests to keep them deterministic. */
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 3, 1);

    // =========================================================================
    // Happy-path construction
    // =========================================================================

    @Test
    @DisplayName("Valid EXPENSE transaction is created with correct field values")
    void testValidExpenseTransaction() {
        Transaction t = new Transaction(
            150.00, TEST_DATE, "Grocery shopping", Category.FOOD, TransactionType.EXPENSE
        );
        assertEquals(150.00, t.getAmount(), 0.001);
        assertEquals(TEST_DATE, t.getDate());
        assertEquals("Grocery shopping", t.getDescription());
        assertEquals(Category.FOOD, t.getCategory());
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertNotNull(t.getId(), "Auto-generated ID must not be null");
        assertFalse(t.getId().isEmpty(), "Auto-generated ID must not be empty");
    }

    @Test
    @DisplayName("Valid INCOME transaction is created with correct field values")
    void testValidIncomeTransaction() {
        Transaction t = new Transaction(
            2500.00, TEST_DATE, "Monthly salary", Category.SALARY, TransactionType.INCOME
        );
        assertEquals(TransactionType.INCOME, t.getType());
        assertEquals(Category.SALARY, t.getCategory());
        assertEquals(2500.00, t.getAmount(), 0.001);
    }

    @Test
    @DisplayName("Explicit-ID constructor stores the given ID")
    void testExplicitIdConstructor() {
        Transaction t = new Transaction(
            "my-id-123", 50.0, TEST_DATE, "Test", Category.OTHER, TransactionType.EXPENSE
        );
        assertEquals("my-id-123", t.getId());
    }

    // =========================================================================
    // Amount boundary values
    // =========================================================================

    @Test
    @DisplayName("Minimum valid amount (0.01) is accepted")
    void testMinimumAmount() {
        assertDoesNotThrow(() ->
            new Transaction(0.01, TEST_DATE, "Small purchase", Category.OTHER, TransactionType.EXPENSE)
        );
    }

    @Test
    @DisplayName("Amount exactly zero throws IllegalArgumentException")
    void testAmountZeroThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(0.0, TEST_DATE, "Test", Category.FOOD, TransactionType.EXPENSE)
        );
    }

    @Test
    @DisplayName("Negative amount throws IllegalArgumentException")
    void testNegativeAmountThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(-10.0, TEST_DATE, "Test", Category.FOOD, TransactionType.EXPENSE)
        );
    }

    @Test
    @DisplayName("Very small negative amount (−0.001) throws IllegalArgumentException")
    void testTinyNegativeAmountThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(-0.001, TEST_DATE, "Test", Category.FOOD, TransactionType.EXPENSE)
        );
    }

    // =========================================================================
    // Date validation
    // =========================================================================

    @Test
    @DisplayName("Null date throws IllegalArgumentException")
    void testNullDateThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(100.0, null, "Test", Category.FOOD, TransactionType.EXPENSE)
        );
    }

    // =========================================================================
    // Description validation
    // =========================================================================

    @Test
    @DisplayName("Null description throws IllegalArgumentException")
    void testNullDescriptionThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(100.0, TEST_DATE, null, Category.FOOD, TransactionType.EXPENSE)
        );
    }

    @Test
    @DisplayName("Empty description throws IllegalArgumentException")
    void testEmptyDescriptionThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(100.0, TEST_DATE, "", Category.FOOD, TransactionType.EXPENSE)
        );
    }

    @Test
    @DisplayName("Whitespace-only description throws IllegalArgumentException")
    void testWhitespaceDescriptionThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(100.0, TEST_DATE, "   ", Category.FOOD, TransactionType.EXPENSE)
        );
    }

    @Test
    @DisplayName("Description exactly 100 characters is accepted")
    void testDescriptionAt100CharsAccepted() {
        String desc100 = "A".repeat(100);
        assertDoesNotThrow(() ->
            new Transaction(100.0, TEST_DATE, desc100, Category.FOOD, TransactionType.EXPENSE)
        );
    }

    @Test
    @DisplayName("Description of 101 characters throws IllegalArgumentException")
    void testDescriptionOver100CharsThrows() {
        String desc101 = "A".repeat(101);
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(100.0, TEST_DATE, desc101, Category.FOOD, TransactionType.EXPENSE)
        );
    }

    @Test
    @DisplayName("Description is trimmed of surrounding whitespace")
    void testDescriptionTrimmed() {
        Transaction t = new Transaction(
            50.0, TEST_DATE, "  Lunch  ", Category.FOOD, TransactionType.EXPENSE
        );
        assertEquals("Lunch", t.getDescription());
    }

    // =========================================================================
    // Category and type validation
    // =========================================================================

    @Test
    @DisplayName("Null category throws IllegalArgumentException")
    void testNullCategoryThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(100.0, TEST_DATE, "Test", null, TransactionType.EXPENSE)
        );
    }

    @Test
    @DisplayName("Null transaction type throws IllegalArgumentException")
    void testNullTypeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Transaction(100.0, TEST_DATE, "Test", Category.FOOD, null)
        );
    }

    // =========================================================================
    // Setters
    // =========================================================================

    @Test
    @DisplayName("setAmount updates the amount to a valid value")
    void testSetAmountValid() {
        Transaction t = new Transaction(100.0, TEST_DATE, "Test", Category.FOOD, TransactionType.EXPENSE);
        t.setAmount(200.0);
        assertEquals(200.0, t.getAmount(), 0.001);
    }

    @Test
    @DisplayName("setAmount with zero throws IllegalArgumentException")
    void testSetAmountZeroThrows() {
        Transaction t = new Transaction(100.0, TEST_DATE, "Test", Category.FOOD, TransactionType.EXPENSE);
        assertThrows(IllegalArgumentException.class, () -> t.setAmount(0.0));
    }

    @Test
    @DisplayName("setCategory updates the category")
    void testSetCategory() {
        Transaction t = new Transaction(100.0, TEST_DATE, "Test", Category.FOOD, TransactionType.EXPENSE);
        t.setCategory(Category.TRANSPORT);
        assertEquals(Category.TRANSPORT, t.getCategory());
    }

    @Test
    @DisplayName("setCategory with null throws IllegalArgumentException")
    void testSetCategoryNullThrows() {
        Transaction t = new Transaction(100.0, TEST_DATE, "Test", Category.FOOD, TransactionType.EXPENSE);
        assertThrows(IllegalArgumentException.class, () -> t.setCategory(null));
    }

    // =========================================================================
    // Equality
    // =========================================================================

    @Test
    @DisplayName("Two transactions with the same explicit ID are equal")
    void testEqualityById() {
        Transaction t1 = new Transaction("same-id", 100.0, TEST_DATE, "Desc1", Category.FOOD, TransactionType.EXPENSE);
        Transaction t2 = new Transaction("same-id", 999.0, TEST_DATE, "Desc2", Category.SALARY, TransactionType.INCOME);
        assertEquals(t1, t2, "Equality is ID-based; other fields should not matter");
    }

    @Test
    @DisplayName("Two transactions with different IDs are not equal")
    void testInequalityDifferentId() {
        Transaction t1 = new Transaction("id-1", 100.0, TEST_DATE, "Test", Category.FOOD, TransactionType.EXPENSE);
        Transaction t2 = new Transaction("id-2", 100.0, TEST_DATE, "Test", Category.FOOD, TransactionType.EXPENSE);
        assertNotEquals(t1, t2);
    }

    @Test
    @DisplayName("Transaction is equal to itself (reflexivity)")
    void testReflexiveEquality() {
        Transaction t = new Transaction(100.0, TEST_DATE, "Test", Category.FOOD, TransactionType.EXPENSE);
        assertEquals(t, t);
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString contains the ID and amount")
    void testToStringContainsKeyFields() {
        Transaction t = new Transaction("abc-123", 75.50, TEST_DATE, "Lunch", Category.FOOD, TransactionType.EXPENSE);
        String str = t.toString();
        assertTrue(str.contains("abc-123"));
        assertTrue(str.contains("75.50"));
    }
}
