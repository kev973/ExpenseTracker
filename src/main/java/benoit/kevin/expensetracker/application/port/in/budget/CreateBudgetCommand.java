package benoit.kevin.expensetracker.application.port.in.budget;

import benoit.kevin.expensetracker.domain.period.Period;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.Objects;

public record CreateBudgetCommand(UserId owner, Period period) {
    public CreateBudgetCommand {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(period);
    }
}
