package benoit.kevin.expensetracker.application.port.out;

import benoit.kevin.expensetracker.domain.user.User;

import java.util.Optional;

public interface UserPort {
    void save(User user);

    Optional<User> findByEmail(String email);
}
