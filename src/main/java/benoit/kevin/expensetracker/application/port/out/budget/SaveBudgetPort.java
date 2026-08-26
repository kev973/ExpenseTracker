package benoit.kevin.expensetracker.application.port.out.budget;

import benoit.kevin.expensetracker.domain.budget.Budget;

public interface SaveBudgetPort {
    void save(Budget budget);
}
