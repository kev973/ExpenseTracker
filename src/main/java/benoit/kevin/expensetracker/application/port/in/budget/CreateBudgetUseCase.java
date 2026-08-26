package benoit.kevin.expensetracker.application.port.in.budget;

import benoit.kevin.expensetracker.domain.budget.BudgetId;

public interface CreateBudgetUseCase {
    BudgetId createBudget(CreateBudgetCommand command);
}
