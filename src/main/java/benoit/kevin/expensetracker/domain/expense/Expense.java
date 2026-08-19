package benoit.kevin.expensetracker.domain.expense;

import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.Objects;

public record Expense(ExpenseId expenseId, UserId userId, long amount, String description, String label) {
    public Expense{
        Objects.requireNonNull(expenseId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(description);
        if(amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
