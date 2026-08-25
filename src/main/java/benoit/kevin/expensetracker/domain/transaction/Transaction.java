package benoit.kevin.expensetracker.domain.transaction;

import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.time.LocalDate;

public sealed interface Transaction permits Expense, Income, Refund {
    TransactionId transactionId();
    UserId userId();
    Money amount();
    String description();
    LocalDate date();
}
