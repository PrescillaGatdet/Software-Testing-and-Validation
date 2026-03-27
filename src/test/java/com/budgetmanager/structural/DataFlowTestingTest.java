package com.budgetmanager.structural;

import com.budgetmanager.controller.TransactionController;
import com.budgetmanager.dao.TransactionDAO;
import com.budgetmanager.model.Category;
import com.budgetmanager.model.Transaction;
import com.budgetmanager.model.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Data Flow Testing for TransactionController.filterByDateRange(LocalDate from, LocalDate to)
 *
 * This test class implements structural testing using data flow analysis.
 * Data flow testing focuses on the definition-use (def-use) pairs of variables,
 * ensuring that every definition of a variable is tested with all its uses.
 *
 * ============================================================================
 * TARGET METHOD SOURCE CODE (for reference)
 * ============================================================================
 *
 * public List<Transaction> filterByDateRange(LocalDate from, LocalDate to) throws IOException {
 *     if (from == null || to == null) {                           // Line 138
 *         throw new IllegalArgumentException("...");              // Line 139
 *     }
 *     if (from.isAfter(to)) {                                     // Line 141
 *         throw new IllegalArgumentException("...");              // Line 142-143
 *     }
 *     List<Transaction> result = new ArrayList<>();               // Line 145
 *     for (Transaction t : dao.loadAll()) {                       // Line 146
 *         LocalDate d = t.getDate();                              // Line 147
 *         if (!d.isBefore(from) && !d.isAfter(to)) {              // Line 148
 *             result.add(t);                                      // Line 149
 *         }
 *     }
 *     return result;                                              // Line 152
 * }
 *
 * ============================================================================
 * VARIABLE DEFINITIONS AND USES (DEF-USE PAIRS)
 * ============================================================================
 *
 * | Variable | Defined at           | Used at                     | Pair ID |
 * |----------|----------------------|-----------------------------|---------|
 * | from     | Parameter entry      | null check (line 138)       | DU1     |
 * | from     | Parameter entry      | from.isAfter(to) (line 141) | DU2     |
 * | from     | Parameter entry      | !d.isBefore(from) (line 148)| DU3     |
 * | to       | Parameter entry      | null check (line 138)       | DU4     |
 * | to       | Parameter entry      | from.isAfter(to) (line 141) | DU5     |
 * | to       | Parameter entry      | !d.isAfter(to) (line 148)   | DU6     |
 * | result   | Line 145 (new AL)    | result.add(t) (line 149)    | DU7     |
 * | result   | Line 145 (new AL)    | return result (line 152)    | DU8     |
 * | t        | Line 146 (for-each)  | t.getDate() (line 147)      | DU9     |
 * | t        | Line 146 (for-each)  | result.add(t) (line 149)    | DU10    |
 * | d        | Line 147 (getDate()) | !d.isBefore(from) (line 148)| DU11    |
 * | d        | Line 147 (getDate()) | !d.isAfter(to) (line 148)   | DU12    |
 *
 * ============================================================================
 * DATA FLOW TESTING CRITERIA
 * ============================================================================
 *
 * ALL-DEFS: For every variable definition, at least one def-use path is tested.
 *   - from: DU1 or DU2 or DU3
 *   - to: DU4 or DU5 or DU6
 *   - result: DU7 or DU8
 *   - t: DU9 or DU10
 *   - d: DU11 or DU12
 *
 * ALL-USES: For every variable definition, ALL def-use paths are tested.
 *   - from: DU1, DU2, DU3
 *   - to: DU4, DU5, DU6
 *   - result: DU7, DU8
 *   - t: DU9, DU10
 *   - d: DU11, DU12
 *
 * This test class implements the ALL-USES criterion (covers ALL-DEFS by definition).
 *
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Data Flow Testing - TransactionController.filterByDateRange()")
class DataFlowTestingTest {

    @Mock
    private TransactionDAO dao;

    private TransactionController controller;

    // Test dates
    private static final LocalDate MARCH_1  = LocalDate.of(2026, 3, 1);
    private static final LocalDate MARCH_15 = LocalDate.of(2026, 3, 15);
    private static final LocalDate MARCH_31 = LocalDate.of(2026, 3, 31);
    private static final LocalDate APRIL_1  = LocalDate.of(2026, 4, 1);

    @BeforeEach
    void setUp() {
        controller = new TransactionController(dao);
    }

    // =========================================================================
    // DU1 & DU4: 'from' and 'to' used in null check (line 138)
    // Definition: Parameter entry
    // Use: if (from == null || to == null)
    // =========================================================================

    @Test
    @DisplayName("DU1: 'from' definition reaches null check use - from is null")
    void du1_fromDefinitionReachesNullCheck_fromIsNull() {
        // Tests: from defined at parameter entry, used at null check
        // Path: def(from) at entry -> use(from) at "from == null"
        // Triggers: TRUE branch (from == null)

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> controller.filterByDateRange(null, MARCH_31)
        );

        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    @DisplayName("DU4: 'to' definition reaches null check use - to is null")
    void du4_toDefinitionReachesNullCheck_toIsNull() {
        // Tests: to defined at parameter entry, used at null check
        // Path: def(to) at entry -> use(to) at "to == null"
        // Triggers: TRUE branch (to == null)

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> controller.filterByDateRange(MARCH_1, null)
        );

        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    @DisplayName("DU1 & DU4: Both 'from' and 'to' pass null check - valid dates")
    void du1_du4_bothDefinitionsReachNullCheck_validDates() throws IOException {
        // Tests: Both from and to pass the null check (FALSE branch)
        // Path: def(from), def(to) at entry -> use at null check -> FALSE

        when(dao.loadAll()).thenReturn(Collections.emptyList());

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertNotNull(result);
    }

    // =========================================================================
    // DU2 & DU5: 'from' and 'to' used in isAfter comparison (line 141)
    // Definition: Parameter entry
    // Use: if (from.isAfter(to))
    // =========================================================================

    @Test
    @DisplayName("DU2 & DU5: 'from' and 'to' reach isAfter check - from > to throws")
    void du2_du5_fromAndToReachIsAfterCheck_fromAfterTo() {
        // Tests: from and to definitions reach the isAfter comparison
        // Path: def(from), def(to) at entry -> use at "from.isAfter(to)"
        // Triggers: TRUE branch (from is after to)

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> controller.filterByDateRange(MARCH_31, MARCH_1)
        );

        assertTrue(ex.getMessage().contains("Start date cannot be after end date"));
    }

    @Test
    @DisplayName("DU2 & DU5: 'from' and 'to' reach isAfter check - from <= to passes")
    void du2_du5_fromAndToReachIsAfterCheck_fromBeforeOrEqualTo() throws IOException {
        // Tests: from and to pass the isAfter check (FALSE branch)
        // Path: def(from), def(to) -> use at isAfter -> FALSE -> continues

        when(dao.loadAll()).thenReturn(Collections.emptyList());

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // DU3 & DU6: 'from' and 'to' used in loop date comparison (line 148)
    // Definition: Parameter entry
    // Use: if (!d.isBefore(from) && !d.isAfter(to))
    // =========================================================================

    @Test
    @DisplayName("DU3 & DU6: 'from' and 'to' reach loop comparison - date in range")
    void du3_du6_fromAndToReachLoopComparison_dateInRange() throws IOException {
        // Tests: from and to definitions reach the loop's date boundary check
        // Path: def(from), def(to) -> use at "!d.isBefore(from) && !d.isAfter(to)"
        // Condition: TRUE (transaction date is within [from, to])

        Transaction inRange = makeTransaction("t1", MARCH_15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(inRange));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("DU3: 'from' used in isBefore check - date exactly on 'from' boundary")
    void du3_fromUsedInIsBeforeCheck_dateOnFromBoundary() throws IOException {
        // Tests: from is used to check lower boundary (d.isBefore(from))
        // When date == from: !d.isBefore(from) is TRUE

        Transaction onFrom = makeTransaction("t1", MARCH_1);
        when(dao.loadAll()).thenReturn(Collections.singletonList(onFrom));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getId());
    }

    @Test
    @DisplayName("DU6: 'to' used in isAfter check - date exactly on 'to' boundary")
    void du6_toUsedInIsAfterCheck_dateOnToBoundary() throws IOException {
        // Tests: to is used to check upper boundary (d.isAfter(to))
        // When date == to: !d.isAfter(to) is TRUE

        Transaction onTo = makeTransaction("t1", MARCH_31);
        when(dao.loadAll()).thenReturn(Collections.singletonList(onTo));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getId());
    }

    @Test
    @DisplayName("DU3 & DU6: Date before 'from' - isBefore returns TRUE, excluded")
    void du3_du6_dateBeforeFrom_excluded() throws IOException {
        // Tests: from boundary check - date < from
        // Condition: d.isBefore(from) is TRUE, so !d.isBefore(from) is FALSE
        // Result: transaction excluded

        Transaction beforeFrom = makeTransaction("t1", LocalDate.of(2026, 2, 28));
        when(dao.loadAll()).thenReturn(Collections.singletonList(beforeFrom));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("DU3 & DU6: Date after 'to' - isAfter returns TRUE, excluded")
    void du3_du6_dateAfterTo_excluded() throws IOException {
        // Tests: to boundary check - date > to
        // Condition: d.isAfter(to) is TRUE, so !d.isAfter(to) is FALSE
        // Result: transaction excluded

        Transaction afterTo = makeTransaction("t1", APRIL_1);
        when(dao.loadAll()).thenReturn(Collections.singletonList(afterTo));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // DU7: 'result' used in result.add(t) (line 149)
    // Definition: Line 145 (result = new ArrayList<>())
    // Use: result.add(t)
    // =========================================================================

    @Test
    @DisplayName("DU7: 'result' definition reaches add() use - transaction added")
    void du7_resultDefinitionReachesAdd_transactionAdded() throws IOException {
        // Tests: result is defined as new ArrayList, then used in result.add(t)
        // Path: def(result) at line 145 -> use(result) at "result.add(t)" line 149

        Transaction inRange = makeTransaction("t1", MARCH_15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(inRange));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(1, result.size());
        assertTrue(result.contains(inRange));
    }

    @Test
    @DisplayName("DU7: Multiple adds to 'result' - all in-range transactions added")
    void du7_multipleAddsToResult() throws IOException {
        // Tests: result.add() is called multiple times during loop

        Transaction t1 = makeTransaction("t1", MARCH_1);
        Transaction t2 = makeTransaction("t2", MARCH_15);
        Transaction t3 = makeTransaction("t3", MARCH_31);
        when(dao.loadAll()).thenReturn(Arrays.asList(t1, t2, t3));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(3, result.size());
    }

    // =========================================================================
    // DU8: 'result' used in return statement (line 152)
    // Definition: Line 145 (result = new ArrayList<>())
    // Use: return result
    // =========================================================================

    @Test
    @DisplayName("DU8: 'result' definition reaches return use - empty list returned")
    void du8_resultDefinitionReachesReturn_emptyList() throws IOException {
        // Tests: result is defined, loop doesn't add anything, result is returned
        // Path: def(result) at line 145 -> use(result) at "return result" line 152
        // Case: Empty list from DAO, no additions

        when(dao.loadAll()).thenReturn(Collections.emptyList());

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("DU8: 'result' with additions reaches return use - populated list returned")
    void du8_resultDefinitionReachesReturn_populatedList() throws IOException {
        // Tests: result has items added, then returned
        // Covers def-use path from definition through add() to return

        Transaction t1 = makeTransaction("t1", MARCH_15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(t1));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // =========================================================================
    // DU9 & DU10: 't' (loop variable) definitions and uses
    // Definition: Line 146 (for-each iterator)
    // Uses: t.getDate() (line 147), result.add(t) (line 149)
    // =========================================================================

    @Test
    @DisplayName("DU9: 't' definition reaches getDate() use - date extracted for comparison")
    void du9_tDefinitionReachesGetDate() throws IOException {
        // Tests: t is defined by for-each, then t.getDate() is called
        // Path: def(t) at for-each -> use(t) at "t.getDate()"

        Transaction t = makeTransaction("t1", MARCH_15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(t));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        // If getDate() wasn't called, the comparison couldn't happen
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("DU10: 't' definition reaches add() use - transaction added to result")
    void du10_tDefinitionReachesAdd() throws IOException {
        // Tests: t is defined by for-each, passes filter, added to result
        // Path: def(t) at for-each -> use(t) at "result.add(t)"

        Transaction t = makeTransaction("t1", MARCH_15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(t));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(1, result.size());
        assertSame(t, result.get(0)); // Same object reference
    }

    @Test
    @DisplayName("DU9 & DU10: 't' used in multiple iterations")
    void du9_du10_tUsedInMultipleIterations() throws IOException {
        // Tests: t is redefined each iteration, both uses exercised each time

        Transaction t1 = makeTransaction("t1", MARCH_1);
        Transaction t2 = makeTransaction("t2", MARCH_15);
        when(dao.loadAll()).thenReturn(Arrays.asList(t1, t2));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));
    }

    // =========================================================================
    // DU11 & DU12: 'd' (date variable) definitions and uses
    // Definition: Line 147 (d = t.getDate())
    // Uses: !d.isBefore(from) (line 148), !d.isAfter(to) (line 148)
    // =========================================================================

    @Test
    @DisplayName("DU11: 'd' definition reaches isBefore() use - lower bound check")
    void du11_dDefinitionReachesIsBefore() throws IOException {
        // Tests: d is defined from t.getDate(), then used in d.isBefore(from)
        // Path: def(d) at "d = t.getDate()" -> use(d) at "d.isBefore(from)"

        Transaction t = makeTransaction("t1", MARCH_15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(t));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        // d.isBefore(MARCH_1) for MARCH_15 returns FALSE, so !FALSE = TRUE
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("DU12: 'd' definition reaches isAfter() use - upper bound check")
    void du12_dDefinitionReachesIsAfter() throws IOException {
        // Tests: d is defined from t.getDate(), then used in d.isAfter(to)
        // Path: def(d) at "d = t.getDate()" -> use(d) at "d.isAfter(to)"

        Transaction t = makeTransaction("t1", MARCH_15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(t));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        // d.isAfter(MARCH_31) for MARCH_15 returns FALSE, so !FALSE = TRUE
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("DU11 & DU12: 'd' used in both boundary checks - date exactly on boundaries")
    void du11_du12_dUsedInBothBoundaryChecks() throws IOException {
        // Tests: d is used in both isBefore and isAfter in the same condition
        // When d == from: isBefore returns FALSE (not before itself)
        // When d == to: isAfter returns FALSE (not after itself)

        Transaction onFrom = makeTransaction("t1", MARCH_1);
        Transaction onTo = makeTransaction("t2", MARCH_31);
        when(dao.loadAll()).thenReturn(Arrays.asList(onFrom, onTo));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("DU11: 'd' isBefore returns TRUE - date before range, excluded")
    void du11_dIsBeforeReturnsTrue_excluded() throws IOException {
        // Tests: d.isBefore(from) returns TRUE, so !TRUE = FALSE
        // Transaction should be excluded

        Transaction beforeRange = makeTransaction("t1", LocalDate.of(2026, 2, 15));
        when(dao.loadAll()).thenReturn(Collections.singletonList(beforeRange));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("DU12: 'd' isAfter returns TRUE - date after range, excluded")
    void du12_dIsAfterReturnsTrue_excluded() throws IOException {
        // Tests: d.isAfter(to) returns TRUE, so !TRUE = FALSE
        // Transaction should be excluded

        Transaction afterRange = makeTransaction("t1", APRIL_1);
        when(dao.loadAll()).thenReturn(Collections.singletonList(afterRange));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // COMPREHENSIVE ALL-USES COVERAGE TEST
    // =========================================================================

    @Test
    @DisplayName("All-Uses Coverage: Single test exercising all 12 def-use pairs")
    void allUsesCoverage_comprehensiveTest() throws IOException {
        // This test exercises a path that covers all def-use pairs:
        // DU1, DU2, DU3: from used in null check (FALSE), isAfter (FALSE), isBefore
        // DU4, DU5, DU6: to used in null check (FALSE), isAfter (FALSE), isAfter
        // DU7: result used in add()
        // DU8: result used in return
        // DU9: t used in getDate()
        // DU10: t used in add()
        // DU11: d used in isBefore()
        // DU12: d used in isAfter()

        Transaction t1 = makeTransaction("t1", MARCH_15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(t1));

        // Execute method - exercises all def-use pairs except the exception paths
        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        // Verify all uses were exercised
        assertNotNull(result);                    // DU8: result returned
        assertEquals(1, result.size());           // DU7: result.add called
        assertEquals("t1", result.get(0).getId());// DU10: t added to result
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    /**
     * Creates a test transaction with the given ID and date.
     */
    private Transaction makeTransaction(String id, LocalDate date) {
        return new Transaction(id, 100.00, date, "Test transaction",
            Category.FOOD, TransactionType.EXPENSE);
    }
}
