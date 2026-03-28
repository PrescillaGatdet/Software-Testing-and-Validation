package com.budgetmanager.model;

/**
 * Categories available for transactions and budgets.
 * Each value has a human-readable display name for console output.
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

    /** Returns the human-readable name for display in reports and menus. */
    public String getDisplayName() {
        return displayName;
    }
}
