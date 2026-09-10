package benoit.kevin.expensetracker.application.port.in;

import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.label.LabelId;

import java.util.List;

/**
 * Read model. Amounts are plain minor units because remaining can go negative,
 * which Money forbids by design.
 */
public record BudgetSummary(BudgetId budgetId,
                            long plannedTotal,
                            long totalIncome,
                            long totalConsumed,
                            List<LabelLine> lines) {

    public record LabelLine(LabelId labelId, long limit, long consumed, long remaining) {}
}
