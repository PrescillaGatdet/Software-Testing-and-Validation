package com.budgetmanager.dao;

import com.budgetmanager.model.Category;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Transaction persistence.
 * Translates between Transaction domain objects and their CSV representation.
 *
 * CSV format (one row per transaction):
 *   id,amount,date,description,category,type
 *
 * Design notes (MVC — DAO layer):
 *   - FileManager is injected via constructor for testability (Mockito mocking
 *     in unit tests, real FileManager in integration tests).
 *   - The header row is always written first when creating a new file.
 *   - Descriptions that contain commas are wrapped in double-quotes to avoid
 *     splitting on the wrong delimiter.
 *   - Malformed CSV lines are silently skipped rather than crashing — this
 *     satisfies Constraint C3 (data integrity for unexpected shutdowns).
 *
 * Constraints addressed:
 *   C2 (Local Storage): reads/writes only to the local filesystem via FileManager.
 *   C3 (Data Integrity): graceful handling of malformed lines; no partial writes.
 *   C8 (MVC): no dependency on View or Controller layers.
 *
 * Tested by: TransactionDAOTest
 */
public class TransactionDAO {

    /** CSV header written at the top of a new transactions file. */
    private static final String HEADER = "id,amount,date,description,category,type";

    private final String filePath;
    private final FileManager fileManager;

    /**
     * Creates a TransactionDAO.
     *
     * @param filePath    path to the CSV file used for persistence
     * @param fileManager the FileManager instance to use for all I/O
     */
    public TransactionDAO(String filePath, FileManager fileManager) {
        this.filePath    = filePath;
        this.fileManager = fileManager;
    }

    /**
     * Appends a transaction to the CSV file.
     * If the file does not exist yet, the header row is written first.
     *
     * @param transaction the transaction to persist
     * @throws IOException if the file cannot be written
     */
    public void save(Transaction transaction) throws IOException {
        if (!fileManager.fileExists(filePath)) {
            fileManager.appendLine(filePath, HEADER);
        }
        fileManager.appendLine(filePath, toCsv(transaction));
    }

    /**
     * Loads all transactions from the CSV file.
     * Returns an empty list when the file does not exist.
     * Silently skips rows that cannot be parsed.
     *
     * @return list of all stored transactions
     * @throws IOException if the file exists but cannot be read
     */
    public List<Transaction> loadAll() throws IOException {
        List<String> lines = fileManager.readLines(filePath);
        List<Transaction> transactions = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("id,")) continue; // skip header
            Transaction t = fromCsv(line);
            if (t != null) {
                transactions.add(t);
            }
        }
        return transactions;
    }

    /**
     * Deletes a transaction by ID by rewriting the file without that entry.
     * If no transaction with the given ID exists, the file is left unchanged.
     *
     * @param id the ID of the transaction to delete
     * @throws IOException if the file cannot be read or written
     */
    public void deleteById(String id) throws IOException {
        List<String> lines = fileManager.readLines(filePath);
        List<String> kept = new ArrayList<>();
        for (String line : lines) {
            // Always keep the header; keep data rows that do NOT match the ID
            if (line.startsWith("id,") || !line.startsWith(id + ",")) {
                kept.add(line);
            }
        }
        fileManager.writeLines(filePath, kept);
    }

    /**
     * Returns all transactions that belong to the specified category.
     *
     * @param category the category to filter by
     * @return list of matching transactions (empty if none found)
     * @throws IOException if the file cannot be read
     */
    public List<Transaction> findByCategory(Category category) throws IOException {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : loadAll()) {
            if (t.getCategory() == category) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Finds a transaction by its unique ID.
     *
     * @param id the transaction ID to look up
     * @return Optional containing the transaction, or empty if not found
     * @throws IOException if the file cannot be read
     */
    public Optional<Transaction> findById(String id) throws IOException {
        return loadAll().stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    // -------------------------------------------------------------------------
    // CSV serialisation helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a Transaction to its CSV row representation.
     * Wraps descriptions containing commas in double-quotes.
     */
    private String toCsv(Transaction t) {
        return String.join(",",
                t.getId(),
                String.valueOf(t.getAmount()),
                t.getDate().toString(),
                escapeCommas(t.getDescription()),
                t.getCategory().name(),
                t.getType().name()
        );
    }

    /**
     * Parses a CSV row back into a Transaction.
     * Returns null if the row is malformed, so the caller can skip it.
     *
     * Uses parseCsvLine() instead of String.split() to correctly handle
     * description fields that contain commas wrapped in double-quotes.
     */
    private Transaction fromCsv(String line) {
        try {
            String[] parts = parseCsvLine(line);
            if (parts.length < 6) return null;

            String id            = parts[0].trim();
            double amount        = Double.parseDouble(parts[1].trim());
            LocalDate date       = LocalDate.parse(parts[2].trim());
            String description   = parts[3].trim();
            Category category    = Category.valueOf(parts[4].trim());
            TransactionType type = TransactionType.valueOf(parts[5].trim());

            return new Transaction(id, amount, date, description, category, type);
        } catch (Exception e) {
            // Skip any row that cannot be parsed cleanly
            return null;
        }
    }

    /**
     * Wraps a field value in double-quotes if it contains a comma.
     * Internal double-quotes are escaped by doubling them ("" per RFC 4180).
     */
    private String escapeCommas(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Parses a single CSV line into an array of field values, correctly
     * handling fields that are wrapped in double-quotes (including fields
     * that contain commas or escaped double-quotes inside them).
     *
     * Algorithm: walk character by character; toggle inQuotes on unescaped
     * double-quotes; split on commas that are outside quotes only.
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // Check for an escaped double-quote ("") inside a quoted field
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // consume the second quote
                } else {
                    inQuotes = !inQuotes; // enter or exit quoted section
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString()); // add the last field
        return fields.toArray(new String[0]);
    }
}
