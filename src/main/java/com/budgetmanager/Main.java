package com.budgetmanager;

import com.budgetmanager.controller.BudgetController;
import com.budgetmanager.controller.ReportController;
import com.budgetmanager.controller.TransactionController;
import com.budgetmanager.dao.BudgetDAO;
import com.budgetmanager.dao.FileManager;
import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Budget;
import com.budgetmanager.model.Category;
import com.budgetmanager.model.Report;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;
import com.budgetmanager.view.ConsoleView;
import com.budgetmanager.view.ReportView;
import com.budgetmanager.view.TransactionView;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Entry point for the Budget Management System.
 *
 * Wires together all MVC layers:
 *   Model       — Transaction, Budget, Report (in com.budgetmanager.model)
 *   DAO (I/O)   — TransactionDAO, BudgetDAO, FileManager
 *   Controller  — TransactionController, BudgetController, ReportController
 *   View        — ConsoleView, TransactionView, ReportView
 *
 * All CSV data is stored in the local "data/" directory (Constraint C2).
 * The application runs as a console loop until the user selects "0. Exit".
 *
 * Usage:
 *   mvn package
 *   java -jar target/budget-management-system-1.0-SNAPSHOT.jar
 *
 * Constraints addressed here:
 *   C4 (Cross-Platform): uses only standard Java I/O and relative file paths.
 *   C6 (Clear Feedback): every action confirms success or prints an error.
 */
public class Main {

    /** Path to the transactions CSV file (relative to working directory). */
    private static final String TRANSACTIONS_FILE = "data/transactions.csv";

    /** Path to the budgets CSV file (relative to working directory). */
    private static final String BUDGETS_FILE = "data/budgets.csv";

    public static void main(String[] args) {
        // ------------------------------------------------------------------
        // Dependency wiring
        // ------------------------------------------------------------------
        FileManager     fileManager = new FileManager();
        TransactionDAO  txnDAO      = new TransactionDAO(TRANSACTIONS_FILE, fileManager);
        BudgetDAO       budgetDAO   = new BudgetDAO(BUDGETS_FILE, fileManager);

        TransactionController txnCtrl    = new TransactionController(txnDAO, fileManager);
        BudgetController      budgetCtrl = new BudgetController(budgetDAO);
        ReportController      reportCtrl = new ReportController(txnDAO);

        ConsoleView     console  = new ConsoleView();
        TransactionView txnView  = new TransactionView();
        ReportView      repView  = new ReportView();

        // ------------------------------------------------------------------
        // Welcome banner
        // ------------------------------------------------------------------
        console.showMessage("╔══════════════════════════════════════════╗");
        console.showMessage("║    Budget Management System v1.0         ║");
        console.showMessage("║    ENSE 375 — Software Testing           ║");
        console.showMessage("╚══════════════════════════════════════════╝");

        // ------------------------------------------------------------------
        // Main application loop
        // ------------------------------------------------------------------
        boolean running = true;
        while (running) {
            console.showMainMenu();
            String choice = console.readInput();

            try {
                switch (choice) {
                    case "1":
                        handleAddTransaction(console, txnView, txnCtrl, budgetCtrl);
                        break;
                    case "2":
                        txnView.displayTransactionList(txnCtrl.getAll());
                        break;
                    case "3":
                        handleFilterByCategory(console, txnView, txnCtrl);
                        break;
                    case "4":
                        handleFilterByDateRange(console, txnView, txnCtrl);
                        break;
                    case "5":
                        handleSearchByDescription(console, txnView, txnCtrl);
                        break;
                    case "6":
                        handleSetBudgetLimit(console, budgetCtrl);
                        break;
                    case "7":
                        repView.displayBudgetStatus(budgetCtrl.getAllBudgets());
                        break;
                    case "8":
                        repView.displayAlerts(budgetCtrl.checkAllAlerts());
                        break;
                    case "9":
                        handleMonthlyReport(console, repView, reportCtrl);
                        break;
                    case "10":
                        handleYearlyReport(console, repView, reportCtrl);
                        break;
                    case "11":
                        repView.displayCategoryBreakdown(
                            reportCtrl.generateCategoryBreakdown());
                        break;
                    case "0":
                        running = false;
                        console.showMessage("Goodbye!");
                        break;
                    default:
                        console.showError("Unknown option '" + choice + "'. Enter 0-11.");
                }
            } catch (IOException e) {
                console.showError("File I/O error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                console.showError("Invalid input: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Handler helpers — each handles one menu option
    // -------------------------------------------------------------------------

    /**
     * Prompts the user for all transaction fields, creates the transaction,
     * and updates the spending budget for the category if applicable.
     */
    private static void handleAddTransaction(ConsoleView console,
                                             TransactionView txnView,
                                             TransactionController txnCtrl,
                                             BudgetController budgetCtrl) throws IOException {
        txnView.showAddTransactionPrompt();

        double    amount = console.readDouble("  Amount: $");
        LocalDate date   = console.readDate("  Date");

        console.showMessage("  Description (1-100 chars):");
        console.showMessage("  > ");
        String desc = console.readInput();

        // Category selection
        console.showCategoryMenu();
        Category category = parseCategory(console.readInput());
        if (category == null) {
            console.showError("Invalid category. Transaction cancelled.");
            return;
        }

        // Type selection
        console.showTypeMenu();
        TransactionType type = parseType(console.readInput());
        if (type == null) {
            console.showError("Invalid type. Transaction cancelled.");
            return;
        }

        Transaction t = txnCtrl.addTransaction(amount, date, desc, category, type);
        console.showSuccess("Transaction added: " + t.getId());

        // Update budget spending for expense transactions
        if (type == TransactionType.EXPENSE) {
            budgetCtrl.updateSpending(category, amount);
        }
    }

    /**
     * Prompts for a category and displays matching transactions.
     */
    private static void handleFilterByCategory(ConsoleView console,
                                               TransactionView txnView,
                                               TransactionController txnCtrl) throws IOException {
        console.showCategoryMenu();
        Category category = parseCategory(console.readInput());
        if (category == null) {
            console.showError("Invalid category.");
            return;
        }
        List<Transaction> results = txnCtrl.filterByCategory(category);
        txnView.displayTransactionList(results);
    }

    /**
     * Prompts for start and end dates and displays transactions in that range.
     */
    private static void handleFilterByDateRange(ConsoleView console,
                                                TransactionView txnView,
                                                TransactionController txnCtrl) throws IOException {
        LocalDate from = console.readDate("  Start date");
        LocalDate to   = console.readDate("  End date");
        List<Transaction> results = txnCtrl.filterByDateRange(from, to);
        txnView.displayTransactionList(results);
    }

    /**
     * Prompts for a keyword and displays matching transactions.
     */
    private static void handleSearchByDescription(ConsoleView console,
                                                  TransactionView txnView,
                                                  TransactionController txnCtrl) throws IOException {
        console.showMessage("  Search keyword: ");
        String keyword = console.readInput();
        List<Transaction> results = txnCtrl.searchByDescription(keyword);
        txnView.displayTransactionList(results);
    }

    /**
     * Prompts for a category and limit and sets the budget.
     */
    private static void handleSetBudgetLimit(ConsoleView console,
                                             BudgetController budgetCtrl) throws IOException {
        console.showCategoryMenu();
        Category category = parseCategory(console.readInput());
        if (category == null) {
            console.showError("Invalid category.");
            return;
        }
        double limit = console.readDouble("  Monthly limit: $");
        budgetCtrl.setBudgetLimit(category, limit);
        console.showSuccess("Budget set: " + category.getDisplayName()
            + " — $" + String.format("%.2f", limit));
    }

    /**
     * Prompts for year and month and generates a monthly report.
     */
    private static void handleMonthlyReport(ConsoleView console,
                                            ReportView repView,
                                            ReportController reportCtrl) throws IOException {
        int year  = console.readInt("  Year (e.g. 2026): ");
        int month = console.readInt("  Month (1-12): ");
        Report report = reportCtrl.generateMonthlyReport(year, month);
        repView.displayReport(report);
    }

    /**
     * Prompts for a year and generates a yearly report.
     */
    private static void handleYearlyReport(ConsoleView console,
                                           ReportView repView,
                                           ReportController reportCtrl) throws IOException {
        int year = console.readInt("  Year (e.g. 2026): ");
        Report report = reportCtrl.generateYearlyReport(year);
        repView.displayReport(report);
    }

    // -------------------------------------------------------------------------
    // Input parsing helpers
    // -------------------------------------------------------------------------

    /**
     * Maps a user's 1-7 selection to the corresponding Category enum value.
     *
     * @param input the user's raw input string
     * @return the selected Category, or null if the input is invalid
     */
    private static Category parseCategory(String input) {
        switch (input.trim()) {
            case "1": return Category.FOOD;
            case "2": return Category.TRANSPORT;
            case "3": return Category.ENTERTAINMENT;
            case "4": return Category.UTILITIES;
            case "5": return Category.SALARY;
            case "6": return Category.INVESTMENT;
            case "7": return Category.OTHER;
            default:  return null;
        }
    }

    /**
     * Maps a user's 1-2 selection to EXPENSE or INCOME.
     *
     * @param input the user's raw input string
     * @return the selected TransactionType, or null if invalid
     */
    private static TransactionType parseType(String input) {
        switch (input.trim()) {
            case "1": return TransactionType.EXPENSE;
            case "2": return TransactionType.INCOME;
            default:  return null;
        }
    }
}
