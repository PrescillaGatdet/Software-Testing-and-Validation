package com.budgetmanager.model;

/**
 * Represents a monthly spending budget limit for a specific category.
 * Tracks how much has been spent and whether the limit has been exceeded.
 */
public class Budget {

    /** Fraction of the limit at which a WARNING alert is triggered (80%). */
    public static final double WARNING_THRESHOLD = 0.80;

    private Category category;
    private double limit;
    private double currentSpending;

    /**
     * Creates a Budget for a given category with the specified spending limit.
     * Current spending is initialised to zero.
     *
     * @param category the category this budget applies to (must not be null)
     * @param limit    the maximum allowed spending in this category (must be > 0)
     * @throws IllegalArgumentException if category is null or limit is not positive
     */
    public Budget(Category category, double limit) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0, got: " + limit);
        }
        this.category       = category;
        this.limit          = limit;
        this.currentSpending = 0.0;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Returns the category this budget covers. */
    public Category getCategory() { return category; }

    /** Returns the spending limit for this budget. */
    public double getLimit() { return limit; }

    /** Returns how much has been spent in this category so far. */
    public double getCurrentSpending() { return currentSpending; }

    // -------------------------------------------------------------------------
    // Setters (with validation)
    // -------------------------------------------------------------------------

    /**
     * Updates the spending limit.
     *
     * @param limit new limit (must be > 0)
     */
    public void setLimit(double limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0, got: " + limit);
        }
        this.limit = limit;
    }

    /**
     * Updates the current spending total.
     * Called by BudgetController when expense transactions are recorded.
     *
     * @param currentSpending new spending total (must be >= 0)
     */
    public void setCurrentSpending(double currentSpending) {
        if (currentSpending < 0) {
            throw new IllegalArgumentException(
                "Current spending cannot be negative, got: " + currentSpending);
        }
        this.currentSpending = currentSpending;
    }

    // -------------------------------------------------------------------------
    // Computed properties
    // -------------------------------------------------------------------------

    /**
     * Returns the remaining budget (limit minus current spending).
     * The result can be negative if the budget has been exceeded.
     */
    public double getRemainingBudget() {
        return limit - currentSpending;
    }

    /**
     * Returns {@code true} if current spending has strictly exceeded the limit.
     * Spending exactly equal to the limit is NOT considered exceeded.
     */
    public boolean isExceeded() {
        return currentSpending > limit;
    }

    /**
     * Returns {@code true} if current spending is at or above the WARNING_THRESHOLD
     * fraction of the limit (default 80%).
     */
    public boolean isNearLimit() {
        return getUsagePercentage() >= WARNING_THRESHOLD;
    }

    /**
     * Returns the fraction of the budget that has been used (0.0 to 1.0+).
     * Returns 0.0 when the limit is zero to prevent division-by-zero.
     */
    public double getUsagePercentage() {
        if (limit == 0) return 0.0;
        return currentSpending / limit;
    }

    @Override
    public String toString() {
        return String.format(
            "Budget{category=%s, limit=%.2f, spent=%.2f, remaining=%.2f}",
            category, limit, currentSpending, getRemainingBudget()
        );
    }
}
