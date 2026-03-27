package com.budgetmanager.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Report model class.
 *
 * Covers:
 *   - Constructor validation (negative income/expense, null/blank period)
 *   - Period trimming
 *   - getBalance() arithmetic (positive, negative, zero)
 *   - getSavingsRate() arithmetic (normal, zero income, negative balance)
 *   - Immutability (no setters — all values confirmed via getters after construction)
 */
@DisplayName("Report Model — Unit Tests")
class ReportTest {

    // =========================================================================
    // Happy-path construction
    // =========================================================================

    @Test
    @DisplayName("Report is created with the correct field values")
    void testValidReport() {
        Report report = new Report(3000.0, 1500.0, "March 2026");
        assertEquals(3000.0,       report.getTotalIncome(),  0.001);
        assertEquals(1500.0,       report.getTotalExpense(), 0.001);
        assertEquals("March 2026", report.getPeriod());
    }

    @Test
    @DisplayName("Zero income and zero expenses are accepted (empty period)")
    void testZeroValues() {
        assertDoesNotThrow(() -> new Report(0.0, 0.0, "Empty Period"));
    }

    @Test
    @DisplayName("Period string is trimmed of surrounding whitespace")
    void testPeriodTrimmed() {
        Report report = new Report(100.0, 50.0, "  March 2026  ");
        assertEquals("March 2026", report.getPeriod());
    }

    // =========================================================================
    // Constructor validation
    // =========================================================================

    @Test
    @DisplayName("Negative income throws IllegalArgumentException")
    void testNegativeIncomeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Report(-0.01, 0.0, "March 2026")
        );
    }

    @Test
    @DisplayName("Negative expense throws IllegalArgumentException")
    void testNegativeExpenseThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Report(0.0, -0.01, "March 2026")
        );
    }

    @Test
    @DisplayName("Null period throws IllegalArgumentException")
    void testNullPeriodThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Report(100.0, 50.0, null)
        );
    }

    @Test
    @DisplayName("Empty period throws IllegalArgumentException")
    void testEmptyPeriodThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Report(100.0, 50.0, "")
        );
    }

    @Test
    @DisplayName("Whitespace-only period throws IllegalArgumentException")
    void testWhitespacePeriodThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Report(100.0, 50.0, "   ")
        );
    }

    // =========================================================================
    // getBalance
    // =========================================================================

    @Test
    @DisplayName("Balance is income minus expenses (positive surplus)")
    void testPositiveBalance() {
        Report report = new Report(3000.0, 1500.0, "March 2026");
        assertEquals(1500.0, report.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Balance is zero when income equals expenses")
    void testZeroBalance() {
        Report report = new Report(1500.0, 1500.0, "March 2026");
        assertEquals(0.0, report.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Balance is negative when expenses exceed income (deficit)")
    void testNegativeBalance() {
        Report report = new Report(1000.0, 1500.0, "March 2026");
        assertEquals(-500.0, report.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Balance is the full income amount when expenses are zero")
    void testBalanceNoExpenses() {
        Report report = new Report(2000.0, 0.0, "March 2026");
        assertEquals(2000.0, report.getBalance(), 0.001);
    }

    // =========================================================================
    // getSavingsRate
    // =========================================================================

    @Test
    @DisplayName("Savings rate is 0.5 when half of income is saved")
    void testSavingsRateHalf() {
        Report report = new Report(2000.0, 1000.0, "March 2026");
        assertEquals(0.5, report.getSavingsRate(), 0.001);
    }

    @Test
    @DisplayName("Savings rate is 1.0 when all income is saved (no expenses)")
    void testSavingsRateFull() {
        Report report = new Report(2000.0, 0.0, "March 2026");
        assertEquals(1.0, report.getSavingsRate(), 0.001);
    }

    @Test
    @DisplayName("Savings rate is 0.0 when income equals expenses (break even)")
    void testSavingsRateBreakEven() {
        Report report = new Report(1500.0, 1500.0, "March 2026");
        assertEquals(0.0, report.getSavingsRate(), 0.001);
    }

    @Test
    @DisplayName("Savings rate is negative when expenses exceed income")
    void testNegativeSavingsRate() {
        Report report = new Report(1000.0, 2000.0, "March 2026");
        assertEquals(-1.0, report.getSavingsRate(), 0.001);
    }

    @Test
    @DisplayName("Savings rate is 0.0 when total income is zero (no division by zero)")
    void testSavingsRateZeroIncome() {
        Report report = new Report(0.0, 0.0, "March 2026");
        assertEquals(0.0, report.getSavingsRate(), 0.001);
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString contains the period and income value")
    void testToStringContainsKeyFields() {
        Report report = new Report(3000.0, 1500.0, "March 2026");
        String str = report.toString();
        assertTrue(str.contains("March 2026"));
        assertTrue(str.contains("3000"));
    }
}
