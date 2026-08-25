package benoit.kevin.expensetracker.domain.user;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID id) {
    public UserId {
        Objects.requireNonNull(id);
    }
}
