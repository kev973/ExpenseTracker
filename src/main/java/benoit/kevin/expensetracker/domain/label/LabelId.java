package benoit.kevin.expensetracker.domain.label;

import java.util.Objects;
import java.util.UUID;

public record LabelId(UUID id) {
    public LabelId {
        Objects.requireNonNull(id);
    }
}
