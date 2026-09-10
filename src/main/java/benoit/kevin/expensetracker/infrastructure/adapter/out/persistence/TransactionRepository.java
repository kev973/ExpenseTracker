package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface TransactionRepository
        extends JpaRepository<TransactionPersistenceAdapter.TransactionJpaEntity, UUID> {
    List<TransactionPersistenceAdapter.TransactionJpaEntity> findByBudgetIdOrderByDateAsc(UUID budgetId);
}
