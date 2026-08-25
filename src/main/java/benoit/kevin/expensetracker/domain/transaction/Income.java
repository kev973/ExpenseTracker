package benoit.kevin.expensetracker.domain.transaction;

import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.time.LocalDate;
import java.util.Objects;

public record Income(TransactionId transactionId, BudgetId budgetId, UserId userId,
                     Money amount, String source, String description, LocalDate date) implements Transaction {
    public Income{
        Objects.requireNonNull(transactionId);
        Objects.requireNonNull(budgetId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(source);
        Objects.requireNonNull(description);
        Objects.requireNonNull(date);
    }
}
