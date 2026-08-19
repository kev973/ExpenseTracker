package benoit.kevin.expensetracker.domain.user;

import java.util.Objects;

public record User(UserId id, String firstname, String lastname, String email, String password) {
    public User{
        Objects.requireNonNull(id);
        Objects.requireNonNull(firstname);
        Objects.requireNonNull(lastname);
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);
    }
}
