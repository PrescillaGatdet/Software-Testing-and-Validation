package com.budgetmanager.integration;

import com.budgetmanager.controller.BudgetController;
import com.budgetmanager.controller.TransactionController;
import com.budgetmanager.dao.BudgetDAO;
import com.budgetmanager.dao.FileManager;
import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Category;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("Stage 6 — Transaction Integration Tests")
class TransactionIntegrationTest {

    @TempDir
    Path tempDir;

    private String transactionsPath;
    private String budgetsPath;
    private FileManager fileManager;
    private TransactionDAO transactionDAO;
    private BudgetDAO budgetDAO;
    private TransactionController transactionController;
    private BudgetController budgetController;

    @BeforeEach
    void setUp() {
        transactionsPath = tempDir.resolve("transactions.csv").toString();
        budgetsPath = tempDir.resolve("budgets.csv").toString();
        fileManager = new FileManager();
        transactionDAO = new TransactionDAO(transactionsPath, fileManager);
        budgetDAO = new BudgetDAO(budgetsPath, fileManager);
        transactionController = new TransactionController(transactionDAO, fileManager);
        budgetController = new BudgetController(budgetDAO);
    }

    // =========================================================================
    // Test 1: addTransaction_persistsToFile
    // =========================================================================

    @Test
    @DisplayName("addTransaction persists data to file — verifiable by reading file directly")
    void addTransaction_persistsToFile() throws IOException {
        // Act: Add a transaction via controller
        Transaction t = transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),
            "Grocery shopping",
            Category.FOOD,
            TransactionType.EXPENSE
        );

        // Assert: File should exist and contain the transaction data
        assertTrue(Files.exists(Path.of(transactionsPath)), "CSV file should be created");

        List<String> lines = Files.readAllLines(Path.of(transactionsPath));
        assertEquals(2, lines.size(), "File should have header + 1 data row");
        assertTrue(lines.get(0).contains("id,amount,date,description,category,type"),
            "First line should be the header");
        assertTrue(lines.get(1).contains(t.getId()), "Data row should contain transaction ID");
        assertTrue(lines.get(1).contains("100.0"), "Data row should contain amount");
        assertTrue(lines.get(1).contains("FOOD"), "Data row should contain category");
    }

    // =========================================================================
    // Test 2: addAndReload_transactionSurvivesRoundTrip
    // =========================================================================

    @Test
    @DisplayName("Transaction survives round-trip — new DAO instance loads same data")
    void addAndReload_transactionSurvivesRoundTrip() throws IOException {
        // Arrange: Add a transaction
        Transaction original = transactionController.addTransaction(
            250.50,
            LocalDate.of(2026, 3, 20),
            "Monthly utilities",
            Category.UTILITIES,
            TransactionType.EXPENSE
        );

        // Act: Create completely new DAO and controller instances
        TransactionDAO newDAO = new TransactionDAO(transactionsPath, new FileManager());
        TransactionController newController = new TransactionController(newDAO);
        List<Transaction> reloaded = newController.getAll();

        // Assert: Transaction should survive with all fields intact
        assertEquals(1, reloaded.size(), "Should have exactly 1 transaction");
        Transaction loaded = reloaded.get(0);
        assertEquals(original.getId(), loaded.getId());
        assertEquals(250.50, loaded.getAmount(), 0.001);
        assertEquals(LocalDate.of(2026, 3, 20), loaded.getDate());
        assertEquals("Monthly utilities", loaded.getDescription());
        assertEquals(Category.UTILITIES, loaded.getCategory());
        assertEquals(TransactionType.EXPENSE, loaded.getType());
    }

    // =========================================================================
    // Test 3: removeTransaction_removedFromFile
    // =========================================================================

    @Test
    @DisplayName("removeTransaction removes entry from file — only the correct one")
    void removeTransaction_removedFromFile() throws IOException {
        // Arrange: Add 2 transactions
        Transaction t1 = transactionController.addTransaction(
            100.00, LocalDate.of(2026, 3, 1), "Transaction 1",
            Category.FOOD, TransactionType.EXPENSE
        );
        Transaction t2 = transactionController.addTransaction(
            200.00, LocalDate.of(2026, 3, 2), "Transaction 2",
            Category.TRANSPORT, TransactionType.EXPENSE
        );

        // Act: Remove the first transaction
        transactionController.removeTransaction(t1.getId());

        // Assert: Only second transaction remains
        List<Transaction> remaining = transactionController.getAll();
        assertEquals(1, remaining.size(), "Should have exactly 1 transaction remaining");
        assertEquals(t2.getId(), remaining.get(0).getId(), "Remaining transaction should be t2");

        // Verify via fresh DAO instance
        TransactionDAO freshDAO = new TransactionDAO(transactionsPath, new FileManager());
        List<Transaction> freshLoad = freshDAO.loadAll();
        assertEquals(1, freshLoad.size());
        assertEquals(t2.getId(), freshLoad.get(0).getId());
    }

    // =========================================================================
    // Test 4: filterByCategory_afterPersist
    // =========================================================================

    @Test
    @DisplayName("filterByCategory works correctly after transactions are persisted")
    void filterByCategory_afterPersist() throws IOException {
        // Arrange: Add transactions in different categories
        transactionController.addTransaction(
            100.00, LocalDate.of(2026, 3, 1), "Lunch",
            Category.FOOD, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            50.00, LocalDate.of(2026, 3, 2), "Bus fare",
            Category.TRANSPORT, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            75.00, LocalDate.of(2026, 3, 3), "Dinner",
            Category.FOOD, TransactionType.EXPENSE
        );

        // Act: Filter by FOOD category
        List<Transaction> foodTransactions = transactionController.filterByCategory(Category.FOOD);

        // Assert: Only FOOD transactions returned
        assertEquals(2, foodTransactions.size(), "Should return 2 FOOD transactions");
        for (Transaction t : foodTransactions) {
            assertEquals(Category.FOOD, t.getCategory(), "All should be FOOD category");
        }

        // Act: Filter by TRANSPORT category
        List<Transaction> transportTransactions = transactionController.filterByCategory(Category.TRANSPORT);
        assertEquals(1, transportTransactions.size(), "Should return 1 TRANSPORT transaction");
        assertEquals(Category.TRANSPORT, transportTransactions.get(0).getCategory());
    }

    // =========================================================================
    // Test 5: filterByDateRange_acrossRealFile
    // =========================================================================

    @Test
    @DisplayName("filterByDateRange correctly filters transactions from persisted file")
    void filterByDateRange_acrossRealFile() throws IOException {
        // Arrange: Add transactions across Jan, Feb, Mar 2026
        transactionController.addTransaction(
            100.00, LocalDate.of(2026, 1, 15), "January expense",
            Category.FOOD, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            200.00, LocalDate.of(2026, 2, 10), "February expense 1",
            Category.FOOD, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            150.00, LocalDate.of(2026, 2, 20), "February expense 2",
            Category.TRANSPORT, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            300.00, LocalDate.of(2026, 3, 5), "March expense",
            Category.ENTERTAINMENT, TransactionType.EXPENSE
        );

        // Act: Filter by February date range
        List<Transaction> febTransactions = transactionController.filterByDateRange(
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28)
        );

        // Assert: Only February transactions returned
        assertEquals(2, febTransactions.size(), "Should return 2 February transactions");
        for (Transaction t : febTransactions) {
            assertEquals(2, t.getDate().getMonthValue(), "All should be in February");
        }
    }

    // =========================================================================
    // Test 6: searchByDescription_caseInsensitive
    // =========================================================================

    @Test
    @DisplayName("searchByDescription performs case-insensitive search on persisted data")
    void searchByDescription_caseInsensitive() throws IOException {
        // Arrange: Add transactions with various descriptions
        transactionController.addTransaction(
            100.00, LocalDate.of(2026, 3, 1), "Grocery Shopping at Walmart",
            Category.FOOD, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            50.00, LocalDate.of(2026, 3, 2), "Bus ticket",
            Category.TRANSPORT, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            75.00, LocalDate.of(2026, 3, 3), "Weekly grocery run",
            Category.FOOD, TransactionType.EXPENSE
        );

        // Act: Search with lowercase keyword
        List<Transaction> results = transactionController.searchByDescription("grocery");

        // Assert: Both transactions with "grocery" (case-insensitive) found
        assertEquals(2, results.size(), "Should find 2 transactions matching 'grocery'");
        for (Transaction t : results) {
            assertTrue(
                t.getDescription().toLowerCase().contains("grocery"),
                "Description should contain 'grocery' (case-insensitive)"
            );
        }

        // Act: Search with mixed case
        List<Transaction> mixedCaseResults = transactionController.searchByDescription("WALMART");
        assertEquals(1, mixedCaseResults.size(), "Should find 1 transaction matching 'WALMART'");
    }

    // =========================================================================
    // Test 7: exportToCSV_createsReadableFile
    // =========================================================================

    @Test
    @DisplayName("exportToCSV creates a readable CSV file with header and data rows")
    void exportToCSV_createsReadableFile() throws IOException {
        // Arrange: Add 3 transactions
        transactionController.addTransaction(
            100.00, LocalDate.of(2026, 3, 1), "Food expense",
            Category.FOOD, TransactionType.EXPENSE
        );
        transactionController.addTransaction(
            2500.00, LocalDate.of(2026, 3, 1), "Monthly salary",
            Category.SALARY, TransactionType.INCOME
        );
        transactionController.addTransaction(
            50.00, LocalDate.of(2026, 3, 2), "Movie ticket",
            Category.ENTERTAINMENT, TransactionType.EXPENSE
        );

        // Act: Export to a different CSV file
        String exportPath = tempDir.resolve("export.csv").toString();
        transactionController.exportToCSV(exportPath);

        // Assert: Exported file has header + 3 data rows
        assertTrue(Files.exists(Path.of(exportPath)), "Export file should exist");
        List<String> lines = Files.readAllLines(Path.of(exportPath));

        assertEquals(4, lines.size(), "Export should have header + 3 data rows");
        assertEquals("id,amount,date,description,category,type", lines.get(0),
            "First line should be CSV header");

        // Verify data rows contain expected values
        assertTrue(lines.get(1).contains("FOOD") || lines.get(2).contains("FOOD") || lines.get(3).contains("FOOD"),
            "One line should contain FOOD");
        assertTrue(lines.get(1).contains("INCOME") || lines.get(2).contains("INCOME") || lines.get(3).contains("INCOME"),
            "One line should contain INCOME");
    }

    // =========================================================================
    // Test 8: addExpense_updatesBudgetSpending
    // =========================================================================

    @Test
    @DisplayName("Adding expense transaction updates budget spending correctly")
    void addExpense_updatesBudgetSpending() throws IOException {
        // Arrange: Set up a budget for FOOD category
        budgetController.setBudgetLimit(Category.FOOD, 500.00);

        // Act: Add FOOD expense and manually update budget (as Main.java does)
        Transaction t = transactionController.addTransaction(
            150.00, LocalDate.of(2026, 3, 15), "Grocery shopping",
            Category.FOOD, TransactionType.EXPENSE
        );

        // Simulate what Main.java does on expense: update budget spending
        if (t.getType() == TransactionType.EXPENSE) {
            budgetController.updateSpending(t.getCategory(), t.getAmount());
        }

        // Assert: Budget spending is updated
        assertFalse(budgetController.isOverBudget(Category.FOOD),
            "Should not be over budget (150 < 500)");
        assertEquals(350.00, budgetController.getRemainingBudget(Category.FOOD), 0.001,
            "Remaining budget should be 350 (500 - 150)");

        // Add another expense that brings it near limit
        Transaction t2 = transactionController.addTransaction(
            300.00, LocalDate.of(2026, 3, 16), "Restaurant dinner",
            Category.FOOD, TransactionType.EXPENSE
        );
        budgetController.updateSpending(t2.getCategory(), t2.getAmount());

        // Now at 450/500 = 90% — should trigger near-limit warning
        List<String> alerts = budgetController.checkAllAlerts();
        assertEquals(1, alerts.size(), "Should have 1 warning alert");
        assertTrue(alerts.get(0).contains("WARNING"), "Alert should be a WARNING");
        assertTrue(alerts.get(0).contains("Food"), "Alert should mention Food category");
    }
}
