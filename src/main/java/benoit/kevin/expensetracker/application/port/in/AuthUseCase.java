package benoit.kevin.expensetracker.application.port.in;

import java.util.Objects;

public interface AuthUseCase {

    String register(Register command);

    String login(Login command);

    record Register(String firstname, String lastname, String email, String rawPassword) {
        public Register {
            Objects.requireNonNull(firstname);
            Objects.requireNonNull(lastname);
            Objects.requireNonNull(email);
            Objects.requireNonNull(rawPassword);
            if (rawPassword.length() < 8) {
                throw new IllegalArgumentException("password must be at least 8 characters");
            }
        }
    }

    record Login(String email, String rawPassword) {
        public Login {
            Objects.requireNonNull(email);
            Objects.requireNonNull(rawPassword);
        }
    }
}
