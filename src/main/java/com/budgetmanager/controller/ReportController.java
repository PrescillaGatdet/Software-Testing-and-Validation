package com.budgetmanager.controller;

import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Category;
import com.budgetmanager.model.Report;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;

import java.io.IOException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Controller for generating financial summary reports.
 * Aggregates transaction data to produce monthly reports, yearly reports,
 * and per-category expense breakdowns.
 *
 * Design notes (MVC — CONTROLLER layer):
 *   - TransactionDAO is injected via constructor for testability
 *     (Mockito mock in unit tests, real DAO in integration tests).
 *   - All computed values come from raw Transaction data; no caching is used
 *     so that reports always reflect the current state of the CSV files.
 *   - Report objects are immutable (see Report model) — each generate call
 *     creates a fresh Report instance.
 *   - No console I/O — display is handled by ReportView.
 *
 * Constraints addressed:
 *   C3 (Data Integrity): month validation prevents nonsense period labels.
 *   C7 (Code Coverage): every public method is fully unit-tested via mocks.
 *   C8 (MVC): depends only on DAO and model layers; zero View dependency.
 *
 * Tested by: ReportControllerTest
 */
public class ReportController {

    private final TransactionDAO transactionDAO;

    /**
     * Creates a ReportController backed by the given DAO.
     *
     * @param transactionDAO DAO for reading transaction records
     */
    public ReportController(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    // -------------------------------------------------------------------------
    // Report generation
    // -------------------------------------------------------------------------

    /**
     * Generates a financial summary for a specific calendar month.
     * Transactions outside the given year/month are ignored.
     *
     * @param year  the 4-digit calendar year (e.g., 2026)
     * @param month the calendar month 1–12
     * @return a Report with totalIncome, totalExpense, and a period label
     *         such as "March 2026"
     * @throws IOException              if the DAO cannot read from disk
     * @throws IllegalArgumentException if month is not in the range 1–12
     */
    public Report generateMonthlyReport(int year, int month) throws IOException {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                "Month must be between 1 and 12, got: " + month);
        }
        double income  = 0.0;
        double expense = 0.0;
        for (Transaction t : transactionDAO.loadAll()) {
            if (t.getDate().getYear() == year && t.getDate().getMonthValue() == month) {
                if (t.getType() == TransactionType.INCOME) {
                    income += t.getAmount();
                } else {
                    expense += t.getAmount();
                }
            }
        }
        String period = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                        + " " + year;
        return new Report(income, expense, period);
    }

    /**
     * Generates a financial summary for an entire calendar year.
     * All transactions for the given year are aggregated regardless of month.
     *
     * @param year the 4-digit calendar year (e.g., 2026)
     * @return a Report with totalIncome, totalExpense, and period label = year
     * @throws IOException if the DAO cannot read from disk
     */
    public Report generateYearlyReport(int year) throws IOException {
        double income  = 0.0;
        double expense = 0.0;
        for (Transaction t : transactionDAO.loadAll()) {
            if (t.getDate().getYear() == year) {
                if (t.getType() == TransactionType.INCOME) {
                    income += t.getAmount();
                } else {
                    expense += t.getAmount();
                }
            }
        }
        return new Report(income, expense, String.valueOf(year));
    }

    /**
     * Generates a map of total EXPENSE amounts per category.
     * Only EXPENSE transactions are counted; INCOME transactions are ignored.
     * Categories with zero expenses are omitted from the result.
     *
     * @return Map from Category to cumulative expense amount;
     *         entries are ordered by Category enum declaration order
     * @throws IOException if the DAO cannot read from disk
     */
    public Map<Category, Double> generateCategoryBreakdown() throws IOException {
        Map<Category, Double> breakdown = new EnumMap<>(Category.class);
        for (Transaction t : transactionDAO.loadAll()) {
            if (t.getType() == TransactionType.EXPENSE) {
                breakdown.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return breakdown;
    }
}
