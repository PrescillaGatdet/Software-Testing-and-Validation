package com.budgetmanager.controller;

import com.budgetmanager.dao.BudgetDAO;
import com.budgetmanager.model.Budget;
import com.budgetmanager.model.Category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BudgetController.
 *
 * Strategy: BudgetDAO is mocked — no files are created.
 * Tests cover all decision-table rules for alert generation, all setter
 * validation paths, and all query methods.
 *
 * Decision Table rules verified:
 *   Rule 1 (no budget set)      → no alert in checkAllAlerts
 *   Rule 2 (spending ≤ 80 %)   → no alert
 *   Rule 3 (80–100 % spending)  → WARNING alert
 *   Rule 4 (spending > 100 %)   → EXCEEDED alert
 *
 * Tested methods: setBudgetLimit, updateSpending, isOverBudget,
 *                 getRemainingBudget, getBudget, getAllBudgets, checkAllAlerts
 */
@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {

    @Mock private BudgetDAO dao;

    private BudgetController controller;

    @BeforeEach
    void setUp() {
        controller = new BudgetController(dao);
    }

    // =========================================================================
    // setBudgetLimit
    // =========================================================================

    @Test
    void setBudgetLimit_createsNewBudgetWhenNoneExists() throws IOException {
        when(dao.findByCategory(Category.FOOD)).thenReturn(Optional.empty());

        controller.setBudgetLimit(Category.FOOD, 500.00);

        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
        verify(dao).save(captor.capture());
        Budget saved = captor.getValue();
        assertEquals(Category.FOOD, saved.getCategory());
        assertEquals(500.00,        saved.getLimit(), 0.001);
        assertEquals(0.00,          saved.getCurrentSpending(), 0.001);
    }

    @Test
    void setBudgetLimit_updatesExistingLimitPreservesSpending() throws IOException {
        Budget existing = new Budget(Category.FOOD, 300.00);
        existing.setCurrentSpending(120.00);
        when(dao.findByCategory(Category.FOOD)).thenReturn(Optional.of(existing));

        controller.setBudgetLimit(Category.FOOD, 600.00);

        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
        verify(dao).save(captor.capture());
        Budget saved = captor.getValue();
        assertEquals(600.00, saved.getLimit(),           0.001);
        assertEquals(120.00, saved.getCurrentSpending(), 0.001); // spending preserved
    }

    @Test
    void setBudgetLimit_nullCategory_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.setBudgetLimit(null, 100.00));
    }

    @Test
    void setBudgetLimit_zeroLimit_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.setBudgetLimit(Category.FOOD, 0.00));
    }

    @Test
    void setBudgetLimit_negativeLimit_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.setBudgetLimit(Category.FOOD, -50.00));
    }

    // =========================================================================
    // updateSpending
    // =========================================================================

    @Test
    void updateSpending_existingBudget_addsAmountToCurrentSpending() throws IOException {
        Budget budget = new Budget(Category.FOOD, 500.00);
        budget.setCurrentSpending(100.00);
        when(dao.findByCategory(Category.FOOD)).thenReturn(Optional.of(budget));

        controller.updateSpending(Category.FOOD, 75.00);

        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
        verify(dao).save(captor.capture());
        assertEquals(175.00, captor.getValue().getCurrentSpending(), 0.001);
    }

    @Test
    void updateSpending_noBudgetSet_doesNotCallSave() throws IOException {
        // Decision Table Rule 1: no budget → no effect
        when(dao.findByCategory(Category.TRANSPORT)).thenReturn(Optional.empty());

        controller.updateSpending(Category.TRANSPORT, 50.00);

        verify(dao, never()).save(any());
    }

    @Test
    void updateSpending_nullCategory_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.updateSpending(null, 50.00));
    }

    @Test
    void updateSpending_negativeAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.updateSpending(Category.FOOD, -10.00));
    }

    @Test
    void updateSpending_zeroAmount_isAllowed() throws IOException {
        // Adding 0 is harmless — no exception should be thrown
        Budget budget = new Budget(Category.FOOD, 500.00);
        when(dao.findByCategory(Category.FOOD)).thenReturn(Optional.of(budget));

        assertDoesNotThrow(() -> controller.updateSpending(Category.FOOD, 0.00));
    }

    // =========================================================================
    // isOverBudget
    // =========================================================================

    @Test
    void isOverBudget_returnsTrueWhenExceeded() throws IOException {
        Budget budget = new Budget(Category.FOOD, 100.00);
        budget.setCurrentSpending(150.00); // 150 > 100 → exceeded
        when(dao.findByCategory(Category.FOOD)).thenReturn(Optional.of(budget));

        assertTrue(controller.isOverBudget(Category.FOOD));
    }

    @Test
    void isOverBudget_returnsFalseWhenNotExceeded() throws IOException {
        Budget budget = new Budget(Category.FOOD, 100.00);
        budget.setCurrentSpending(80.00); // exactly at 80%
        when(dao.findByCategory(Category.FOOD)).thenReturn(Optional.of(budget));

        assertFalse(controller.isOverBudget(Category.FOOD));
    }

    @Test
    void isOverBudget_returnsFalseWhenNoBudgetSet() throws IOException {
        // Decision Table Rule 1 — no budget means no exceedance
        when(dao.findByCategory(Category.ENTERTAINMENT)).thenReturn(Optional.empty());

        assertFalse(controller.isOverBudget(Category.ENTERTAINMENT));
    }

    @Test
    void isOverBudget_nullCategory_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.isOverBudget(null));
    }

    // =========================================================================
    // getRemainingBudget
    // =========================================================================

    @Test
    void getRemainingBudget_returnsLimitMinusSpending() throws IOException {
        Budget budget = new Budget(Category.FOOD, 500.00);
        budget.setCurrentSpending(200.00);
        when(dao.findByCategory(Category.FOOD)).thenReturn(Optional.of(budget));

        assertEquals(300.00, controller.getRemainingBudget(Category.FOOD), 0.001);
    }

    @Test
    void getRemainingBudget_noBudget_returnsDoubleMaxValue() throws IOException {
        when(dao.findByCategory(Category.OTHER)).thenReturn(Optional.empty());

        assertEquals(Double.MAX_VALUE, controller.getRemainingBudget(Category.OTHER));
    }

    @Test
    void getRemainingBudget_nullCategory_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.getRemainingBudget(null));
    }

    // =========================================================================
    // getBudget
    // =========================================================================

    @Test
    void getBudget_returnsPresentOptionalWhenSet() throws IOException {
        Budget budget = new Budget(Category.FOOD, 400.00);
        when(dao.findByCategory(Category.FOOD)).thenReturn(Optional.of(budget));

        Optional<Budget> result = controller.getBudget(Category.FOOD);
        assertTrue(result.isPresent());
        assertEquals(400.00, result.get().getLimit(), 0.001);
    }

    @Test
    void getBudget_returnsEmptyOptionalWhenNotSet() throws IOException {
        when(dao.findByCategory(Category.SALARY)).thenReturn(Optional.empty());

        assertTrue(controller.getBudget(Category.SALARY).isEmpty());
    }

    @Test
    void getBudget_nullCategory_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.getBudget(null));
    }

    // =========================================================================
    // getAllBudgets
    // =========================================================================

    @Test
    void getAllBudgets_delegatesToDao() throws IOException {
        Budget b1 = new Budget(Category.FOOD,  300.00);
        Budget b2 = new Budget(Category.OTHER, 100.00);
        when(dao.loadAll()).thenReturn(Arrays.asList(b1, b2));

        List<Budget> result = controller.getAllBudgets();
        assertEquals(2, result.size());
        verify(dao, times(1)).loadAll();
    }

    // =========================================================================
    // checkAllAlerts — Decision Table coverage
    // =========================================================================

    @Test
    void checkAllAlerts_noAlerts_whenNoBudgetsSet() throws IOException {
        // Rule 1: no budget entries → empty alert list
        when(dao.loadAll()).thenReturn(Collections.emptyList());

        assertTrue(controller.checkAllAlerts().isEmpty());
    }

    @Test
    void checkAllAlerts_noAlert_whenSpendingBelow80Percent() throws IOException {
        // Rule 2: 79.9% usage → no alert
        Budget budget = new Budget(Category.FOOD, 1000.00);
        budget.setCurrentSpending(799.00); // 79.9%
        when(dao.loadAll()).thenReturn(Collections.singletonList(budget));

        assertTrue(controller.checkAllAlerts().isEmpty());
    }

    @Test
    void checkAllAlerts_warningAlert_whenSpendingAt80Percent() throws IOException {
        // Rule 3: exactly 80% → WARNING
        Budget budget = new Budget(Category.FOOD, 1000.00);
        budget.setCurrentSpending(800.00); // exactly 80%
        when(dao.loadAll()).thenReturn(Collections.singletonList(budget));

        List<String> alerts = controller.checkAllAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).startsWith("WARNING:"));
        assertTrue(alerts.get(0).contains("Food"));
    }

    @Test
    void checkAllAlerts_warningAlert_whenSpendingAt99Percent() throws IOException {
        // Rule 3: 99% → still WARNING (not yet exceeded)
        Budget budget = new Budget(Category.FOOD, 1000.00);
        budget.setCurrentSpending(990.00);
        when(dao.loadAll()).thenReturn(Collections.singletonList(budget));

        List<String> alerts = controller.checkAllAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).startsWith("WARNING:"));
    }

    @Test
    void checkAllAlerts_exceededAlert_whenSpendingOver100Percent() throws IOException {
        // Rule 4: 150% → EXCEEDED
        Budget budget = new Budget(Category.FOOD, 100.00);
        budget.setCurrentSpending(150.00);
        when(dao.loadAll()).thenReturn(Collections.singletonList(budget));

        List<String> alerts = controller.checkAllAlerts();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).startsWith("EXCEEDED:"));
        assertTrue(alerts.get(0).contains("Food"));
    }

    @Test
    void checkAllAlerts_mixedAlerts_multipleBudgets() throws IOException {
        Budget fine     = new Budget(Category.TRANSPORT,    1000.00); // 50% → no alert
        Budget warning  = new Budget(Category.FOOD,          100.00);
        Budget exceeded = new Budget(Category.ENTERTAINMENT, 200.00);

        fine.setCurrentSpending(500.00);     // 50% — Rule 2
        warning.setCurrentSpending(85.00);   // 85% — Rule 3
        exceeded.setCurrentSpending(250.00); // 125% — Rule 4

        when(dao.loadAll()).thenReturn(Arrays.asList(fine, warning, exceeded));

        List<String> alerts = controller.checkAllAlerts();
        assertEquals(2, alerts.size()); // WARNING + EXCEEDED only
        assertTrue(alerts.stream().anyMatch(a -> a.startsWith("WARNING:")));
        assertTrue(alerts.stream().anyMatch(a -> a.startsWith("EXCEEDED:")));
    }
}
