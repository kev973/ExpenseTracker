package benoit.kevin.expensetracker.domain.envelope;

import java.util.Objects;
import java.util.UUID;

public record EnvelopeId(UUID id) {
    public EnvelopeId {
        Objects.requireNonNull(id);
    }
}