package benoit.kevin.expensetracker.domain.budget;

import java.util.Objects;
import java.util.UUID;

public record BudgetId(UUID id) {
    public BudgetId {
        Objects.requireNonNull(id);
    }
}
