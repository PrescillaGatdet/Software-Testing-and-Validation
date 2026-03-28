package com.budgetmanager.controller;

import com.budgetmanager.dao.BudgetDAO;
import com.budgetmanager.model.Budget;
import com.budgetmanager.model.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for budget limit management and alert generation.
 * Handles setting spending limits per category, tracking expenses against
 * those limits, and generating human-readable alert messages.
 *
 * Alert logic (maps directly to Decision Table in TESTING.md):
 *   Rule 1 — No budget set for category   → no alert.
 *   Rule 2 — Spending ≤ 80 % of limit     → no alert.
 *   Rule 3 — Spending 80–100 % of limit   → WARNING alert.
 *   Rule 4 — Spending > 100 % of limit    → EXCEEDED alert.
 */
public class BudgetController {

    private final BudgetDAO dao;

    /**
     * Creates a BudgetController backed by the given DAO.
     *
     * @param dao the BudgetDAO to delegate persistence to
     */
    public BudgetController(BudgetDAO dao) {
        this.dao = dao;
    }

    // -------------------------------------------------------------------------
    // Budget configuration
    // -------------------------------------------------------------------------

    /**
     * Sets or replaces the spending limit for a category.
     * If a budget already exists for the category its limit is updated while
     * the current spending total is preserved.
     *
     * @param category the category to configure (not null)
     * @param limit    the new spending limit (must be > 0)
     * @throws IOException              if the DAO cannot read or write disk
     * @throws IllegalArgumentException if category is null or limit is not positive
     */
    public void setBudgetLimit(Category category, double limit) throws IOException {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0, got: " + limit);
        }
        Optional<Budget> existing = dao.findByCategory(category);
        Budget budget;
        if (existing.isPresent()) {
            // Preserve current spending; only update the limit
            budget = existing.get();
            budget.setLimit(limit);
        } else {
            budget = new Budget(category, limit);
        }
        dao.save(budget);
    }

    /**
     * Adds an expense amount to the running spending total for a category.
     * Has no effect if no budget limit has been set for the category
     * (satisfies Decision Table Rule 1 — no phantom budget entries).
     *
     * @param category the category whose spending to update (not null)
     * @param amount   the expense amount to add (must be >= 0)
     * @throws IOException              if the DAO cannot read or write disk
     * @throws IllegalArgumentException if category is null or amount is negative
     */
    public void updateSpending(Category category, double amount) throws IOException {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative, got: " + amount);
        }
        Optional<Budget> existing = dao.findByCategory(category);
        if (existing.isPresent()) {
            Budget budget = existing.get();
            budget.setCurrentSpending(budget.getCurrentSpending() + amount);
            dao.save(budget);
        }
        // No budget set — silently ignore (Rule 1 of decision table)
    }

    // -------------------------------------------------------------------------
    // Budget queries
    // -------------------------------------------------------------------------

    /**
     * Returns true if the current spending for the category strictly exceeds
     * the budget limit.  Returns false when no budget has been set.
     *
     * @param category the category to check (not null)
     * @return true if over budget, false otherwise or if no budget set
     * @throws IOException              if the DAO cannot read from disk
     * @throws IllegalArgumentException if category is null
     */
    public boolean isOverBudget(Category category) throws IOException {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return dao.findByCategory(category)
                  .map(Budget::isExceeded)
                  .orElse(false);
    }

    /**
     * Returns the remaining budget (limit minus current spending) for a category.
     * Returns {@code Double.MAX_VALUE} when no budget limit has been set,
     * indicating effectively unlimited budget.
     *
     * @param category the category to query (not null)
     * @return remaining amount, or Double.MAX_VALUE if no budget set
     * @throws IOException              if the DAO cannot read from disk
     * @throws IllegalArgumentException if category is null
     */
    public double getRemainingBudget(Category category) throws IOException {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return dao.findByCategory(category)
                  .map(Budget::getRemainingBudget)
                  .orElse(Double.MAX_VALUE);
    }

    /**
     * Returns the Budget object for a category, or empty if none is set.
     *
     * @param category the category to look up (not null)
     * @return Optional containing the Budget, or empty
     * @throws IOException              if the DAO cannot read from disk
     * @throws IllegalArgumentException if category is null
     */
    public Optional<Budget> getBudget(Category category) throws IOException {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return dao.findByCategory(category);
    }

    /**
     * Returns all budgets that have been configured.
     *
     * @return list of all Budget entries (may be empty)
     * @throws IOException if the DAO cannot read from disk
     */
    public List<Budget> getAllBudgets() throws IOException {
        return dao.loadAll();
    }

    // -------------------------------------------------------------------------
    // Alert generation
    // -------------------------------------------------------------------------

    /**
     * Checks all budgets and returns a list of alert messages for categories
     * that are near or over their limit.
     *
     * Alert rules (Decision Table):
     *   - No entry in DAO     → Rule 1: no alert
     *   - usage < 80 %        → Rule 2: no alert
     *   - 80 % <= usage <= 100% → Rule 3: WARNING message
     *   - usage > 100 %       → Rule 4: EXCEEDED message
     *
     * @return list of alert strings (empty if no alerts)
     * @throws IOException if the DAO cannot read from disk
     */
    public List<String> checkAllAlerts() throws IOException {
        List<String> alerts = new ArrayList<>();
        for (Budget budget : dao.loadAll()) {
            if (budget.isExceeded()) {
                // Rule 4 — EXCEEDED
                alerts.add(String.format(
                    "EXCEEDED: %s - spent %.2f of %.2f limit",
                    budget.getCategory().getDisplayName(),
                    budget.getCurrentSpending(),
                    budget.getLimit()
                ));
            } else if (budget.isNearLimit()) {
                // Rule 3 — WARNING (80-100%)
                alerts.add(String.format(
                    "WARNING: %s - spent %.2f of %.2f (%.0f%% used)",
                    budget.getCategory().getDisplayName(),
                    budget.getCurrentSpending(),
                    budget.getLimit(),
                    budget.getUsagePercentage() * 100
                ));
            }
            // Rules 1 and 2 produce no alert — nothing added
        }
        return alerts;
    }
}
