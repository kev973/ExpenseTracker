package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import benoit.kevin.expensetracker.domain.budget.Budget;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.period.Period;
import benoit.kevin.expensetracker.domain.user.UserId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "budgets")
public class BudgetJpaEntity {
    @Id
    private UUID id;

    private UUID ownerId;

    private LocalDate startDate;

    private LocalDate endDate;


    protected BudgetJpaEntity() {}

    BudgetJpaEntity(BudgetId budgetId, UserId ownerId, LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(budgetId);
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(startDate);
        Objects.requireNonNull(endDate);

        this.id = budgetId.id();
        this.ownerId = ownerId.id();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }


    static BudgetJpaEntity fromDomain(Budget budget) {
        return new BudgetJpaEntity(budget.budgetId(),
                budget.ownerId(),
                budget.period().startDate(),
                budget.period().endDate());
    }

    Budget toDomain() {

        return new Budget(new BudgetId(id),
                new UserId(ownerId),
                new Period(startDate, endDate));
    }
}
