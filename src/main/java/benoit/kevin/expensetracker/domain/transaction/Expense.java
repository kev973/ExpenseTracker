package benoit.kevin.expensetracker.domain.transaction;

import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.time.LocalDate;
import java.util.Objects;

public record Expense(TransactionId transactionId, BudgetId budgetId, UserId userId, LabelId labelId,
                      Money amount, String description, LocalDate date) implements Transaction {
    public Expense{
        Objects.requireNonNull(transactionId);
        Objects.requireNonNull(budgetId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(labelId);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(description);
        Objects.requireNonNull(date);
    }
}
