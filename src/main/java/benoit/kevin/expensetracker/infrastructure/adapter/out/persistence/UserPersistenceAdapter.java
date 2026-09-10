package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import benoit.kevin.expensetracker.application.port.out.UserPort;
import benoit.kevin.expensetracker.domain.user.User;
import benoit.kevin.expensetracker.domain.user.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserPort {

    @Entity
    @Table(name = "users")
    static class UserJpaEntity {

        @Id
        private UUID id;

        private String firstname;

        private String lastname;

        @Column(unique = true, nullable = false)
        private String email;

        private String passwordHash;

        protected UserJpaEntity() {}

        static UserJpaEntity fromDomain(User user) {
            var entity = new UserJpaEntity();
            entity.id = user.userId().id();
            entity.firstname = user.firstname();
            entity.lastname = user.lastname();
            entity.email = user.email();
            entity.passwordHash = user.passwordHash();
            return entity;
        }

        User toDomain() {
            return new User(new UserId(id), firstname, lastname, email, passwordHash);
        }
    }

    private final UserRepository repository;

    public UserPersistenceAdapter(UserRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void save(User user) {
        repository.save(UserJpaEntity.fromDomain(user));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(UserJpaEntity::toDomain);
    }
}
