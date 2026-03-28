package com.budgetmanager.controller;

import com.budgetmanager.dao.FileManager;
import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Category;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for transaction management operations.
 * Implements all business logic related to creating, retrieving,
 * filtering, searching, and exporting transactions.
 */
public class TransactionController {

    private final TransactionDAO dao;
    private final FileManager fileManager;

    /**
     * Full constructor — used in production and for unit tests that
     * also exercise the export functionality.
     *
     * @param dao         the TransactionDAO to delegate persistence to
     * @param fileManager the FileManager used when exporting to CSV
     */
    public TransactionController(TransactionDAO dao, FileManager fileManager) {
        this.dao         = dao;
        this.fileManager = fileManager;
    }

    /**
     * Convenience constructor — uses a default FileManager.
     * Suitable for unit tests that do not test exportToCSV.
     *
     * @param dao the TransactionDAO to delegate persistence to
     */
    public TransactionController(TransactionDAO dao) {
        this(dao, new FileManager());
    }

    // -------------------------------------------------------------------------
    // Transaction CRUD
    // -------------------------------------------------------------------------

    /**
     * Creates a new transaction with an auto-generated UUID and persists it.
     * Input validation is delegated to the Transaction constructor.
     *
     * @param amount      amount (>= 0.01)
     * @param date        transaction date (not null)
     * @param description 1–100 character description
     * @param category    transaction category (not null)
     * @param type        INCOME or EXPENSE (not null)
     * @return the newly created and saved Transaction
     * @throws IOException              if the DAO cannot write to disk
     * @throws IllegalArgumentException if any argument is invalid (from model)
     */
    public Transaction addTransaction(double amount, LocalDate date, String description,
                                      Category category, TransactionType type) throws IOException {
        Transaction t = new Transaction(amount, date, description, category, type);
        dao.save(t);
        return t;
    }

    /**
     * Removes the transaction with the given ID from persistent storage.
     * Does nothing if the ID does not match any stored transaction.
     *
     * @param id the transaction ID to remove (not null or empty)
     * @throws IOException              if the DAO cannot read or write disk
     * @throws IllegalArgumentException if id is null or empty
     */
    public void removeTransaction(String id) throws IOException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty");
        }
        dao.deleteById(id.trim());
    }

    /**
     * Returns all stored transactions.
     *
     * @return list of all transactions (may be empty)
     * @throws IOException if the DAO cannot read from disk
     */
    public List<Transaction> getAll() throws IOException {
        return dao.loadAll();
    }

    // -------------------------------------------------------------------------
    // Filtering and search
    // -------------------------------------------------------------------------

    /**
     * Returns all transactions that belong to the given category.
     *
     * @param category the category to filter by (not null)
     * @return matching transactions (empty list if none)
     * @throws IOException              if the DAO cannot read from disk
     * @throws IllegalArgumentException if category is null
     */
    public List<Transaction> filterByCategory(Category category) throws IOException {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return dao.findByCategory(category);
    }

    /**
     * Returns all transactions whose date falls within [from, to] inclusive.
     *
     * @param from start of the date range (not null)
     * @param to   end of the date range (not null, must be >= from)
     * @return transactions in the date range, in storage order
     * @throws IOException              if the DAO cannot read from disk
     * @throws IllegalArgumentException if either date is null or from is after to
     */
    public List<Transaction> filterByDateRange(LocalDate from, LocalDate to) throws IOException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Date range boundaries cannot be null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                "Start date cannot be after end date: " + from + " > " + to);
        }
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : dao.loadAll()) {
            LocalDate d = t.getDate();
            if (!d.isBefore(from) && !d.isAfter(to)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Returns all transactions whose description contains the given keyword
     * (case-insensitive substring match).
     *
     * @param keyword the search term (not null or empty)
     * @return matching transactions (empty list if none)
     * @throws IOException              if the DAO cannot read from disk
     * @throws IllegalArgumentException if keyword is null or empty
     */
    public List<Transaction> searchByDescription(String keyword) throws IOException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be null or empty");
        }
        String lower = keyword.trim().toLowerCase();
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : dao.loadAll()) {
            if (t.getDescription().toLowerCase().contains(lower)) {
                result.add(t);
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Export
    // -------------------------------------------------------------------------

    /**
     * Exports all transactions to a CSV file at the given path.
     * The file is overwritten if it already exists.
     * Descriptions containing commas or double-quotes are quoted per RFC 4180.
     *
     * @param path destination file path (not null or empty)
     * @throws IOException              if the file cannot be written
     * @throws IllegalArgumentException if path is null or empty
     */
    public void exportToCSV(String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Export path cannot be null or empty");
        }
        List<Transaction> transactions = dao.loadAll();
        List<String> lines = new ArrayList<>();
        lines.add("id,amount,date,description,category,type");
        for (Transaction t : transactions) {
            lines.add(String.join(",",
                t.getId(),
                String.valueOf(t.getAmount()),
                t.getDate().toString(),
                escapeCsvField(t.getDescription()),
                t.getCategory().name(),
                t.getType().name()
            ));
        }
        fileManager.writeLines(path.trim(), lines);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Wraps a field in double-quotes if it contains a comma or double-quote.
     * Internal double-quotes are escaped by doubling (RFC 4180).
     */
    private String escapeCsvField(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
