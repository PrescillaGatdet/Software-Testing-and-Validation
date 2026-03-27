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
 * Path Testing for TransactionController.filterByDateRange(LocalDate from, LocalDate to)
 *
 * This test class implements structural testing using path testing methodology.
 * The target method has a cyclomatic complexity of V(G) = 5, requiring 5 independent
 * basis paths to achieve complete path coverage.
 *
 * ============================================================================
 * CONTROL FLOW GRAPH (CFG) for filterByDateRange(LocalDate from, LocalDate to)
 * ============================================================================
 *
 *     +------------------+
 *     | Node 1: ENTRY    |
 *     | (from, to)       |
 *     +--------+---------+
 *              |
 *              v
 *     +------------------+
 *     | Node 2: DECISION |
 *     | from == null ||  |
 *     | to == null?      |
 *     +--------+---------+
 *              |
 *       +------+------+
 *       | TRUE        | FALSE
 *       v             v
 *  +----------+   +------------------+
 *  | Node 3:  |   | Node 4: DECISION |
 *  | throw    |   | from.isAfter(to)?|
 *  | IAE      |   +--------+---------+
 *  +----------+            |
 *                   +------+------+
 *                   | TRUE        | FALSE
 *                   v             v
 *              +----------+   +------------------+
 *              | Node 5:  |   | Node 6:          |
 *              | throw    |   | result = new     |
 *              | IAE      |   | ArrayList<>()    |
 *              +----------+   +--------+---------+
 *                                      |
 *                                      v
 *                             +------------------+
 *                             | Node 7: FOR LOOP |
 *                             | hasNext()?       |
 *                             +--------+---------+
 *                                      |
 *                               +------+------+
 *                               | TRUE        | FALSE
 *                               v             v
 *                      +------------------+  +------------------+
 *                      | Node 8: DECISION |  | Node 10: RETURN  |
 *                      | !d.isBefore(from)|  | return result    |
 *                      | && !d.isAfter(to)|  +------------------+
 *                      +--------+---------+
 *                               |
 *                        +------+------+
 *                        | TRUE        | FALSE
 *                        v             v
 *                   +----------+       |
 *                   | Node 9:  |       |
 *                   | result.  |       |
 *                   | add(t)   |       |
 *                   +----+-----+       |
 *                        |             |
 *                        +------+------+
 *                               |
 *                               v
 *                        (back to Node 7)
 *
 * ============================================================================
 * CYCLOMATIC COMPLEXITY CALCULATION
 * ============================================================================
 *
 * Method 1: V(G) = E - N + 2P
 *   - Edges (E) = 11
 *   - Nodes (N) = 10
 *   - Connected components (P) = 1
 *   - V(G) = 11 - 10 + 2(1) = 3... wait let me recalculate
 *
 * Method 2: V(G) = Number of predicate nodes + 1
 *   - Predicate nodes: Node 2 (null check), Node 4 (isAfter check),
 *     Node 7 (loop condition), Node 8 (date range check)
 *   - V(G) = 4 + 1 = 5
 *
 * Therefore: V(G) = 5 independent paths required
 *
 * ============================================================================
 * INDEPENDENT BASIS PATHS
 * ============================================================================
 *
 * Path 1: 1 -> 2 -> 3 (from is null)
 *         Entry -> null check TRUE -> throw IAE
 *
 * Path 2: 1 -> 2 -> 3 (to is null)
 *         Entry -> null check TRUE -> throw IAE
 *         (Same path as Path 1, but different condition trigger)
 *
 * Path 3: 1 -> 2 -> 4 -> 5 (from after to)
 *         Entry -> null check FALSE -> isAfter TRUE -> throw IAE
 *
 * Path 4: 1 -> 2 -> 4 -> 6 -> 7 -> 10 (empty list)
 *         Entry -> null check FALSE -> isAfter FALSE -> init list ->
 *         loop FALSE (empty) -> return empty list
 *
 * Path 5: 1 -> 2 -> 4 -> 6 -> 7 -> 8 -> 9 -> 7 -> 10 (transaction in range)
 *         Entry -> null check FALSE -> isAfter FALSE -> init list ->
 *         loop TRUE -> date check TRUE -> add -> loop FALSE -> return
 *
 * Path 5b: 1 -> 2 -> 4 -> 6 -> 7 -> 8 -> 7 -> 10 (transaction outside range)
 *          Entry -> null check FALSE -> isAfter FALSE -> init list ->
 *          loop TRUE -> date check FALSE -> loop FALSE -> return empty
 *
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Path Testing - TransactionController.filterByDateRange()")
class PathTestingTest {

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
    // PATH 1: Entry -> Node 2 (TRUE: from == null) -> Node 3 (throw IAE)
    // =========================================================================

    @Test
    @DisplayName("Path 1: from=null triggers null check, throws IllegalArgumentException")
    void path1_fromIsNull_throwsException() {
        // Path: 1 -> 2 -> 3
        // Condition: from == null evaluates to TRUE
        // Expected: IllegalArgumentException thrown at Node 3

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> controller.filterByDateRange(null, MARCH_31)
        );

        assertTrue(ex.getMessage().contains("null"));
        verifyNoInteractions(dao); // DAO never called on this path
    }

    // =========================================================================
    // PATH 2: Entry -> Node 2 (TRUE: to == null) -> Node 3 (throw IAE)
    // =========================================================================

    @Test
    @DisplayName("Path 2: to=null triggers null check, throws IllegalArgumentException")
    void path2_toIsNull_throwsException() {
        // Path: 1 -> 2 -> 3
        // Condition: to == null evaluates to TRUE (short-circuit)
        // Expected: IllegalArgumentException thrown at Node 3

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> controller.filterByDateRange(MARCH_1, null)
        );

        assertTrue(ex.getMessage().contains("null"));
        verifyNoInteractions(dao); // DAO never called on this path
    }

    // =========================================================================
    // PATH 3: Entry -> Node 2 (FALSE) -> Node 4 (TRUE) -> Node 5 (throw IAE)
    // =========================================================================

    @Test
    @DisplayName("Path 3: from > to triggers isAfter check, throws IllegalArgumentException")
    void path3_fromAfterTo_throwsException() {
        // Path: 1 -> 2 -> 4 -> 5
        // Condition at Node 2: from != null && to != null -> FALSE (continues)
        // Condition at Node 4: from.isAfter(to) -> TRUE
        // Expected: IllegalArgumentException thrown at Node 5

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> controller.filterByDateRange(MARCH_31, MARCH_1)
        );

        assertTrue(ex.getMessage().contains("Start date cannot be after end date"));
        verifyNoInteractions(dao); // DAO never called on this path
    }

    // =========================================================================
    // PATH 4: Entry -> Node 2 (FALSE) -> Node 4 (FALSE) -> Node 6 ->
    //         Node 7 (FALSE: empty list) -> Node 10 (return)
    // =========================================================================

    @Test
    @DisplayName("Path 4: Valid range with empty DAO returns empty list")
    void path4_emptyList_noTransactions() throws IOException {
        // Path: 1 -> 2 -> 4 -> 6 -> 7 -> 10
        // Node 2: null check FALSE (both dates valid)
        // Node 4: isAfter FALSE (from <= to)
        // Node 6: result = new ArrayList<>()
        // Node 7: loop condition FALSE (empty list from DAO)
        // Node 10: return empty result

        when(dao.loadAll()).thenReturn(Collections.emptyList());

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertTrue(result.isEmpty());
        verify(dao, times(1)).loadAll();
    }

    // =========================================================================
    // PATH 5: Entry -> Node 2 (FALSE) -> Node 4 (FALSE) -> Node 6 ->
    //         Node 7 (TRUE) -> Node 8 (TRUE) -> Node 9 -> Node 7 (FALSE) -> Node 10
    // =========================================================================

    @Test
    @DisplayName("Path 5: Transaction within date range is included in result")
    void path5_transactionInRange_included() throws IOException {
        // Path: 1 -> 2 -> 4 -> 6 -> 7 -> 8 -> 9 -> 7 -> 10
        // Node 2: null check FALSE
        // Node 4: isAfter FALSE
        // Node 6: result = new ArrayList<>()
        // Node 7: loop TRUE (1 transaction)
        // Node 8: !d.isBefore(from) && !d.isAfter(to) -> TRUE (date in range)
        // Node 9: result.add(t)
        // Node 7: loop FALSE (no more)
        // Node 10: return result with 1 transaction

        Transaction inRange = makeTransaction("t1", MARCH_15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(inRange));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getId());
        verify(dao, times(1)).loadAll();
    }

    // =========================================================================
    // PATH 5b: Entry -> Node 2 (FALSE) -> Node 4 (FALSE) -> Node 6 ->
    //          Node 7 (TRUE) -> Node 8 (FALSE) -> Node 7 (FALSE) -> Node 10
    // =========================================================================

    @Test
    @DisplayName("Path 5b: Transaction outside date range is excluded from result")
    void path5b_transactionOutsideRange_excluded() throws IOException {
        // Path: 1 -> 2 -> 4 -> 6 -> 7 -> 8 -> 7 -> 10
        // Node 8: !d.isBefore(from) && !d.isAfter(to) -> FALSE (date outside range)
        // Transaction is NOT added to result

        Transaction outsideRange = makeTransaction("t1", APRIL_1);
        when(dao.loadAll()).thenReturn(Collections.singletonList(outsideRange));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertTrue(result.isEmpty());
        verify(dao, times(1)).loadAll();
    }

    // =========================================================================
    // PATH 5c: Boundary test - transaction exactly on 'from' date (inclusive)
    // =========================================================================

    @Test
    @DisplayName("Path 5c: Transaction exactly on 'from' boundary is included (inclusive)")
    void path5c_transactionOnFromBoundary_included() throws IOException {
        // Tests inclusive lower bound: date == from
        // Condition: !d.isBefore(from) -> TRUE (same date is not before)
        //           !d.isAfter(to) -> TRUE (same or earlier)

        Transaction onFromBoundary = makeTransaction("t1", MARCH_1);
        when(dao.loadAll()).thenReturn(Collections.singletonList(onFromBoundary));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getId());
    }

    // =========================================================================
    // PATH 5d: Boundary test - transaction exactly on 'to' date (inclusive)
    // =========================================================================

    @Test
    @DisplayName("Path 5d: Transaction exactly on 'to' boundary is included (inclusive)")
    void path5d_transactionOnToBoundary_included() throws IOException {
        // Tests inclusive upper bound: date == to
        // Condition: !d.isBefore(from) -> TRUE (date is after from)
        //           !d.isAfter(to) -> TRUE (same date is not after)

        Transaction onToBoundary = makeTransaction("t1", MARCH_31);
        when(dao.loadAll()).thenReturn(Collections.singletonList(onToBoundary));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getId());
    }

    // =========================================================================
    // PATH 5e: Same day range - from == to, transaction on that day
    // =========================================================================

    @Test
    @DisplayName("Path 5e: Same-day range (from == to) includes transaction on that day")
    void path5e_sameDayRange_transactionIncluded() throws IOException {
        // Special case: from == to (single day range)
        // Transaction on that exact day should be included

        Transaction sameDay = makeTransaction("t1", MARCH_15);
        when(dao.loadAll()).thenReturn(Collections.singletonList(sameDay));

        List<Transaction> result = controller.filterByDateRange(MARCH_15, MARCH_15);

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getId());
    }

    // =========================================================================
    // PATH 5f: Multiple transactions - mixed in/out of range
    // =========================================================================

    @Test
    @DisplayName("Path 5f: Multiple transactions - filters correctly (in-range vs out-of-range)")
    void path5f_multipleTransactions_mixedResults() throws IOException {
        // Tests full loop execution with multiple iterations
        // Some transactions in range, some outside

        Transaction beforeRange = makeTransaction("t1", LocalDate.of(2026, 2, 28));
        Transaction inRange1    = makeTransaction("t2", MARCH_1);
        Transaction inRange2    = makeTransaction("t3", MARCH_15);
        Transaction inRange3    = makeTransaction("t4", MARCH_31);
        Transaction afterRange  = makeTransaction("t5", APRIL_1);

        when(dao.loadAll()).thenReturn(Arrays.asList(
            beforeRange, inRange1, inRange2, inRange3, afterRange));

        List<Transaction> result = controller.filterByDateRange(MARCH_1, MARCH_31);

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getId().equals("t2")));
        assertTrue(result.stream().anyMatch(t -> t.getId().equals("t3")));
        assertTrue(result.stream().anyMatch(t -> t.getId().equals("t4")));
        assertFalse(result.stream().anyMatch(t -> t.getId().equals("t1")));
        assertFalse(result.stream().anyMatch(t -> t.getId().equals("t5")));
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
