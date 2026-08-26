package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BudgetRepository extends JpaRepository<BudgetJpaEntity, UUID> {
}
