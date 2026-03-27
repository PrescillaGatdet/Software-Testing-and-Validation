package com.budgetmanager.dao;

import com.budgetmanager.model.Category;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionDAO.
 *
 * Uses a real FileManager and a temporary file (not a mock) so that the
 * CSV serialisation round-trip is verified end-to-end.
 *
 * Covers:
 *   - save → loadAll round-trip (single and multiple transactions)
 *   - Header is NOT returned as a transaction
 *   - deleteById removes the correct entry and leaves others intact
 *   - findByCategory filters correctly
 *   - findById returns present/empty Optional
 *   - Description containing a comma is preserved across save/load
 *   - loadAll on missing file returns empty list (no IOException)
 */
@DisplayName("TransactionDAO — Unit Tests")
class TransactionDAOTest {

    private TransactionDAO dao;
    private Path tempDir;
    private String csvPath;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("dao-tx-test-");
        csvPath = tempDir.resolve("transactions.csv").toString();
        dao     = new TransactionDAO(csvPath, new FileManager());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
             .sorted((a, b) -> b.compareTo(a))
             .forEach(p -> p.toFile().delete());
    }

    // -------------------------------------------------------------------------
    // Helper: create a deterministic Transaction for tests
    // -------------------------------------------------------------------------
    private Transaction makeTransaction(String id, double amount,
                                        Category cat, TransactionType type) {
        return new Transaction(
            id, amount, LocalDate.of(2026, 3, 1), "Test description", cat, type
        );
    }

    // =========================================================================
    // loadAll on empty / missing file
    // =========================================================================

    @Test
    @DisplayName("loadAll returns empty list when CSV file does not exist")
    void testLoadAllMissingFile() throws IOException {
        List<Transaction> result = dao.loadAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // save → loadAll round-trip
    // =========================================================================

    @Test
    @DisplayName("Save and reload a single transaction — all fields match")
    void testSaveAndLoadSingleTransaction() throws IOException {
        Transaction original = makeTransaction("t001", 150.00, Category.FOOD, TransactionType.EXPENSE);
        dao.save(original);

        List<Transaction> loaded = dao.loadAll();
        assertEquals(1, loaded.size());

        Transaction t = loaded.get(0);
        assertEquals("t001",                   t.getId());
        assertEquals(150.00,                   t.getAmount(),   0.001);
        assertEquals(LocalDate.of(2026, 3, 1), t.getDate());
        assertEquals("Test description",       t.getDescription());
        assertEquals(Category.FOOD,            t.getCategory());
        assertEquals(TransactionType.EXPENSE,  t.getType());
    }

    @Test
    @DisplayName("Save three transactions — all three are loaded")
    void testSaveMultipleTransactions() throws IOException {
        dao.save(makeTransaction("t001", 100.0,  Category.FOOD,      TransactionType.EXPENSE));
        dao.save(makeTransaction("t002", 2500.0, Category.SALARY,    TransactionType.INCOME));
        dao.save(makeTransaction("t003", 50.0,   Category.TRANSPORT, TransactionType.EXPENSE));

        assertEquals(3, dao.loadAll().size());
    }

    @Test
    @DisplayName("Header row is NOT included in loadAll results")
    void testHeaderNotLoadedAsTransaction() throws IOException {
        dao.save(makeTransaction("t001", 100.0, Category.FOOD, TransactionType.EXPENSE));
        // File now has: header line + 1 data line = 2 lines total
        // loadAll should return exactly 1 transaction
        assertEquals(1, dao.loadAll().size());
    }

    // =========================================================================
    // deleteById
    // =========================================================================

    @Test
    @DisplayName("deleteById removes the correct transaction")
    void testDeleteByIdRemovesCorrectEntry() throws IOException {
        dao.save(makeTransaction("t001", 100.0, Category.FOOD,   TransactionType.EXPENSE));
        dao.save(makeTransaction("t002", 200.0, Category.SALARY, TransactionType.INCOME));

        dao.deleteById("t001");

        List<Transaction> remaining = dao.loadAll();
        assertEquals(1, remaining.size());
        assertEquals("t002", remaining.get(0).getId());
    }

    @Test
    @DisplayName("deleteById with unknown ID leaves all transactions intact")
    void testDeleteByIdUnknownIdNoEffect() throws IOException {
        dao.save(makeTransaction("t001", 100.0, Category.FOOD, TransactionType.EXPENSE));
        dao.deleteById("does-not-exist");
        assertEquals(1, dao.loadAll().size());
    }

    @Test
    @DisplayName("deleteById on the only transaction results in an empty list")
    void testDeleteByIdLastTransaction() throws IOException {
        dao.save(makeTransaction("t001", 100.0, Category.FOOD, TransactionType.EXPENSE));
        dao.deleteById("t001");
        assertTrue(dao.loadAll().isEmpty());
    }

    // =========================================================================
    // findByCategory
    // =========================================================================

    @Test
    @DisplayName("findByCategory returns only transactions in that category")
    void testFindByCategoryFiltersCorrectly() throws IOException {
        dao.save(makeTransaction("t001", 100.0,  Category.FOOD,      TransactionType.EXPENSE));
        dao.save(makeTransaction("t002", 2500.0, Category.SALARY,    TransactionType.INCOME));
        dao.save(makeTransaction("t003", 50.0,   Category.FOOD,      TransactionType.EXPENSE));

        List<Transaction> food = dao.findByCategory(Category.FOOD);
        assertEquals(2, food.size());
        food.forEach(t -> assertEquals(Category.FOOD, t.getCategory()));
    }

    @Test
    @DisplayName("findByCategory returns empty list when no transactions match")
    void testFindByCategoryNoMatch() throws IOException {
        dao.save(makeTransaction("t001", 100.0, Category.FOOD, TransactionType.EXPENSE));
        List<Transaction> result = dao.findByCategory(Category.ENTERTAINMENT);
        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // findById
    // =========================================================================

    @Test
    @DisplayName("findById returns the matching transaction")
    void testFindByIdFound() throws IOException {
        Transaction t = makeTransaction("t001", 100.0, Category.FOOD, TransactionType.EXPENSE);
        dao.save(t);
        Optional<Transaction> found = dao.findById("t001");
        assertTrue(found.isPresent());
        assertEquals("t001", found.get().getId());
    }

    @Test
    @DisplayName("findById returns empty Optional for an unknown ID")
    void testFindByIdNotFound() throws IOException {
        Optional<Transaction> found = dao.findById("ghost-id");
        assertFalse(found.isPresent());
    }

    // =========================================================================
    // Edge cases
    // =========================================================================

    @Test
    @DisplayName("Description containing a comma is preserved after save/load")
    void testDescriptionWithCommaRoundTrip() throws IOException {
        Transaction t = new Transaction(
            "t001", 100.0, LocalDate.of(2026, 3, 1),
            "Grocery, bread and milk", Category.FOOD, TransactionType.EXPENSE
        );
        dao.save(t);
        List<Transaction> loaded = dao.loadAll();
        assertEquals(1, loaded.size());
        assertEquals("Grocery, bread and milk", loaded.get(0).getDescription());
    }

    @Test
    @DisplayName("INCOME type survives a save/load round-trip")
    void testIncomeTypeRoundTrip() throws IOException {
        dao.save(makeTransaction("t001", 3000.0, Category.SALARY, TransactionType.INCOME));
        Transaction loaded = dao.loadAll().get(0);
        assertEquals(TransactionType.INCOME, loaded.getType());
        assertEquals(Category.SALARY,        loaded.getCategory());
    }
}
