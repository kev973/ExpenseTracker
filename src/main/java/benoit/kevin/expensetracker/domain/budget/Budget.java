package benoit.kevin.expensetracker.domain.budget;

import benoit.kevin.expensetracker.domain.period.Period;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.Objects;

public record Budget(BudgetId budgetId, UserId ownerId, Period period) {
    public Budget{
        Objects.requireNonNull(budgetId);
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(period);
    }
}
