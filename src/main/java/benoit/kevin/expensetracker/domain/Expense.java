package benoit.kevin.expensetracker.domain;

import java.util.Objects;

public record Expense(long id, long amount, String description) {
    public Expense{
        Objects.requireNonNull(description);
        if(amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
