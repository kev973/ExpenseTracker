package benoit.kevin.expensetracker.infrastructure.adapter.out.id;

import benoit.kevin.expensetracker.application.port.out.budget.BudgetIdGenerator;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidBudgetIdGenerator implements BudgetIdGenerator {

    @Override
    public BudgetId next() {
        return new BudgetId(UUID.randomUUID());
    }
}
