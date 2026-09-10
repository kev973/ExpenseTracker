package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import benoit.kevin.expensetracker.application.port.out.BudgetPort;
import benoit.kevin.expensetracker.domain.budget.Budget;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.user.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class BudgetPersistenceAdapter implements BudgetPort {

    private final BudgetRepository repository;

    public BudgetPersistenceAdapter(BudgetRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    /**
     * Updates the managed row in place rather than merging a detached copy, so that
     * orphanRemoval on the envelope collection behaves predictably.
     */
    @Override
    @Transactional
    public void save(Budget budget) {
        repository.findById(budget.budgetId().id())
                .ifPresentOrElse(existing -> existing.replaceEnvelopes(budget.envelopes()),
                        () -> repository.save(BudgetJpaEntity.fromDomain(budget)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Budget> load(BudgetId budgetId) {
        return repository.findById(budgetId.id()).map(BudgetJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> findByOwner(UserId ownerId) {
        return repository.findByOwnerId(ownerId.id()).stream().map(BudgetJpaEntity::toDomain).toList();
    }
}
