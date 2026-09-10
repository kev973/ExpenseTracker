package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface UserRepository extends JpaRepository<UserPersistenceAdapter.UserJpaEntity, UUID> {
    Optional<UserPersistenceAdapter.UserJpaEntity> findByEmail(String email);
}
