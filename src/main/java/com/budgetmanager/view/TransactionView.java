package com.budgetmanager.view;

import com.budgetmanager.model.Transaction;

import java.io.PrintStream;
import java.util.List;

/**
 * Displays transaction lists and details as formatted console output.
 */
public class TransactionView {

    private static final String DIVIDER =
        "+--------------------------------------+----------+------------+----------------------+---------------+----------+";
    private static final String HEADER  =
        "| ID (truncated)                       | Amount   | Date       | Description          | Category      | Type     |";

    private final PrintStream out;

    /**
     * Creates a TransactionView that writes to stdout.
     */
    public TransactionView() {
        this(System.out);
    }

    /**
     * Testable constructor — injects the output destination.
     *
     * @param out the PrintStream to write to
     */
    public TransactionView(PrintStream out) {
        this.out = out;
    }

    // -------------------------------------------------------------------------
    // List display
    // -------------------------------------------------------------------------

    /**
     * Displays a list of transactions in a formatted table.
     * Shows the first 36 characters of the UUID, amount, date, description
     * (truncated to 20 chars), category, and type.
     *
     * @param transactions the list to display; may be empty
     */
    public void displayTransactionList(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            out.println("  No transactions found.");
            return;
        }
        out.println(DIVIDER);
        out.println(HEADER);
        out.println(DIVIDER);
        for (Transaction t : transactions) {
            String shortId  = t.getId().length() > 36 ? t.getId().substring(0, 36) : t.getId();
            String shortDesc = t.getDescription().length() > 20
                ? t.getDescription().substring(0, 17) + "..."
                : t.getDescription();
            out.printf("| %-36s | %8.2f | %s | %-20s | %-13s | %-8s |%n",
                shortId,
                t.getAmount(),
                t.getDate(),
                shortDesc,
                t.getCategory().getDisplayName(),
                t.getType().name()
            );
        }
        out.println(DIVIDER);
        out.printf("  Total: %d transaction(s)%n", transactions.size());
    }

    // -------------------------------------------------------------------------
    // Single transaction detail
    // -------------------------------------------------------------------------

    /**
     * Displays the full details of a single transaction in a labelled format.
     *
     * @param t the transaction to display (must not be null)
     */
    public void displayTransactionDetails(Transaction t) {
        out.println("  ------- Transaction Details -------");
        out.println("  ID          : " + t.getId());
        out.printf ("  Amount      : %.2f%n", t.getAmount());
        out.println("  Date        : " + t.getDate());
        out.println("  Description : " + t.getDescription());
        out.println("  Category    : " + t.getCategory().getDisplayName());
        out.println("  Type        : " + t.getType().name());
        out.println("  -----------------------------------");
    }

    /**
     * Prints a header banner for the "Add Transaction" workflow.
     * Called by Main before prompting the user for transaction fields.
     */
    public void showAddTransactionPrompt() {
        out.println();
        out.println("  === Add New Transaction ===");
    }
}
