package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import benoit.kevin.expensetracker.domain.budget.Budget;
import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.envelope.Envelope;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.period.Period;
import benoit.kevin.expensetracker.domain.user.UserId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "budget_id")
    private List<EnvelopeJpaEntity> envelopes = new ArrayList<>();

    protected BudgetJpaEntity() {}

    BudgetJpaEntity(BudgetId budgetId, UserId ownerId, LocalDate startDate, LocalDate endDate, List<EnvelopeJpaEntity> envelopes) {
        Objects.requireNonNull(budgetId);
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(startDate);
        Objects.requireNonNull(endDate);
        Objects.requireNonNull(envelopes);

        this.id = budgetId.id();
        this.ownerId = ownerId.id();
        this.startDate = startDate;
        this.endDate = endDate;
        this.envelopes = new ArrayList<>(envelopes);
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

    public List<EnvelopeJpaEntity> getEnvelopes() {
        return List.copyOf(envelopes);
    }

    static BudgetJpaEntity fromDomain(Budget budget) {
        var envelopes = budget.envelopes().entrySet().stream()
                .map(entry -> EnvelopeJpaEntity.fromDomain(entry.getKey(), entry.getValue()))
                .toList();

        return new BudgetJpaEntity(budget.budgetId(),
                budget.ownerId(),
                budget.period().startDate(),
                budget.period().endDate(),
                envelopes);
    }

    Budget toDomain() {
        Map<LabelId, Envelope> byLabel = new HashMap<>();
        for (var envelope : envelopes) {
            byLabel.put(new LabelId(envelope.getLabelId()), envelope.toDomain());
        }

        return new Budget(new BudgetId(id),
                new UserId(ownerId),
                new Period(startDate, endDate),
                byLabel);
    }
}
