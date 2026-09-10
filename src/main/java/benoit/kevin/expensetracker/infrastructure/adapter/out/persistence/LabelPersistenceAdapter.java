package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import benoit.kevin.expensetracker.application.port.out.LabelPort;
import benoit.kevin.expensetracker.domain.label.Label;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.user.UserId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class LabelPersistenceAdapter implements LabelPort {

    @Entity
    @Table(name = "labels")
    static class LabelJpaEntity {

        @Id
        private UUID id;

        private UUID parentId;

        private UUID userId;

        private String name;

        protected LabelJpaEntity() {}

        static LabelJpaEntity fromDomain(Label label) {
            var entity = new LabelJpaEntity();
            entity.id = label.labelId().id();
            entity.parentId = label.parentId().map(LabelId::id).orElse(null);
            entity.userId = label.userId().id();
            entity.name = label.name();
            return entity;
        }

        Label toDomain() {
            return new Label(new LabelId(id),
                    Optional.ofNullable(parentId).map(LabelId::new),
                    new UserId(userId),
                    name);
        }
    }

    private final LabelRepository repository;

    public LabelPersistenceAdapter(LabelRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void save(Label label) {
        repository.save(LabelJpaEntity.fromDomain(label));
    }

    @Override
    public List<Label> findByUser(UserId userId) {
        return repository.findByUserId(userId.id()).stream().map(LabelJpaEntity::toDomain).toList();
    }

    @Override
    public boolean exists(LabelId labelId) {
        return repository.existsById(labelId.id());
    }
}
