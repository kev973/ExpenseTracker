package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import benoit.kevin.expensetracker.application.port.out.TransactionPort;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.transaction.Expense;
import benoit.kevin.expensetracker.domain.transaction.Income;
import benoit.kevin.expensetracker.domain.transaction.Refund;
import benoit.kevin.expensetracker.domain.transaction.Transaction;
import benoit.kevin.expensetracker.domain.transaction.TransactionId;
import benoit.kevin.expensetracker.domain.user.UserId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionPersistenceAdapter implements TransactionPort {

    /**
     * One table for the three kinds, discriminated by {@code type}. An Expense and an
     * Income carry a budgetId; a Refund carries an expenseId instead and takes its budget
     * from the expense it refunds, which is why budgetId is copied onto it here: it is a
     * query concern of this adapter, not a domain field.
     */
    @Entity
    @Table(name = "transactions")
    static class TransactionJpaEntity {

        @Id
        private UUID id;

        private String type;

        private UUID budgetId;

        private UUID userId;

        private UUID labelId;

        private UUID expenseId;

        private String source;

        private long amountMinorUnits;

        private String description;

        private LocalDate date;

        protected TransactionJpaEntity() {}

        static TransactionJpaEntity fromDomain(Transaction transaction, UUID budgetId) {
            var entity = new TransactionJpaEntity();
            entity.id = transaction.transactionId().id();
            entity.userId = transaction.userId().id();
            entity.amountMinorUnits = transaction.amount().minorUnits();
            entity.description = transaction.description();
            entity.date = transaction.date();
            entity.budgetId = budgetId;
            switch (transaction) {
                case Expense expense -> {
                    entity.type = "EXPENSE";
                    entity.labelId = expense.labelId().id();
                }
                case Income income -> {
                    entity.type = "INCOME";
                    entity.source = income.source();
                }
                case Refund refund -> {
                    entity.type = "REFUND";
                    entity.expenseId = refund.expenseId().id();
                }
            }
            return entity;
        }

        Transaction toDomain() {
            var transactionId = new TransactionId(id);
            var user = new UserId(userId);
            var amount = new Money(amountMinorUnits);
            return switch (type) {
                case "EXPENSE" -> new Expense(transactionId, new BudgetId(budgetId), user,
                        new LabelId(labelId), amount, description, date);
                case "INCOME" -> new Income(transactionId, new BudgetId(budgetId), user,
                        amount, source, description, date);
                case "REFUND" -> new Refund(transactionId, new TransactionId(expenseId), user,
                        amount, description, date);
                default -> throw new IllegalStateException("unknown transaction type " + type);
            };
        }
    }

    private final TransactionRepository repository;

    public TransactionPersistenceAdapter(TransactionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void save(Transaction transaction) {
        repository.save(TransactionJpaEntity.fromDomain(transaction, budgetIdOf(transaction)));
    }

    @Override
    public List<Transaction> findByBudget(BudgetId budgetId) {
        return repository.findByBudgetIdOrderByDateAsc(budgetId.id()).stream()
                .map(TransactionJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Transaction> load(TransactionId transactionId) {
        return repository.findById(transactionId.id()).map(TransactionJpaEntity::toDomain);
    }

    private UUID budgetIdOf(Transaction transaction) {
        return switch (transaction) {
            case Expense expense -> expense.budgetId().id();
            case Income income -> income.budgetId().id();
            case Refund refund -> repository.findById(refund.expenseId().id())
                    .orElseThrow(() -> new IllegalStateException("refunded expense not found"))
                    .budgetId;
        };
    }
}
