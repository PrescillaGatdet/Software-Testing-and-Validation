package com.budgetmanager.view;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Primary console view — handles all text-based user interaction.
 * Displays menus, prompts for input, and shows feedback messages.
 */
public class ConsoleView {

    private final Scanner     scanner;
    private final PrintStream out;

    /**
     * Creates a ConsoleView that reads from stdin and writes to stdout.
     */
    public ConsoleView() {
        this(new Scanner(System.in), System.out);
    }

    /**
     * Testable constructor — injects I/O streams.
     *
     * @param scanner the input source
     * @param out     the output destination
     */
    public ConsoleView(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out     = out;
    }

    // -------------------------------------------------------------------------
    // Menu display
    // -------------------------------------------------------------------------

    public void showMainMenu() {
        out.println();
        out.println("========================================");
        out.println("   Budget Management System — Menu");
        out.println("========================================");
        out.println("  1. Add Transaction");
        out.println("  2. View All Transactions");
        out.println("  3. Filter by Category");
        out.println("  4. Filter by Date Range");
        out.println("  5. Search by Description");
        out.println("  6. Set Budget Limit");
        out.println("  7. View Budget Status");
        out.println("  8. Check Budget Alerts");
        out.println("  9. Generate Monthly Report");
        out.println(" 10. Generate Yearly Report");
        out.println(" 11. Category Expense Breakdown");
        out.println("  0. Exit");
        out.println("========================================");
        out.print("Enter choice: ");
    }

    public void showCategoryMenu() {
        out.println("  Categories:");
        out.println("  1. Food");
        out.println("  2. Transport");
        out.println("  3. Entertainment");
        out.println("  4. Utilities");
        out.println("  5. Salary");
        out.println("  6. Investment");
        out.println("  7. Other");
        out.print("  Select category (1-7): ");
    }

    /**
     * Prints the transaction type sub-menu.
     */
    public void showTypeMenu() {
        out.println("  Type:");
        out.println("  1. Expense");
        out.println("  2. Income");
        out.print("  Select type (1-2): ");
    }

    // -------------------------------------------------------------------------
    // Feedback messages
    // -------------------------------------------------------------------------

    /**
     * Prints a general-purpose informational message.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        out.println(message);
    }

    /**
     * Prints an error message prefixed with "ERROR:" for visual clarity.
     *
     * @param message the error description
     */
    public void showError(String message) {
        out.println("ERROR: " + message);
    }

    /**
     * Prints a success message prefixed with a checkmark symbol.
     *
     * @param message the success description
     */
    public void showSuccess(String message) {
        out.println("[OK] " + message);
    }

    // -------------------------------------------------------------------------
    // Input reading
    // -------------------------------------------------------------------------

    /**
     * Reads a single line of input from the user and trims it.
     *
     * @return the trimmed input string, or empty string on EOF
     */
    public String readInput() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return "";
    }

    /**
     * Displays a prompt and reads a positive double value.
     * Re-prompts until a valid number > 0 is entered.
     *
     * @param prompt the prompt text to display
     * @return a valid positive double
     */
    public double readDouble(String prompt) {
        while (true) {
            out.print(prompt);
            String raw = readInput();
            try {
                double value = Double.parseDouble(raw);
                if (value > 0) {
                    return value;
                }
                out.println("  Amount must be greater than 0. Try again.");
            } catch (NumberFormatException e) {
                out.println("  Invalid number. Try again.");
            }
        }
    }

    /**
     * Displays a prompt and reads an integer.
     * Re-prompts until a valid integer is entered.
     *
     * @param prompt the prompt text to display
     * @return a valid integer
     */
    public int readInt(String prompt) {
        while (true) {
            out.print(prompt);
            String raw = readInput();
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                out.println("  Invalid number. Try again.");
            }
        }
    }

    /**
     * Displays a prompt and reads a LocalDate in YYYY-MM-DD format.
     * Re-prompts until a valid date is entered.
     * Pressing Enter with no input defaults to today's date.
     *
     * @param prompt the prompt text to display
     * @return a valid LocalDate
     */
    public LocalDate readDate(String prompt) {
        while (true) {
            out.print(prompt + " (YYYY-MM-DD, or Enter for today): ");
            String raw = readInput();
            if (raw.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException e) {
                out.println("  Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }

    /**
     * Displays a yes/no confirmation prompt.
     *
     * @param prompt the question to ask
     * @return true if the user entered 'y' or 'yes' (case-insensitive)
     */
    public boolean confirmAction(String prompt) {
        out.print(prompt + " (y/n): ");
        String raw = readInput().toLowerCase();
        return raw.equals("y") || raw.equals("yes");
    }

    /**
     * Returns the underlying PrintStream for use by other View classes
     * that need to share the same output destination.
     *
     * @return the output PrintStream
     */
    public PrintStream getOut() {
        return out;
    }
}
