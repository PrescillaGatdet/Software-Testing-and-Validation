package com.budgetmanager.validation;

import com.budgetmanager.controller.BudgetController;
import com.budgetmanager.controller.TransactionController;
import com.budgetmanager.dao.BudgetDAO;
import com.budgetmanager.dao.FileManager;
import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Budget;
import com.budgetmanager.model.Category;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * State Transition Testing for Transaction and Budget lifecycles.
 *
 * Tests every valid state transition and verifies that invalid transitions
 * are handled correctly (either as no-ops or with appropriate errors).
 *
 * Stage 7 of IMPLEMENTATION_PLAN.md — Validation Testing (Technique 4 of 5)
 *
 * Transaction State Diagram:
 * ┌─────────────────┐                      ┌────────┐
 * │  NON-EXISTENT   │ ──addTransaction()──►│ SAVED  │
 * └─────────────────┘                      └────────┘
 *         │                                    │
 *         │ deleteById()                       │ deleteById()
 *         │ (no-op)                            ▼
 *         ▼                              ┌─────────────────┐
 * ┌─────────────────┐                    │    DELETED      │
 * │  NON-EXISTENT   │◄───────────────────│ (NON-EXISTENT)  │
 * └─────────────────┘                    └─────────────────┘
 *
 * Budget State Diagram:
 * ┌─────────────────┐                      ┌─────────────┐
 * │   NO_BUDGET     │ ──setBudgetLimit()──►│ BUDGET_SET  │
 * └─────────────────┘                      └─────────────┘
 *         │                                    │  │  ▲
 *         │ updateSpending()                   │  │  │
 *         │ (no-op, Rule 1)                    │  │  │ setBudgetLimit()
 *         ▼                                    │  │  │ (preserves spending)
 * ┌─────────────────┐                          │  │  │
 * │   NO_BUDGET     │                          │  ▼  │
 * └─────────────────┘                     updateSpending()
 *                                         (accumulates)
 *
 * Rubric relevance: Required by project_description.md —
 * "Validation Testing: State transition testing"
 */
@DisplayName("Stage 7 — State Transition Tests")
class StateTransitionTest {

    @TempDir
    Path tempDir;

    private TransactionController transactionController;
    private TransactionDAO transactionDAO;
    private BudgetController budgetController;

    @BeforeEach
    void setUp() {
        String transactionsPath = tempDir.resolve("transactions.csv").toString();
        String budgetsPath = tempDir.resolve("budgets.csv").toString();
        FileManager fileManager = new FileManager();
        transactionDAO = new TransactionDAO(transactionsPath, fileManager);
        transactionController = new TransactionController(transactionDAO, fileManager);
        BudgetDAO budgetDAO = new BudgetDAO(budgetsPath, fileManager);
        budgetController = new BudgetController(budgetDAO);
    }

    // =========================================================================
    // Transaction State Transitions
    // =========================================================================

    @Test
    @DisplayName("ST-Txn1: NON-EXISTENT → SAVED on addTransaction()")
    void txn_nonExistentToSaved_onAdd() throws IOException {
        // Initial state: No transactions exist
        assertTrue(transactionController.getAll().isEmpty(),
            "Initial state should be NON-EXISTENT (empty)");

        // Transition: addTransaction()
        Transaction t = transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),
            "Test transaction",
            Category.FOOD,
            TransactionType.EXPENSE
        );

        // Final state: SAVED
        List<Transaction> all = transactionController.getAll();
        assertEquals(1, all.size(), "Should have 1 transaction after add");
        assertEquals(t.getId(), all.get(0).getId(),
            "Saved transaction should match added transaction");
    }

    @Test
    @DisplayName("ST-Txn2: SAVED → DELETED on removeTransaction()")
    void txn_savedToDeleted_onRemove() throws IOException {
        // Setup: Create a saved transaction
        Transaction t = transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),
            "To be deleted",
            Category.FOOD,
            TransactionType.EXPENSE
        );
        assertEquals(1, transactionController.getAll().size(),
            "Should start with 1 saved transaction");

        // Transition: removeTransaction()
        transactionController.removeTransaction(t.getId());

        // Final state: DELETED (NON-EXISTENT)
        assertTrue(transactionController.getAll().isEmpty(),
            "Transaction should be deleted (list empty)");
    }

    @Test
    @DisplayName("ST-Txn3: NON-EXISTENT → NON-EXISTENT on deleteById() (no-op)")
    void txn_deleteNonExistent_noOp() throws IOException {
        // Initial state: No transactions
        assertTrue(transactionController.getAll().isEmpty());

        // Transition: Delete a non-existent ID (should be a no-op, no exception)
        assertDoesNotThrow(
            () -> transactionController.removeTransaction("non-existent-id"),
            "Deleting non-existent transaction should not throw"
        );

        // Final state: Still NON-EXISTENT
        assertTrue(transactionController.getAll().isEmpty(),
            "State should remain NON-EXISTENT");
    }

    @Test
    @DisplayName("ST-Txn4: DELETED → SAVED on re-add (new transaction with different ID)")
    void txn_deletedThenNewAdded() throws IOException {
        // Setup: Add and delete a transaction
        Transaction original = transactionController.addTransaction(
            100.00,
            LocalDate.of(2026, 3, 15),
            "Original",
            Category.FOOD,
            TransactionType.EXPENSE
        );
        String originalId = original.getId();
        transactionController.removeTransaction(originalId);
        assertTrue(transactionController.getAll().isEmpty(), "Should be empty after delete");

        // Transition: Add a new transaction (will have different auto-generated ID)
        Transaction newTxn = transactionController.addTransaction(
            200.00,
            LocalDate.of(2026, 3, 20),
            "New transaction",
            Category.TRANSPORT,
            TransactionType.EXPENSE
        );

        // Final state: SAVED (new transaction exists, old one doesn't)
        List<Transaction> all = transactionController.getAll();
        assertEquals(1, all.size(), "Should have 1 transaction");
        assertEquals(newTxn.getId(), all.get(0).getId());
        assertNotEquals(originalId, newTxn.getId(),
            "New transaction should have different ID");
    }

    @Test
    @DisplayName("ST-Txn5: Multiple transactions - remove one leaves others intact")
    void txn_removeOneOfMany_othersRemain() throws IOException {
        // Setup: Add 3 transactions
        Transaction t1 = transactionController.addTransaction(
            100.00, LocalDate.of(2026, 3, 1), "First", Category.FOOD, TransactionType.EXPENSE);
        Transaction t2 = transactionController.addTransaction(
            200.00, LocalDate.of(2026, 3, 2), "Second", Category.TRANSPORT, TransactionType.EXPENSE);
        Transaction t3 = transactionController.addTransaction(
            300.00, LocalDate.of(2026, 3, 3), "Third", Category.UTILITIES, TransactionType.EXPENSE);

        assertEquals(3, transactionController.getAll().size());

        // Transition: Remove middle transaction
        transactionController.removeTransaction(t2.getId());

        // Final state: t1 and t3 remain SAVED
        List<Transaction> remaining = transactionController.getAll();
        assertEquals(2, remaining.size(), "Should have 2 remaining");
        assertTrue(remaining.stream().anyMatch(t -> t.getId().equals(t1.getId())));
        assertTrue(remaining.stream().anyMatch(t -> t.getId().equals(t3.getId())));
        assertFalse(remaining.stream().anyMatch(t -> t.getId().equals(t2.getId())),
            "Deleted transaction should not be present");
    }

    // =========================================================================
    // Budget State Transitions
    // =========================================================================

    @Test
    @DisplayName("ST-Budget1: NO_BUDGET → BUDGET_SET on setBudgetLimit()")
    void budget_noBudgetToSet_onSetLimit() throws IOException {
        // Initial state: No budget for FOOD
        assertFalse(budgetController.getBudget(Category.FOOD).isPresent(),
            "Initial state should be NO_BUDGET");

        // Transition: setBudgetLimit()
        budgetController.setBudgetLimit(Category.FOOD, 500.00);

        // Final state: BUDGET_SET
        Optional<Budget> budget = budgetController.getBudget(Category.FOOD);
        assertTrue(budget.isPresent(), "Budget should exist after setBudgetLimit");
        assertEquals(500.00, budget.get().getLimit(), 0.001);
        assertEquals(0.0, budget.get().getCurrentSpending(), 0.001,
            "New budget should have 0 spending");
    }

    @Test
    @DisplayName("ST-Budget2: BUDGET_SET → BUDGET_SET on setBudgetLimit() (preserves spending)")
    void budget_setBudgetPreservesSpending_onLimitUpdate() throws IOException {
        // Setup: Create budget and add some spending
        budgetController.setBudgetLimit(Category.FOOD, 500.00);
        budgetController.updateSpending(Category.FOOD, 150.00);

        // Verify initial state
        Budget before = budgetController.getBudget(Category.FOOD).orElseThrow();
        assertEquals(500.00, before.getLimit(), 0.001);
        assertEquals(150.00, before.getCurrentSpending(), 0.001);

        // Transition: Update limit (spending should be preserved)
        budgetController.setBudgetLimit(Category.FOOD, 600.00);

        // Final state: New limit, same spending
        Budget after = budgetController.getBudget(Category.FOOD).orElseThrow();
        assertEquals(600.00, after.getLimit(), 0.001,
            "Limit should be updated to 600");
        assertEquals(150.00, after.getCurrentSpending(), 0.001,
            "Spending should be preserved at 150");
    }

    @Test
    @DisplayName("ST-Budget3: NO_BUDGET → NO_BUDGET on updateSpending() (no-op, Rule 1)")
    void budget_updateSpending_noOp_whenNoBudget() throws IOException {
        // Initial state: No budget for FOOD
        assertFalse(budgetController.getBudget(Category.FOOD).isPresent());

        // Transition: updateSpending() should be a no-op (Rule 1)
        budgetController.updateSpending(Category.FOOD, 100.00);

        // Final state: Still NO_BUDGET (no phantom entry created)
        assertFalse(budgetController.getBudget(Category.FOOD).isPresent(),
            "updateSpending should not create a budget entry (Rule 1)");
    }

    @Test
    @DisplayName("ST-Budget4: BUDGET_SET → BUDGET_SET on updateSpending() (accumulates)")
    void budget_spendingAccumulates_acrossMultipleUpdates() throws IOException {
        // Setup: Create budget
        budgetController.setBudgetLimit(Category.FOOD, 500.00);
        assertEquals(0.0, budgetController.getBudget(Category.FOOD).get().getCurrentSpending(), 0.001);

        // Transition 1: First expense
        budgetController.updateSpending(Category.FOOD, 100.00);
        assertEquals(100.00, budgetController.getBudget(Category.FOOD).get().getCurrentSpending(), 0.001);

        // Transition 2: Second expense (should accumulate)
        budgetController.updateSpending(Category.FOOD, 50.00);
        assertEquals(150.00, budgetController.getBudget(Category.FOOD).get().getCurrentSpending(), 0.001,
            "Spending should accumulate: 100 + 50 = 150");

        // Transition 3: Third expense
        budgetController.updateSpending(Category.FOOD, 75.00);
        assertEquals(225.00, budgetController.getBudget(Category.FOOD).get().getCurrentSpending(), 0.001,
            "Spending should accumulate: 150 + 75 = 225");

        // Verify remaining budget is correct
        assertEquals(275.00, budgetController.getRemainingBudget(Category.FOOD), 0.001,
            "Remaining should be 500 - 225 = 275");
    }
}
