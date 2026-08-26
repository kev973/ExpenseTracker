package benoit.kevin.expensetracker.application.port.out.budget;

import benoit.kevin.expensetracker.domain.budget.BudgetId;

public interface BudgetIdGenerator {
    BudgetId next();
}