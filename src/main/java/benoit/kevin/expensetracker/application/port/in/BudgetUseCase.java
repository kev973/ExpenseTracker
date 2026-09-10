package benoit.kevin.expensetracker.application.port.in;

import benoit.kevin.expensetracker.domain.budget.Budget;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.period.Period;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.List;
import java.util.Objects;

public interface BudgetUseCase {

    BudgetId createBudget(CreateBudget command);

    Budget getBudget(BudgetId budgetId, UserId caller);

    List<Budget> listBudgets(UserId owner);

    void allocateEnvelope(AllocateEnvelope command);

    BudgetSummary getSummary(BudgetId budgetId, UserId caller);

    record CreateBudget(UserId owner, Period period) {
        public CreateBudget {
            Objects.requireNonNull(owner);
            Objects.requireNonNull(period);
        }
    }

    record AllocateEnvelope(BudgetId budgetId, LabelId labelId, Money limit, UserId caller) {
        public AllocateEnvelope {
            Objects.requireNonNull(budgetId);
            Objects.requireNonNull(labelId);
            Objects.requireNonNull(limit);
            Objects.requireNonNull(caller);
        }
    }
}
