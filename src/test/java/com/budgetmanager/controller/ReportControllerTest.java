package com.budgetmanager.controller;

import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Category;
import com.budgetmanager.model.Report;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock private TransactionDAO txnDAO;

    private ReportController controller;

    @BeforeEach
    void setUp() {
        controller = new ReportController(txnDAO);
    }

    // =========================================================================
    // generateMonthlyReport
    // =========================================================================

    @Test
    void generateMonthlyReport_correctlyTotalsIncomeAndExpense() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Arrays.asList(
            income(2500.00, LocalDate.of(2026, 3, 1)),
            expense(150.00, LocalDate.of(2026, 3, 15)),
            expense(200.00, LocalDate.of(2026, 3, 20))
        ));

        Report report = controller.generateMonthlyReport(2026, 3);

        assertEquals(2500.00, report.getTotalIncome(),  0.001);
        assertEquals(350.00,  report.getTotalExpense(), 0.001);
        assertEquals(2150.00, report.getBalance(),      0.001);
    }

    @Test
    void generateMonthlyReport_ignoresTransactionsOutsideMonth() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Arrays.asList(
            expense(100.00, LocalDate.of(2026, 2, 28)), // February — must be excluded
            expense(50.00,  LocalDate.of(2026, 3, 1)),  // March — included
            expense(75.00,  LocalDate.of(2026, 4, 1))   // April — must be excluded
        ));

        Report report = controller.generateMonthlyReport(2026, 3);

        assertEquals(0.00,  report.getTotalIncome(),  0.001);
        assertEquals(50.00, report.getTotalExpense(), 0.001);
    }

    @Test
    void generateMonthlyReport_noTransactionsInPeriod_returnsZeroReport() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Collections.emptyList());

        Report report = controller.generateMonthlyReport(2026, 6);

        assertEquals(0.0, report.getTotalIncome(),  0.001);
        assertEquals(0.0, report.getTotalExpense(), 0.001);
    }

    @Test
    void generateMonthlyReport_correctPeriodLabel() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Collections.emptyList());

        Report report = controller.generateMonthlyReport(2026, 3);
        assertEquals("March 2026", report.getPeriod());
    }

    @Test
    void generateMonthlyReport_decemberPeriodLabel() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Collections.emptyList());

        Report report = controller.generateMonthlyReport(2025, 12);
        assertEquals("December 2025", report.getPeriod());
    }

    @Test
    void generateMonthlyReport_invalidMonth_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.generateMonthlyReport(2026, 0));
        assertThrows(IllegalArgumentException.class, () ->
            controller.generateMonthlyReport(2026, 13));
    }

    // =========================================================================
    // generateYearlyReport
    // =========================================================================

    @Test
    void generateYearlyReport_correctlyAggregatesAllMonths() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Arrays.asList(
            income(1000.00, LocalDate.of(2026, 1, 1)),
            income(2000.00, LocalDate.of(2026, 6, 15)),
            expense(500.00, LocalDate.of(2026, 3, 10)),
            expense(300.00, LocalDate.of(2026, 11, 20))
        ));

        Report report = controller.generateYearlyReport(2026);

        assertEquals(3000.00, report.getTotalIncome(),  0.001);
        assertEquals(800.00,  report.getTotalExpense(), 0.001);
        assertEquals(2200.00, report.getBalance(),      0.001);
    }

    @Test
    void generateYearlyReport_ignoresOtherYears() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Arrays.asList(
            income(5000.00, LocalDate.of(2025, 12, 31)), // 2025 — excluded
            income(3000.00, LocalDate.of(2026, 1, 1)),   // 2026 — included
            income(1000.00, LocalDate.of(2027, 1, 1))    // 2027 — excluded
        ));

        Report report = controller.generateYearlyReport(2026);

        assertEquals(3000.00, report.getTotalIncome(),  0.001);
        assertEquals(0.0,     report.getTotalExpense(), 0.001);
    }

    @Test
    void generateYearlyReport_noTransactionsForYear_returnsZeroReport() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Collections.emptyList());

        Report report = controller.generateYearlyReport(2030);

        assertEquals(0.0, report.getTotalIncome(),  0.001);
        assertEquals(0.0, report.getTotalExpense(), 0.001);
    }

    @Test
    void generateYearlyReport_periodLabelIsYearAsString() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Collections.emptyList());

        Report report = controller.generateYearlyReport(2026);
        assertEquals("2026", report.getPeriod());
    }

    // =========================================================================
    // generateCategoryBreakdown
    // =========================================================================

    @Test
    void generateCategoryBreakdown_sumsExpensesPerCategory() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Arrays.asList(
            expenseInCategory(100.00, Category.FOOD),
            expenseInCategory(50.00,  Category.FOOD),
            expenseInCategory(200.00, Category.TRANSPORT)
        ));

        Map<Category, Double> breakdown = controller.generateCategoryBreakdown();

        assertEquals(150.00, breakdown.get(Category.FOOD),      0.001);
        assertEquals(200.00, breakdown.get(Category.TRANSPORT),  0.001);
        assertNull(breakdown.get(Category.ENTERTAINMENT)); // not present
    }

    @Test
    void generateCategoryBreakdown_excludesIncomeTransactions() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Arrays.asList(
            income(3000.00,    LocalDate.of(2026, 3, 1)),         // INCOME — must be excluded
            expenseInCategory(100.00, Category.FOOD)
        ));

        Map<Category, Double> breakdown = controller.generateCategoryBreakdown();

        assertEquals(1, breakdown.size()); // only FOOD
        assertEquals(100.00, breakdown.get(Category.FOOD), 0.001);
    }

    @Test
    void generateCategoryBreakdown_emptyWhenNoTransactions() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Collections.emptyList());

        assertTrue(controller.generateCategoryBreakdown().isEmpty());
    }

    @Test
    void generateCategoryBreakdown_multipleCategories_allPresent() throws IOException {
        when(txnDAO.loadAll()).thenReturn(Arrays.asList(
            expenseInCategory(50.00,  Category.FOOD),
            expenseInCategory(30.00,  Category.TRANSPORT),
            expenseInCategory(20.00,  Category.ENTERTAINMENT),
            expenseInCategory(10.00,  Category.UTILITIES)
        ));

        Map<Category, Double> breakdown = controller.generateCategoryBreakdown();

        assertEquals(4, breakdown.size());
        assertEquals(50.00, breakdown.get(Category.FOOD),          0.001);
        assertEquals(30.00, breakdown.get(Category.TRANSPORT),      0.001);
        assertEquals(20.00, breakdown.get(Category.ENTERTAINMENT),  0.001);
        assertEquals(10.00, breakdown.get(Category.UTILITIES),      0.001);
    }

    // =========================================================================
    // Test helpers
    // =========================================================================

    private Transaction income(double amount, LocalDate date) {
        return new Transaction(amount, date, "Income source", Category.SALARY, TransactionType.INCOME);
    }

    private Transaction expense(double amount, LocalDate date) {
        return new Transaction(amount, date, "Expense item", Category.FOOD, TransactionType.EXPENSE);
    }

    private Transaction expenseInCategory(double amount, Category category) {
        return new Transaction(amount, LocalDate.of(2026, 3, 1),
            "Test expense", category, TransactionType.EXPENSE);
    }
}
