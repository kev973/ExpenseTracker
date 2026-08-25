package benoit.kevin.expensetracker.domain.user;

import java.util.Objects;

public record User(UserId userId, String firstname, String lastname, String email) {
    public User{
        Objects.requireNonNull(userId);
        Objects.requireNonNull(firstname);
        Objects.requireNonNull(lastname);
        Objects.requireNonNull(email);
    }
}
