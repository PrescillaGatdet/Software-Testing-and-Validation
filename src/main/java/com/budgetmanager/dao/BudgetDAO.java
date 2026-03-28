package com.budgetmanager.dao;

import com.budgetmanager.model.Budget;
import com.budgetmanager.model.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Budget persistence.
 * Translates between Budget domain objects and their CSV representation.
 *
 * CSV format (one row per category budget):
 *   category,limit,currentSpending
 */
public class BudgetDAO {

    /** CSV header written at the top of a new budgets file. */
    private static final String HEADER = "category,limit,currentSpending";

    private final String filePath;
    private final FileManager fileManager;

    /**
     * Creates a BudgetDAO.
     *
     * @param filePath    path to the CSV file used for persistence
     * @param fileManager the FileManager instance to use for all I/O
     */
    public BudgetDAO(String filePath, FileManager fileManager) {
        this.filePath    = filePath;
        this.fileManager = fileManager;
    }

    /**
     * Saves a budget entry. If a budget for the same category already exists
     * in the file it is replaced; otherwise the entry is appended.
     *
     * @param budget the budget to persist
     * @throws IOException if the file cannot be read or written
     */
    public void save(Budget budget) throws IOException {
        List<Budget> existing = loadAll();
        List<Budget> updated  = new ArrayList<>();
        boolean replaced = false;

        for (Budget b : existing) {
            if (b.getCategory() == budget.getCategory()) {
                updated.add(budget); // replace with the new entry
                replaced = true;
            } else {
                updated.add(b);
            }
        }
        if (!replaced) {
            updated.add(budget); // new entry
        }
        writeAll(updated);
    }

    /**
     * Loads all budgets from the CSV file.
     * Returns an empty list when the file does not exist.
     *
     * @return list of all stored budgets
     * @throws IOException if the file exists but cannot be read
     */
    public List<Budget> loadAll() throws IOException {
        List<String> lines = fileManager.readLines(filePath);
        List<Budget> budgets = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("category,")) continue; // skip header
            Budget b = fromCsv(line);
            if (b != null) {
                budgets.add(b);
            }
        }
        return budgets;
    }

    /**
     * Finds the budget for a given category.
     *
     * @param category the category to look up
     * @return Optional containing the budget, or empty if no budget is set
     * @throws IOException if the file cannot be read
     */
    public Optional<Budget> findByCategory(Category category) throws IOException {
        return loadAll().stream()
                .filter(b -> b.getCategory() == category)
                .findFirst();
    }

    /**
     * Deletes the budget entry for a given category.
     * Does nothing if no entry exists for that category.
     *
     * @param category the category whose budget should be removed
     * @throws IOException if the file cannot be read or written
     */
    public void deleteByCategory(Category category) throws IOException {
        List<Budget> existing = loadAll();
        List<Budget> kept = new ArrayList<>();
        for (Budget b : existing) {
            if (b.getCategory() != category) {
                kept.add(b);
            }
        }
        writeAll(kept);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Rewrites the entire file with the given budgets plus a header row. */
    private void writeAll(List<Budget> budgets) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Budget b : budgets) {
            lines.add(toCsv(b));
        }
        fileManager.writeLines(filePath, lines);
    }

    /** Converts a Budget to its CSV row representation. */
    private String toCsv(Budget b) {
        return String.join(",",
                b.getCategory().name(),
                String.valueOf(b.getLimit()),
                String.valueOf(b.getCurrentSpending())
        );
    }

    /**
     * Parses a CSV row back into a Budget.
     * Returns null if the row is malformed.
     */
    private Budget fromCsv(String line) {
        try {
            String[] parts = line.split(",", 3);
            if (parts.length < 3) return null;

            Category category = Category.valueOf(parts[0].trim());
            double limit      = Double.parseDouble(parts[1].trim());
            double spending   = Double.parseDouble(parts[2].trim());

            Budget b = new Budget(category, limit);
            b.setCurrentSpending(spending);
            return b;
        } catch (Exception e) {
            return null; // skip malformed rows
        }
    }
}
