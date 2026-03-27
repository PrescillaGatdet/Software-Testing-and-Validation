package com.budgetmanager.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single financial transaction (income or expense).
 * This is the core data model for the Budget Management System.
 *
 * Design notes (MVC — MODEL layer):
 *   - Pure data class with no file I/O, no console output, no business logic.
 *   - All validation is in the constructor so objects are always in a valid state.
 *   - Equality is based on ID only, allowing two references to the same logical
 *     transaction to be considered equal regardless of field updates.
 *
 * Constraints addressed:
 *   C3 (Data Integrity): constructor rejects invalid input, preventing corrupt records.
 *   C8 (MVC): this class has zero dependency on DAO or View layers.
 *
 * Tested by: TransactionTest
 */
public class Transaction {

    private final String id;
    private double amount;
    private LocalDate date;
    private String description;
    private Category category;
    private TransactionType type;

    // Validation constants
    /** Minimum allowed transaction amount (inclusive). */
    public static final double MIN_AMOUNT = 0.01;

    /** Maximum allowed description length in characters. */
    public static final int MAX_DESCRIPTION_LENGTH = 100;

    /**
     * Creates a new Transaction with an auto-generated UUID.
     *
     * @param amount      the monetary amount (must be >= 0.01)
     * @param date        the date of the transaction (must not be null)
     * @param description a short description, 1–100 characters
     * @param category    the transaction category (must not be null)
     * @param type        INCOME or EXPENSE (must not be null)
     * @throws IllegalArgumentException if any argument fails validation
     */
    public Transaction(double amount, LocalDate date, String description,
                       Category category, TransactionType type) {
        this(UUID.randomUUID().toString(), amount, date, description, category, type);
    }

    /**
     * Creates a Transaction with an explicit ID.
     * Used when reloading a transaction from CSV storage.
     *
     * @param id          the unique identifier for this transaction
     * @param amount      the monetary amount (must be >= 0.01)
     * @param date        the date of the transaction (must not be null)
     * @param description a short description, 1–100 characters
     * @param category    the transaction category (must not be null)
     * @param type        INCOME or EXPENSE (must not be null)
     * @throws IllegalArgumentException if any argument fails validation
     */
    public Transaction(String id, double amount, LocalDate date, String description,
                       Category category, TransactionType type) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        validateAmount(amount);
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        validateDescription(description);
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        this.id          = id.trim();
        this.amount      = amount;
        this.date        = date;
        this.description = description.trim();
        this.category    = category;
        this.type        = type;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Returns the unique identifier of this transaction. */
    public String getId() { return id; }

    /** Returns the monetary amount of this transaction. */
    public double getAmount() { return amount; }

    /** Returns the date of this transaction. */
    public LocalDate getDate() { return date; }

    /** Returns the description of this transaction. */
    public String getDescription() { return description; }

    /** Returns the category of this transaction. */
    public Category getCategory() { return category; }

    /** Returns the type (INCOME or EXPENSE) of this transaction. */
    public TransactionType getType() { return type; }

    // -------------------------------------------------------------------------
    // Setters (with validation)
    // -------------------------------------------------------------------------

    /**
     * Updates the amount of this transaction.
     *
     * @param amount the new amount (must be >= 0.01)
     */
    public void setAmount(double amount) {
        validateAmount(amount);
        this.amount = amount;
    }

    /**
     * Updates the date of this transaction.
     *
     * @param date the new date (must not be null)
     */
    public void setDate(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date cannot be null");
        this.date = date;
    }

    /**
     * Updates the description of this transaction.
     *
     * @param description the new description (1–100 non-whitespace characters)
     */
    public void setDescription(String description) {
        validateDescription(description);
        this.description = description.trim();
    }

    /**
     * Updates the category of this transaction.
     *
     * @param category the new category (must not be null)
     */
    public void setCategory(Category category) {
        if (category == null) throw new IllegalArgumentException("Category cannot be null");
        this.category = category;
    }

    // -------------------------------------------------------------------------
    // Equality and hashing (ID-based)
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction other = (Transaction) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
            "Transaction{id='%s', amount=%.2f, date=%s, description='%s', category=%s, type=%s}",
            id, amount, date, description, category, type
        );
    }

    // -------------------------------------------------------------------------
    // Private validation helpers
    // -------------------------------------------------------------------------

    private static void validateAmount(double amount) {
        if (amount < MIN_AMOUNT) {
            throw new IllegalArgumentException(
                "Amount must be >= " + MIN_AMOUNT + ", got: " + amount);
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                "Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
    }
}
