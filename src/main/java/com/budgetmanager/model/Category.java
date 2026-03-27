package com.budgetmanager.model;

/**
 * Enum representing the categories available for transactions and budgets.
 * Each category has a human-readable display name used in console output.
 *
 * Satisfies: C6 (clear text-based feedback) by using readable display names.
 * Used by: Transaction, Budget, TransactionController, BudgetController
 * Tested by: TransactionTest, BudgetTest (via constructors)
 */
public enum Category {
    FOOD("Food"),
    TRANSPORT("Transport"),
    ENTERTAINMENT("Entertainment"),
    UTILITIES("Utilities"),
    SALARY("Salary"),
    INVESTMENT("Investment"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable name for display in reports and menus.
     *
     * @return the display name of this category
     */
    public String getDisplayName() {
        return displayName;
    }
}
