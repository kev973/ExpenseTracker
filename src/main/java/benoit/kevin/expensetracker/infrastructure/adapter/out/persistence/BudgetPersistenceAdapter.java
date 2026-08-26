package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import benoit.kevin.expensetracker.application.port.out.budget.SaveBudgetPort;
import benoit.kevin.expensetracker.domain.budget.Budget;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class BudgetPersistenceAdapter implements SaveBudgetPort {

    private final BudgetRepository budgetRepository;

    public BudgetPersistenceAdapter(BudgetRepository budgetRepository) {
        this.budgetRepository = Objects.requireNonNull(budgetRepository);
    }

    @Override
    public void save(Budget budget) {
        budgetRepository.save(BudgetJpaEntity.fromDomain(budget));
    }
}