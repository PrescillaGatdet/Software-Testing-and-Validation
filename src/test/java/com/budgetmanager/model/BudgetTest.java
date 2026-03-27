package com.budgetmanager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Budget model class.
 *
 * Covers:
 *   - Constructor validation (null category, zero/negative limit)
 *   - Correct initial state after construction
 *   - getRemainingBudget() at various spending levels
 *   - isExceeded() boundary behaviour (exactly at limit vs. over limit)
 *   - isNearLimit() at the 80% threshold
 *   - getUsagePercentage() arithmetic
 *   - Setter validation
 */
@DisplayName("Budget Model — Unit Tests")
class BudgetTest {

    private Budget budget;

    @BeforeEach
    void setUp() {
        // Standard budget: FOOD category with a $500 limit
        budget = new Budget(Category.FOOD, 500.0);
    }

    // =========================================================================
    // Construction
    // =========================================================================

    @Test
    @DisplayName("Budget initialises with correct category, limit, and zero spending")
    void testInitialState() {
        assertEquals(Category.FOOD, budget.getCategory());
        assertEquals(500.0, budget.getLimit(), 0.001);
        assertEquals(0.0,   budget.getCurrentSpending(), 0.001);
    }

    @Test
    @DisplayName("Null category throws IllegalArgumentException")
    void testNullCategoryThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Budget(null, 100.0));
    }

    @Test
    @DisplayName("Zero limit throws IllegalArgumentException")
    void testZeroLimitThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Budget(Category.FOOD, 0.0));
    }

    @Test
    @DisplayName("Negative limit throws IllegalArgumentException")
    void testNegativeLimitThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Budget(Category.FOOD, -1.0));
    }

    // =========================================================================
    // getRemainingBudget
    // =========================================================================

    @Test
    @DisplayName("Remaining budget equals limit when spending is zero")
    void testRemainingBudgetNoSpending() {
        assertEquals(500.0, budget.getRemainingBudget(), 0.001);
    }

    @Test
    @DisplayName("Remaining budget decreases proportionally with spending")
    void testRemainingBudgetAfterPartialSpending() {
        budget.setCurrentSpending(200.0);
        assertEquals(300.0, budget.getRemainingBudget(), 0.001);
    }

    @Test
    @DisplayName("Remaining budget is zero when spending equals limit exactly")
    void testRemainingBudgetAtLimit() {
        budget.setCurrentSpending(500.0);
        assertEquals(0.0, budget.getRemainingBudget(), 0.001);
    }

    @Test
    @DisplayName("Remaining budget is negative when spending exceeds limit")
    void testRemainingBudgetNegativeWhenExceeded() {
        budget.setCurrentSpending(600.0);
        assertEquals(-100.0, budget.getRemainingBudget(), 0.001);
    }

    // =========================================================================
    // isExceeded
    // =========================================================================

    @Test
    @DisplayName("isExceeded is false when spending is well below limit")
    void testNotExceededBelowLimit() {
        budget.setCurrentSpending(300.0);
        assertFalse(budget.isExceeded());
    }

    @Test
    @DisplayName("isExceeded is false when spending is one cent below the limit")
    void testNotExceededOneCentBelow() {
        budget.setCurrentSpending(499.99);
        assertFalse(budget.isExceeded());
    }

    @Test
    @DisplayName("isExceeded is false when spending equals the limit exactly (boundary)")
    void testNotExceededAtExactLimit() {
        budget.setCurrentSpending(500.0);
        assertFalse(budget.isExceeded(), "Spending exactly at limit should NOT be considered exceeded");
    }

    @Test
    @DisplayName("isExceeded is true when spending is one cent over the limit")
    void testExceededOneCentOver() {
        budget.setCurrentSpending(500.01);
        assertTrue(budget.isExceeded());
    }

    @Test
    @DisplayName("isExceeded is true when spending greatly exceeds the limit")
    void testExceededLargeOverrun() {
        budget.setCurrentSpending(1000.0);
        assertTrue(budget.isExceeded());
    }

    // =========================================================================
    // isNearLimit (80% warning threshold)
    // =========================================================================

    @Test
    @DisplayName("isNearLimit is false when spending is below 80%")
    void testNoWarningBelowThreshold() {
        budget.setCurrentSpending(399.99); // 79.998%
        assertFalse(budget.isNearLimit());
    }

    @Test
    @DisplayName("isNearLimit is true when spending is exactly 80% (boundary)")
    void testWarningAtExactThreshold() {
        budget.setCurrentSpending(400.0); // 80% of 500
        assertTrue(budget.isNearLimit());
    }

    @Test
    @DisplayName("isNearLimit is true when spending is above 80%")
    void testWarningAboveThreshold() {
        budget.setCurrentSpending(450.0); // 90%
        assertTrue(budget.isNearLimit());
    }

    @Test
    @DisplayName("isNearLimit is true when budget is exceeded (over 100%)")
    void testWarningWhenExceeded() {
        budget.setCurrentSpending(600.0); // 120%
        assertTrue(budget.isNearLimit());
    }

    // =========================================================================
    // getUsagePercentage
    // =========================================================================

    @Test
    @DisplayName("Usage percentage is 0 when no spending has occurred")
    void testUsagePercentageZero() {
        assertEquals(0.0, budget.getUsagePercentage(), 0.001);
    }

    @Test
    @DisplayName("Usage percentage is 0.5 at half the limit")
    void testUsagePercentageHalf() {
        budget.setCurrentSpending(250.0);
        assertEquals(0.5, budget.getUsagePercentage(), 0.001);
    }

    @Test
    @DisplayName("Usage percentage is 1.0 at exactly the limit")
    void testUsagePercentageFull() {
        budget.setCurrentSpending(500.0);
        assertEquals(1.0, budget.getUsagePercentage(), 0.001);
    }

    @Test
    @DisplayName("Usage percentage exceeds 1.0 when over the limit")
    void testUsagePercentageOverLimit() {
        budget.setCurrentSpending(600.0);
        assertEquals(1.2, budget.getUsagePercentage(), 0.001);
    }

    // =========================================================================
    // setters
    // =========================================================================

    @Test
    @DisplayName("setLimit updates the limit to a valid positive value")
    void testSetLimitValid() {
        budget.setLimit(1000.0);
        assertEquals(1000.0, budget.getLimit(), 0.001);
    }

    @Test
    @DisplayName("setLimit with zero throws IllegalArgumentException")
    void testSetLimitZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> budget.setLimit(0.0));
    }

    @Test
    @DisplayName("setCurrentSpending with negative value throws IllegalArgumentException")
    void testSetNegativeSpendingThrows() {
        assertThrows(IllegalArgumentException.class, () -> budget.setCurrentSpending(-0.01));
    }

    @Test
    @DisplayName("setCurrentSpending with zero is accepted")
    void testSetSpendingZeroAccepted() {
        assertDoesNotThrow(() -> budget.setCurrentSpending(0.0));
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString contains category and limit values")
    void testToStringContainsKeyFields() {
        String str = budget.toString();
        assertTrue(str.contains("FOOD"));
        assertTrue(str.contains("500"));
    }
}
