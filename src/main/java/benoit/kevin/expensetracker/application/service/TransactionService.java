package benoit.kevin.expensetracker.application.service;

import benoit.kevin.expensetracker.application.port.in.NotOwnerException;
import benoit.kevin.expensetracker.application.port.in.TransactionUseCase;
import benoit.kevin.expensetracker.application.port.out.BudgetPort;
import benoit.kevin.expensetracker.application.port.out.IdGenerator;
import benoit.kevin.expensetracker.application.port.out.LabelPort;
import benoit.kevin.expensetracker.application.port.out.TransactionPort;
import benoit.kevin.expensetracker.domain.budget.Budget;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.transaction.Expense;
import benoit.kevin.expensetracker.domain.transaction.Income;
import benoit.kevin.expensetracker.domain.transaction.Refund;
import benoit.kevin.expensetracker.domain.transaction.Transaction;
import benoit.kevin.expensetracker.domain.transaction.TransactionId;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class TransactionService implements TransactionUseCase {

    private final TransactionPort transactionPort;
    private final BudgetPort budgetPort;
    private final LabelPort labelPort;
    private final IdGenerator idGenerator;

    public TransactionService(TransactionPort transactionPort,
                              BudgetPort budgetPort,
                              LabelPort labelPort,
                              IdGenerator idGenerator) {
        this.transactionPort = Objects.requireNonNull(transactionPort);
        this.budgetPort = Objects.requireNonNull(budgetPort);
        this.labelPort = Objects.requireNonNull(labelPort);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public TransactionId recordExpense(RecordExpense command) {
        loadOwned(command.budgetId(), command.caller());
        if (!labelPort.exists(command.labelId())) {
            throw new IllegalArgumentException("unknown label");
        }
        var expense = new Expense(new TransactionId(idGenerator.next()),
                command.budgetId(),
                command.caller(),
                command.labelId(),
                command.amount(),
                command.description(),
                command.date());
        transactionPort.save(expense);
        return expense.transactionId();
    }

    @Override
    public TransactionId recordIncome(RecordIncome command) {
        loadOwned(command.budgetId(), command.caller());
        var income = new Income(new TransactionId(idGenerator.next()),
                command.budgetId(),
                command.caller(),
                command.amount(),
                command.source(),
                command.description(),
                command.date());
        transactionPort.save(income);
        return income.transactionId();
    }

    @Override
    public TransactionId recordRefund(RecordRefund command) {
        var target = transactionPort.load(command.expenseId())
                .orElseThrow(() -> new NoSuchElementException("expense not found"));
        if (!(target instanceof Expense expense)) {
            throw new IllegalArgumentException("a refund can only point at an expense");
        }
        loadOwned(expense.budgetId(), command.caller());
        if (command.amount().minorUnits() > expense.amount().minorUnits()) {
            throw new IllegalArgumentException("a refund cannot exceed the expense it refunds");
        }
        var refund = new Refund(new TransactionId(idGenerator.next()),
                expense.transactionId(),
                command.caller(),
                command.amount(),
                command.description(),
                command.date());
        transactionPort.save(refund);
        return refund.transactionId();
    }

    @Override
    public List<Transaction> listByBudget(BudgetId budgetId, UserId caller) {
        loadOwned(budgetId, caller);
        return transactionPort.findByBudget(budgetId);
    }

    private Budget loadOwned(BudgetId budgetId, UserId caller) {
        var budget = budgetPort.load(budgetId)
                .orElseThrow(() -> new NoSuchElementException("budget not found"));
        if (!budget.ownerId().equals(caller)) {
            throw new NotOwnerException();
        }
        return budget;
    }
}
