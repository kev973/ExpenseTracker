package benoit.kevin.expensetracker.domain;

import java.util.Objects;

public record User(long id, String firstname, String lastname, String email, String password) {
    public User{
        Objects.requireNonNull(firstname, "firstname must not be null");
        Objects.requireNonNull(lastname, "lastname must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(password, "password must not be null");
    }
}
