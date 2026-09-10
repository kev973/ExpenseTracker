package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface LabelRepository extends JpaRepository<LabelPersistenceAdapter.LabelJpaEntity, UUID> {
    List<LabelPersistenceAdapter.LabelJpaEntity> findByUserId(UUID userId);
}
