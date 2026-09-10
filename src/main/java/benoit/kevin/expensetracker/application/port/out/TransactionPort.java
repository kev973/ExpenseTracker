package benoit.kevin.expensetracker.application.port.out;

import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.transaction.Transaction;
import benoit.kevin.expensetracker.domain.transaction.TransactionId;

import java.util.List;
import java.util.Optional;

public interface TransactionPort {
    void save(Transaction transaction);

    List<Transaction> findByBudget(BudgetId budgetId);

    Optional<Transaction> load(TransactionId transactionId);
}
