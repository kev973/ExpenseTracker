package benoit.kevin.expensetracker.domain.user;

import java.util.Objects;

public record User(UserId userId, String firstname, String lastname, String email, String passwordHash) {
    public User{
        Objects.requireNonNull(userId);
        Objects.requireNonNull(firstname);
        Objects.requireNonNull(lastname);
        Objects.requireNonNull(email);
        Objects.requireNonNull(passwordHash);
        if(email.isBlank()){
            throw new IllegalArgumentException("email cannot be blank");
        }
        if(passwordHash.isBlank()){
            throw new IllegalArgumentException("passwordHash cannot be blank");
        }
    }
}
