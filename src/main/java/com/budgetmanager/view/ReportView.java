package com.budgetmanager.view;

import com.budgetmanager.model.Budget;
import com.budgetmanager.model.Category;
import com.budgetmanager.model.Report;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Displays financial reports, budget status, and category breakdowns
 * as formatted console output.
 */
public class ReportView {

    private final PrintStream out;

    /**
     * Creates a ReportView that writes to stdout.
     */
    public ReportView() {
        this(System.out);
    }

    /**
     * Testable constructor — injects the output destination.
     *
     * @param out the PrintStream to write to
     */
    public ReportView(PrintStream out) {
        this.out = out;
    }

    // -------------------------------------------------------------------------
    // Report display
    // -------------------------------------------------------------------------

    /**
     * Displays a financial summary report with income, expense, balance,
     * and savings rate.
     *
     * @param report the Report to display (must not be null)
     */
    public void displayReport(Report report) {
        out.println();
        out.println("  ====== Financial Report: " + report.getPeriod() + " ======");
        out.printf ("  Total Income   : $%10.2f%n", report.getTotalIncome());
        out.printf ("  Total Expenses : $%10.2f%n", report.getTotalExpense());
        out.println("  ----------------------------------------");
        out.printf ("  Net Balance    : $%10.2f%n", report.getBalance());
        out.printf ("  Savings Rate   :  %9.1f%%%n", report.getSavingsRate() * 100);
        out.println("  ========================================");
    }

    // -------------------------------------------------------------------------
    // Budget status display
    // -------------------------------------------------------------------------

    /**
     * Displays the spending status of all configured budget categories.
     * Each budget is shown with a 20-character progress bar, the spent/limit
     * amounts, and the current usage percentage.
     *
     * @param budgets list of Budget objects to display; may be empty
     */
    public void displayBudgetStatus(List<Budget> budgets) {
        out.println();
        out.println("  ======= Budget Status =======");
        if (budgets.isEmpty()) {
            out.println("  No budgets configured yet.");
            out.println("  Use option 6 to set a budget limit.");
            return;
        }
        out.printf("  %-15s %8s %8s %8s  %s%n",
            "Category", "Spent", "Limit", "Left", "Progress");
        out.println("  " + "-".repeat(65));
        for (Budget b : budgets) {
            double pct  = b.getUsagePercentage() * 100;
            String bar  = buildProgressBar(b.getUsagePercentage(), 20);
            String flag = b.isExceeded() ? " [EXCEEDED]" : (b.isNearLimit() ? " [WARNING]" : "");
            out.printf("  %-15s %8.2f %8.2f %8.2f  [%s] %.0f%%%s%n",
                b.getCategory().getDisplayName(),
                b.getCurrentSpending(),
                b.getLimit(),
                b.getRemainingBudget(),
                bar,
                pct,
                flag
            );
        }
    }

    // -------------------------------------------------------------------------
    // Category breakdown display
    // -------------------------------------------------------------------------

    /**
     * Displays a breakdown of total expenses grouped by category.
     * Categories are listed in order of enum declaration.
     *
     * @param breakdown map of Category to total expense amount
     */
    public void displayCategoryBreakdown(Map<Category, Double> breakdown) {
        out.println();
        out.println("  ====== Category Expense Breakdown ======");
        if (breakdown.isEmpty()) {
            out.println("  No expense transactions found.");
            return;
        }
        double total = breakdown.values().stream().mapToDouble(Double::doubleValue).sum();
        out.printf("  %-15s %10s  %6s%n", "Category", "Amount", "Share");
        out.println("  " + "-".repeat(38));
        for (Map.Entry<Category, Double> entry : breakdown.entrySet()) {
            double share = total > 0 ? (entry.getValue() / total) * 100 : 0;
            out.printf("  %-15s %10.2f  %5.1f%%%n",
                entry.getKey().getDisplayName(),
                entry.getValue(),
                share
            );
        }
        out.println("  " + "-".repeat(38));
        out.printf("  %-15s %10.2f  100.0%%%n", "TOTAL", total);
        out.println("  =======================================");
    }

    /**
     * Displays a list of budget alert messages.
     * Called after {@code BudgetController.checkAllAlerts()}.
     *
     * @param alerts list of alert strings; may be empty
     */
    public void displayAlerts(List<String> alerts) {
        out.println();
        if (alerts.isEmpty()) {
            out.println("  [OK] All budgets are within limits.");
        } else {
            out.println("  ===== Budget Alerts =====");
            for (String alert : alerts) {
                out.println("  ! " + alert);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a simple text progress bar.
     *
     * @param ratio   fraction filled (0.0 to 1.0+)
     * @param width   total bar width in characters
     * @return a string of filled/empty characters, e.g. "##########          "
     */
    private String buildProgressBar(double ratio, int width) {
        int filled = (int) Math.min(ratio * width, width);
        return "#".repeat(filled) + " ".repeat(width - filled);
    }
}
