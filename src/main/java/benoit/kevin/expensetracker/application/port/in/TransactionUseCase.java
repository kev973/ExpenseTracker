package benoit.kevin.expensetracker.application.port.in;

import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.transaction.Transaction;
import benoit.kevin.expensetracker.domain.transaction.TransactionId;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public interface TransactionUseCase {

    TransactionId recordExpense(RecordExpense command);

    TransactionId recordIncome(RecordIncome command);

    TransactionId recordRefund(RecordRefund command);

    List<Transaction> listByBudget(BudgetId budgetId, UserId caller);

    record RecordExpense(BudgetId budgetId, LabelId labelId, Money amount, String description,
                         LocalDate date, UserId caller) {
        public RecordExpense {
            Objects.requireNonNull(budgetId);
            Objects.requireNonNull(labelId);
            Objects.requireNonNull(amount);
            Objects.requireNonNull(description);
            Objects.requireNonNull(date);
            Objects.requireNonNull(caller);
        }
    }

    record RecordIncome(BudgetId budgetId, Money amount, String source, String description,
                        LocalDate date, UserId caller) {
        public RecordIncome {
            Objects.requireNonNull(budgetId);
            Objects.requireNonNull(amount);
            Objects.requireNonNull(source);
            Objects.requireNonNull(description);
            Objects.requireNonNull(date);
            Objects.requireNonNull(caller);
        }
    }

    record RecordRefund(TransactionId expenseId, Money amount, String description,
                        LocalDate date, UserId caller) {
        public RecordRefund {
            Objects.requireNonNull(expenseId);
            Objects.requireNonNull(amount);
            Objects.requireNonNull(description);
            Objects.requireNonNull(date);
            Objects.requireNonNull(caller);
        }
    }
}
