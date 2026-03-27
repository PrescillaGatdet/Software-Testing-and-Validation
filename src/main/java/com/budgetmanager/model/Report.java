package com.budgetmanager.model;

/**
 * Represents a computed financial summary report for a given time period.
 * Holds aggregate totals for income and expenses produced by ReportController.
 *
 * Design notes (MVC — MODEL layer):
 *   - Immutable: all fields are set at construction time and have no setters.
 *   - Computed properties (balance, savings rate) are derived at call time,
 *     not stored, to avoid stale data.
 *   - ReportController creates Report instances; ReportView displays them.
 *
 * Constraints addressed:
 *   C3 (Data Integrity): constructor rejects negative totals.
 *   C8 (MVC): no dependency on DAO or View layers.
 *
 * Tested by: ReportTest
 */
public class Report {

    private final double totalIncome;
    private final double totalExpense;
    private final String period;

    /**
     * Creates a Report with the given financial totals and period label.
     *
     * @param totalIncome   total income for the period (must be >= 0)
     * @param totalExpense  total expenses for the period (must be >= 0)
     * @param period        human-readable label, e.g. "March 2026" (must not be blank)
     * @throws IllegalArgumentException if any value is invalid
     */
    public Report(double totalIncome, double totalExpense, String period) {
        if (totalIncome < 0) {
            throw new IllegalArgumentException(
                "Total income cannot be negative, got: " + totalIncome);
        }
        if (totalExpense < 0) {
            throw new IllegalArgumentException(
                "Total expense cannot be negative, got: " + totalExpense);
        }
        if (period == null || period.trim().isEmpty()) {
            throw new IllegalArgumentException("Period cannot be null or empty");
        }
        this.totalIncome  = totalIncome;
        this.totalExpense = totalExpense;
        this.period       = period.trim();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Returns the total income for this report's period. */
    public double getTotalIncome() { return totalIncome; }

    /** Returns the total expenses for this report's period. */
    public double getTotalExpense() { return totalExpense; }

    /** Returns the human-readable period label (e.g., "March 2026"). */
    public String getPeriod() { return period; }

    // -------------------------------------------------------------------------
    // Computed properties
    // -------------------------------------------------------------------------

    /**
     * Returns the net balance: income minus expenses.
     * A positive value means a surplus; a negative value means a deficit.
     *
     * @return net balance
     */
    public double getBalance() {
        return totalIncome - totalExpense;
    }

    /**
     * Returns the savings rate as a ratio of balance to income (0.0–1.0+).
     * Returns 0.0 when total income is zero to prevent division-by-zero.
     * A negative value means expenses exceeded income.
     *
     * @return savings rate ratio
     */
    public double getSavingsRate() {
        if (totalIncome == 0) return 0.0;
        return getBalance() / totalIncome;
    }

    @Override
    public String toString() {
        return String.format(
            "Report{period='%s', income=%.2f, expense=%.2f, balance=%.2f}",
            period, totalIncome, totalExpense, getBalance()
        );
    }
}
