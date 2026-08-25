package benoit.kevin.expensetracker.domain.transaction;

import java.util.Objects;
import java.util.UUID;

public record TransactionId(UUID id) {
    public TransactionId {
        Objects.requireNonNull(id);
    }
}
