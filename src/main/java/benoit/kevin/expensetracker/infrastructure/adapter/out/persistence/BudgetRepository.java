package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface BudgetRepository extends JpaRepository<BudgetJpaEntity, UUID> {
    List<BudgetJpaEntity> findByOwnerId(UUID ownerId);
}
