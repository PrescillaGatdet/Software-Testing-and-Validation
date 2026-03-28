package com.budgetmanager.dao;

import com.budgetmanager.model.Budget;
import com.budgetmanager.model.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BudgetDAO — Unit Tests")
class BudgetDAOTest {

    private BudgetDAO dao;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("dao-budget-test-");
        String csvPath = tempDir.resolve("budgets.csv").toString();
        dao = new BudgetDAO(csvPath, new FileManager());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
             .sorted((a, b) -> b.compareTo(a))
             .forEach(p -> p.toFile().delete());
    }

    // =========================================================================
    // loadAll on empty / missing file
    // =========================================================================

    @Test
    @DisplayName("loadAll returns empty list when CSV file does not exist")
    void testLoadAllMissingFile() throws IOException {
        List<Budget> result = dao.loadAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // save → loadAll round-trip
    // =========================================================================

    @Test
    @DisplayName("Save and reload a single budget — all fields match")
    void testSaveAndLoadSingleBudget() throws IOException {
        Budget original = new Budget(Category.FOOD, 500.0);
        original.setCurrentSpending(200.0);
        dao.save(original);

        List<Budget> loaded = dao.loadAll();
        assertEquals(1, loaded.size());

        Budget b = loaded.get(0);
        assertEquals(Category.FOOD, b.getCategory());
        assertEquals(500.0,         b.getLimit(),           0.001);
        assertEquals(200.0,         b.getCurrentSpending(), 0.001);
    }

    @Test
    @DisplayName("Header row is NOT returned as a Budget entry")
    void testHeaderNotReturnedAsBudget() throws IOException {
        dao.save(new Budget(Category.FOOD, 500.0));
        assertEquals(1, dao.loadAll().size());
    }

    @Test
    @DisplayName("Save and reload with zero current spending")
    void testSaveWithZeroSpending() throws IOException {
        dao.save(new Budget(Category.TRANSPORT, 300.0));
        Budget loaded = dao.loadAll().get(0);
        assertEquals(0.0, loaded.getCurrentSpending(), 0.001);
    }

    // =========================================================================
    // Upsert behaviour (save replaces existing same-category entry)
    // =========================================================================

    @Test
    @DisplayName("Saving same category twice replaces — does not duplicate")
    void testSaveReplacesSameCategory() throws IOException {
        dao.save(new Budget(Category.FOOD, 500.0));

        Budget updated = new Budget(Category.FOOD, 800.0);
        updated.setCurrentSpending(350.0);
        dao.save(updated);

        List<Budget> all = dao.loadAll();
        assertEquals(1, all.size(), "Only one entry for FOOD should exist");
        assertEquals(800.0, all.get(0).getLimit(),           0.001);
        assertEquals(350.0, all.get(0).getCurrentSpending(), 0.001);
    }

    @Test
    @DisplayName("Saving different categories adds both independently")
    void testSaveDifferentCategories() throws IOException {
        dao.save(new Budget(Category.FOOD,      500.0));
        dao.save(new Budget(Category.TRANSPORT, 200.0));
        dao.save(new Budget(Category.UTILITIES, 150.0));

        assertEquals(3, dao.loadAll().size());
    }

    // =========================================================================
    // findByCategory
    // =========================================================================

    @Test
    @DisplayName("findByCategory returns the matching budget")
    void testFindByCategoryFound() throws IOException {
        dao.save(new Budget(Category.FOOD,      500.0));
        dao.save(new Budget(Category.TRANSPORT, 200.0));

        Optional<Budget> found = dao.findByCategory(Category.FOOD);
        assertTrue(found.isPresent());
        assertEquals(500.0, found.get().getLimit(), 0.001);
    }

    @Test
    @DisplayName("findByCategory returns empty Optional when category has no budget")
    void testFindByCategoryNotFound() throws IOException {
        dao.save(new Budget(Category.FOOD, 500.0));
        Optional<Budget> found = dao.findByCategory(Category.ENTERTAINMENT);
        assertFalse(found.isPresent());
    }

    // =========================================================================
    // deleteByCategory
    // =========================================================================

    @Test
    @DisplayName("deleteByCategory removes the correct entry and keeps others")
    void testDeleteByCategoryRemovesCorrectEntry() throws IOException {
        dao.save(new Budget(Category.FOOD,      500.0));
        dao.save(new Budget(Category.TRANSPORT, 200.0));

        dao.deleteByCategory(Category.FOOD);

        List<Budget> remaining = dao.loadAll();
        assertEquals(1, remaining.size());
        assertEquals(Category.TRANSPORT, remaining.get(0).getCategory());
    }

    @Test
    @DisplayName("deleteByCategory with unknown category does not change the file")
    void testDeleteByCategoryUnknownNoEffect() throws IOException {
        dao.save(new Budget(Category.FOOD, 500.0));
        dao.deleteByCategory(Category.ENTERTAINMENT);
        assertEquals(1, dao.loadAll().size());
    }

    @Test
    @DisplayName("deleteByCategory on the only entry results in an empty list")
    void testDeleteLastBudgetResultsInEmptyList() throws IOException {
        dao.save(new Budget(Category.FOOD, 500.0));
        dao.deleteByCategory(Category.FOOD);
        assertTrue(dao.loadAll().isEmpty());
    }

    // =========================================================================
    // Computed properties survive round-trip
    // =========================================================================

    @Test
    @DisplayName("isExceeded flag is correct after save/load round-trip")
    void testIsExceededAfterRoundTrip() throws IOException {
        Budget b = new Budget(Category.ENTERTAINMENT, 100.0);
        b.setCurrentSpending(150.0);
        dao.save(b);

        Budget loaded = dao.loadAll().get(0);
        assertTrue(loaded.isExceeded());
        assertEquals(-50.0, loaded.getRemainingBudget(), 0.001);
    }
}
