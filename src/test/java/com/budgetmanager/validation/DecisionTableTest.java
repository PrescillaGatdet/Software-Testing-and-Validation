package com.budgetmanager.validation;

import com.budgetmanager.controller.BudgetController;
import com.budgetmanager.dao.BudgetDAO;
import com.budgetmanager.dao.FileManager;
import com.budgetmanager.model.Category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Decision Table Testing for Budget Alert System.
 *
 * Tests all combinations of conditions that drive budget alert behavior.
 * The Decision Table maps directly to BudgetController.checkAllAlerts().
 *
 * Decision Table:
 * ┌─────────────────────────────┬────────┬────────┬────────┬────────┐
 * │ Condition                   │ Rule 1 │ Rule 2 │ Rule 3 │ Rule 4 │
 * ├─────────────────────────────┼────────┼────────┼────────┼────────┤
 * │ Budget limit set?           │   No   │  Yes   │  Yes   │  Yes   │
 * │ Spending > 100% of limit?   │   —    │   No   │   No   │  Yes   │
 * │ Spending ≥ 80% of limit?    │   —    │   No   │  Yes   │  Yes   │
 * ├─────────────────────────────┼────────┼────────┼────────┼────────┤
 * │ Action: No alert            │   ✓    │   ✓    │   —    │   —    │
 * │ Action: WARNING alert       │   —    │   —    │   ✓    │   —    │
 * │ Action: EXCEEDED alert      │   —    │   —    │   —    │   ✓    │
 * └─────────────────────────────┴────────┴────────┴────────┴────────┘
 *
 * Key boundary: isExceeded() uses strictly greater (>), so 100% = WARNING, 101% = EXCEEDED
 */
@DisplayName("Stage 7 — Decision Table Tests")
class DecisionTableTest {

    @TempDir
    Path tempDir;

    private BudgetController budgetController;

    @BeforeEach
    void setUp() {
        String budgetsPath = tempDir.resolve("budgets.csv").toString();
        FileManager fileManager = new FileManager();
        BudgetDAO budgetDAO = new BudgetDAO(budgetsPath, fileManager);
        budgetController = new BudgetController(budgetDAO);
    }

    // =========================================================================
    // Rule 1: No budget set → No alert
    // =========================================================================

    @Test
    @DisplayName("DT-Rule1: No budget set for category produces no alert")
    void rule1_noBudgetSet_noAlert() throws IOException {
        // Condition: No budget set for FOOD
        // (Don't call setBudgetLimit)

        // Act: Check alerts
        List<String> alerts = budgetController.checkAllAlerts();

        // Assert: No alerts
        assertTrue(alerts.isEmpty(),
            "Rule 1: No budget set should produce no alerts");
    }

    // =========================================================================
    // Rule 2: Budget set, spending < 80% → No alert
    // =========================================================================

    @Test
    @DisplayName("DT-Rule2a: Spending at 0% produces no alert")
    void rule2_spendingAt0Percent_noAlert() throws IOException {
        // Condition: Budget=100, spending=0 (0%)
        budgetController.setBudgetLimit(Category.FOOD, 100.00);
        // Don't add any spending

        // Act
        List<String> alerts = budgetController.checkAllAlerts();

        // Assert: No alerts (0% < 80%)
        assertTrue(alerts.isEmpty(),
            "Rule 2: 0% spending should produce no alert");
    }

    @Test
    @DisplayName("DT-Rule2b: Spending at 79% produces no alert")
    void rule2_spendingAt79Percent_noAlert() throws IOException {
        // Condition: Budget=100, spending=79 (79%)
        budgetController.setBudgetLimit(Category.FOOD, 100.00);
        budgetController.updateSpending(Category.FOOD, 79.00);

        // Act
        List<String> alerts = budgetController.checkAllAlerts();

        // Assert: No alerts (79% < 80%)
        assertTrue(alerts.isEmpty(),
            "Rule 2: 79% spending should produce no alert");
    }

    // =========================================================================
    // Rule 3: Budget set, 80% ≤ spending ≤ 100% → WARNING alert
    // =========================================================================

    @Test
    @DisplayName("DT-Rule3a: Spending at exactly 80% produces WARNING alert")
    void rule3_spendingAtExactly80Percent_warningAlert() throws IOException {
        // Condition: Budget=100, spending=80 (exactly 80%)
        budgetController.setBudgetLimit(Category.FOOD, 100.00);
        budgetController.updateSpending(Category.FOOD, 80.00);

        // Act
        List<String> alerts = budgetController.checkAllAlerts();

        // Assert: WARNING alert
        assertEquals(1, alerts.size(), "Should have exactly 1 alert");
        assertTrue(alerts.get(0).contains("WARNING"),
            "Rule 3: 80% should produce WARNING (not EXCEEDED)");
        assertTrue(alerts.get(0).contains("Food"),
            "Alert should mention Food category");
    }

    @Test
    @DisplayName("DT-Rule3b: Spending at 99% produces WARNING alert")
    void rule3_spendingAt99Percent_warningAlert() throws IOException {
        // Condition: Budget=100, spending=99 (99%)
        budgetController.setBudgetLimit(Category.FOOD, 100.00);
        budgetController.updateSpending(Category.FOOD, 99.00);

        // Act
        List<String> alerts = budgetController.checkAllAlerts();

        // Assert: WARNING alert
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).contains("WARNING"),
            "Rule 3: 99% should produce WARNING");
    }

    @Test
    @DisplayName("DT-Rule3c: Spending at exactly 100% produces WARNING (not EXCEEDED)")
    void rule3_spendingAtExactly100Percent_warningNotExceeded() throws IOException {
        // Condition: Budget=100, spending=100 (exactly 100%)
        // Key: isExceeded() uses strictly greater (>), so 100% is NOT exceeded
        budgetController.setBudgetLimit(Category.FOOD, 100.00);
        budgetController.updateSpending(Category.FOOD, 100.00);

        // Act
        List<String> alerts = budgetController.checkAllAlerts();

        // Assert: WARNING (not EXCEEDED)
        assertEquals(1, alerts.size(), "Should have exactly 1 alert");
        assertTrue(alerts.get(0).contains("WARNING"),
            "Rule 3: 100% (exactly at limit) should be WARNING, not EXCEEDED");
        assertFalse(alerts.get(0).contains("EXCEEDED"),
            "100% should NOT be EXCEEDED (isExceeded uses strict >)");

        // Verify the state: should NOT be over budget at exactly 100%
        assertFalse(budgetController.isOverBudget(Category.FOOD),
            "isOverBudget should return false at exactly 100%");
    }

    // =========================================================================
    // Rule 4: Budget set, spending > 100% → EXCEEDED alert
    // =========================================================================

    @Test
    @DisplayName("DT-Rule4a: Spending at 101% produces EXCEEDED alert")
    void rule4_spendingOver100Percent_exceededAlert() throws IOException {
        // Condition: Budget=100, spending=101 (101%)
        budgetController.setBudgetLimit(Category.FOOD, 100.00);
        budgetController.updateSpending(Category.FOOD, 101.00);

        // Act
        List<String> alerts = budgetController.checkAllAlerts();

        // Assert: EXCEEDED alert
        assertEquals(1, alerts.size(), "Should have exactly 1 alert");
        assertTrue(alerts.get(0).contains("EXCEEDED"),
            "Rule 4: 101% should produce EXCEEDED alert");
        assertTrue(alerts.get(0).contains("Food"),
            "Alert should mention Food category");

        // Verify state
        assertTrue(budgetController.isOverBudget(Category.FOOD),
            "isOverBudget should return true at 101%");
    }

    @Test
    @DisplayName("DT-Rule4b: Spending at 150% produces EXCEEDED alert")
    void rule4_spendingAt150Percent_exceededAlert() throws IOException {
        // Condition: Budget=100, spending=150 (150%)
        budgetController.setBudgetLimit(Category.FOOD, 100.00);
        budgetController.updateSpending(Category.FOOD, 150.00);

        // Act
        List<String> alerts = budgetController.checkAllAlerts();

        // Assert: EXCEEDED alert with amount details
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).contains("EXCEEDED"),
            "Rule 4: 150% should produce EXCEEDED alert");
        assertTrue(alerts.get(0).contains("150"),
            "Alert should mention the spending amount 150");
    }

    // =========================================================================
    // Mixed: Multiple categories with different rules
    // =========================================================================

    @Test
    @DisplayName("DT-Mixed: Three categories trigger Rules 2, 3, and 4 simultaneously")
    void mixed_threeCategories_allRules() throws IOException {
        // Setup three budgets with different spending levels:
        // FOOD: 50% → Rule 2 (no alert)
        // TRANSPORT: 85% → Rule 3 (WARNING)
        // UTILITIES: 110% → Rule 4 (EXCEEDED)

        budgetController.setBudgetLimit(Category.FOOD, 100.00);
        budgetController.setBudgetLimit(Category.TRANSPORT, 100.00);
        budgetController.setBudgetLimit(Category.UTILITIES, 100.00);

        budgetController.updateSpending(Category.FOOD, 50.00);      // 50% - no alert
        budgetController.updateSpending(Category.TRANSPORT, 85.00); // 85% - WARNING
        budgetController.updateSpending(Category.UTILITIES, 110.00); // 110% - EXCEEDED

        // Act
        List<String> alerts = budgetController.checkAllAlerts();

        // Assert: 2 alerts (WARNING for Transport, EXCEEDED for Utilities)
        assertEquals(2, alerts.size(),
            "Should have 2 alerts: WARNING for Transport, EXCEEDED for Utilities");

        // Check that we have one WARNING and one EXCEEDED
        long warningCount = alerts.stream().filter(a -> a.contains("WARNING")).count();
        long exceededCount = alerts.stream().filter(a -> a.contains("EXCEEDED")).count();

        assertEquals(1, warningCount, "Should have exactly 1 WARNING alert");
        assertEquals(1, exceededCount, "Should have exactly 1 EXCEEDED alert");

        // Verify specific categories
        assertTrue(alerts.stream().anyMatch(a -> a.contains("Transport")),
            "One alert should mention Transport");
        assertTrue(alerts.stream().anyMatch(a -> a.contains("Utilities")),
            "One alert should mention Utilities");
        assertFalse(alerts.stream().anyMatch(a -> a.contains("Food")),
            "No alert should mention Food (only 50%)");
    }
}
