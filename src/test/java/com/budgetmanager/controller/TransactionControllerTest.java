package com.budgetmanager.controller;

import com.budgetmanager.dao.FileManager;
import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Category;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock private TransactionDAO dao;
    @Mock private FileManager    fileManager;

    private TransactionController controller;

    /** Fixed reference date used across tests. */
    private static final LocalDate DATE = LocalDate.of(2026, 3, 1);

    @BeforeEach
    void setUp() {
        controller = new TransactionController(dao, fileManager);
    }

    // =========================================================================
    // addTransaction
    // =========================================================================

    @Test
    void addTransaction_savesTransactionAndReturnsIt() throws IOException {
        Transaction result = controller.addTransaction(
            50.00, DATE, "Groceries", Category.FOOD, TransactionType.EXPENSE);

        assertNotNull(result);
        assertEquals(50.00,              result.getAmount());
        assertEquals(DATE,               result.getDate());
        assertEquals("Groceries",        result.getDescription());
        assertEquals(Category.FOOD,      result.getCategory());
        assertEquals(TransactionType.EXPENSE, result.getType());

        // DAO save must have been called exactly once with the same Transaction
        verify(dao, times(1)).save(result);
    }

    @Test
    void addTransaction_propagatesIOException() throws IOException {
        doThrow(new IOException("disk full")).when(dao).save(any(Transaction.class));

        assertThrows(IOException.class, () ->
            controller.addTransaction(10.00, DATE, "Lunch", Category.FOOD, TransactionType.EXPENSE));
    }

    @Test
    void addTransaction_delegatesValidationToModel_invalidAmount() {
        // amount 0.00 is below Transaction.MIN_AMOUNT — model throws
        assertThrows(IllegalArgumentException.class, () ->
            controller.addTransaction(0.00, DATE, "Zero", Category.FOOD, TransactionType.EXPENSE));
    }

    // =========================================================================
    // removeTransaction
    // =========================================================================

    @Test
    void removeTransaction_delegatesToDaoDeleteById() throws IOException {
        controller.removeTransaction("txn-001");
        verify(dao, times(1)).deleteById("txn-001");
    }

    @Test
    void removeTransaction_trimsIdBeforeDeleting() throws IOException {
        controller.removeTransaction("  txn-001  ");
        verify(dao, times(1)).deleteById("txn-001");
    }

    @Test
    void removeTransaction_nullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.removeTransaction(null));
    }

    @Test
    void removeTransaction_emptyId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.removeTransaction("   "));
    }

    // =========================================================================
    // getAll
    // =========================================================================

    @Test
    void getAll_returnsAllTransactionsFromDao() throws IOException {
        Transaction t1 = makeTransaction("t1", 100.00, DATE, Category.FOOD);
        Transaction t2 = makeTransaction("t2", 200.00, DATE, Category.SALARY);
        when(dao.loadAll()).thenReturn(Arrays.asList(t1, t2));

        List<Transaction> result = controller.getAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));
    }

    @Test
    void getAll_returnsEmptyListWhenNoTransactions() throws IOException {
        when(dao.loadAll()).thenReturn(Collections.emptyList());

        assertTrue(controller.getAll().isEmpty());
    }

    // =========================================================================
    // filterByCategory
    // =========================================================================

    @Test
    void filterByCategory_delegatesToDao() throws IOException {
        Transaction t = makeTransaction("t1", 50.00, DATE, Category.FOOD);
        when(dao.findByCategory(Category.FOOD)).thenReturn(Collections.singletonList(t));

        List<Transaction> result = controller.filterByCategory(Category.FOOD);

        assertEquals(1, result.size());
        assertEquals(Category.FOOD, result.get(0).getCategory());
        verify(dao, times(1)).findByCategory(Category.FOOD);
    }

    @Test
    void filterByCategory_nullCategory_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.filterByCategory(null));
    }

    // =========================================================================
    // filterByDateRange
    // =========================================================================

    @Test
    void filterByDateRange_returnsTransactionsInRangeInclusive() throws IOException {
        LocalDate jan1  = LocalDate.of(2026, 1, 1);
        LocalDate feb15 = LocalDate.of(2026, 2, 15);
        LocalDate mar31 = LocalDate.of(2026, 3, 31);

        Transaction inside = makeTransactionOnDate("t1", jan1);
        Transaction edge   = makeTransactionOnDate("t2", mar31);
        Transaction beyond = makeTransactionOnDate("t3", LocalDate.of(2026, 4, 1));
        when(dao.loadAll()).thenReturn(Arrays.asList(inside, edge, beyond));

        List<Transaction> result = controller.filterByDateRange(jan1, mar31);

        assertEquals(2, result.size());
        assertTrue(result.contains(inside));
        assertTrue(result.contains(edge));
        assertFalse(result.contains(beyond));
        // Verify the middle date works too
        Transaction mid = makeTransactionOnDate("t4", feb15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(mid));
        List<Transaction> midResult = controller.filterByDateRange(jan1, mar31);
        assertEquals(1, midResult.size());
    }

    @Test
    void filterByDateRange_startAfterEnd_throwsIllegalArgument() {
        LocalDate from = LocalDate.of(2026, 3, 31);
        LocalDate to   = LocalDate.of(2026, 1, 1);
        assertThrows(IllegalArgumentException.class, () ->
            controller.filterByDateRange(from, to));
    }

    @Test
    void filterByDateRange_nullFrom_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.filterByDateRange(null, DATE));
    }

    @Test
    void filterByDateRange_nullTo_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.filterByDateRange(DATE, null));
    }

    @Test
    void filterByDateRange_sameDayRange_returnsTransactionOnThatDay() throws IOException {
        Transaction t = makeTransactionOnDate("t1", DATE);
        when(dao.loadAll()).thenReturn(Collections.singletonList(t));

        List<Transaction> result = controller.filterByDateRange(DATE, DATE);
        assertEquals(1, result.size());
    }

    // =========================================================================
    // searchByDescription
    // =========================================================================

    @Test
    void searchByDescription_caseInsensitiveMatch() throws IOException {
        Transaction t1 = makeTransactionWithDesc("t1", "Grocery shopping");
        Transaction t2 = makeTransactionWithDesc("t2", "Monthly salary");
        Transaction t3 = makeTransactionWithDesc("t3", "GROCERY store");
        when(dao.loadAll()).thenReturn(Arrays.asList(t1, t2, t3));

        List<Transaction> result = controller.searchByDescription("grocery");

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t3));
        assertFalse(result.contains(t2));
    }

    @Test
    void searchByDescription_noMatch_returnsEmptyList() throws IOException {
        when(dao.loadAll()).thenReturn(Collections.singletonList(
            makeTransactionWithDesc("t1", "Salary payment")));

        List<Transaction> result = controller.searchByDescription("xyz123");
        assertTrue(result.isEmpty());
    }

    @Test
    void searchByDescription_nullKeyword_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.searchByDescription(null));
    }

    @Test
    void searchByDescription_emptyKeyword_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.searchByDescription("   "));
    }

    // =========================================================================
    // exportToCSV
    // =========================================================================

    @Test
    void exportToCSV_writesHeaderAndTransactionRows() throws IOException {
        Transaction t = makeTransaction("t1", 99.99, DATE, Category.FOOD);
        when(dao.loadAll()).thenReturn(Collections.singletonList(t));

        controller.exportToCSV("export/transactions.csv");

        // Capture the lines written to FileManager
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(fileManager).writeLines(eq("export/transactions.csv"), captor.capture());

        List<String> written = captor.getValue();
        assertEquals("id,amount,date,description,category,type", written.get(0));
        assertTrue(written.get(1).startsWith("t1,99.99,2026-03-01"));
        assertTrue(written.get(1).contains("FOOD"));
        assertTrue(written.get(1).contains("EXPENSE"));
    }

    @Test
    void exportToCSV_emptyTransactions_writesOnlyHeader() throws IOException {
        when(dao.loadAll()).thenReturn(Collections.emptyList());

        controller.exportToCSV("out.csv");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(fileManager).writeLines(anyString(), captor.capture());
        assertEquals(1, captor.getValue().size()); // only header
    }

    @Test
    void exportToCSV_nullPath_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.exportToCSV(null));
    }

    @Test
    void exportToCSV_emptyPath_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            controller.exportToCSV("  "));
    }

    // =========================================================================
    // Test helpers
    // =========================================================================

    private Transaction makeTransaction(String id, double amount, LocalDate date, Category cat) {
        return new Transaction(id, amount, date, "Test description", cat, TransactionType.EXPENSE);
    }

    private Transaction makeTransactionOnDate(String id, LocalDate date) {
        return new Transaction(id, 10.00, date, "Test desc", Category.FOOD, TransactionType.EXPENSE);
    }

    private Transaction makeTransactionWithDesc(String id, String description) {
        return new Transaction(id, 10.00, DATE, description, Category.FOOD, TransactionType.EXPENSE);
    }
}
