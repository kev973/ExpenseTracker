package benoit.kevin.expensetracker.application.port.out;

import benoit.kevin.expensetracker.domain.budget.Budget;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface BudgetPort {
    void save(Budget budget);

    Optional<Budget> load(BudgetId budgetId);

    List<Budget> findByOwner(UserId ownerId);
}
