package benoit.kevin.expensetracker.domain.transaction;

import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.time.LocalDate;
import java.util.Objects;

public record Refund(TransactionId transactionId, TransactionId expenseId, UserId userId, Money amount, String description, LocalDate date) implements Transaction {
    public Refund {
        Objects.requireNonNull(transactionId);
        Objects.requireNonNull(expenseId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(description);
        Objects.requireNonNull(date);
        if(transactionId.equals(expenseId)){
            throw new IllegalArgumentException("expenseId cannot be the same as transactionId");
        }
    }
}
