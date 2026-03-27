package com.budgetmanager.model;

/**
 * Enum representing the type of a financial transaction.
 * A transaction is either income (money coming in) or an expense (money going out).
 *
 * Used by: Transaction, TransactionController, ReportController
 * Tested by: TransactionTest (via Transaction constructor)
 */
public enum TransactionType {
    /** Money received — e.g., salary, freelance payment, investment return */
    INCOME,

    /** Money spent — e.g., groceries, rent, entertainment */
    EXPENSE
}
