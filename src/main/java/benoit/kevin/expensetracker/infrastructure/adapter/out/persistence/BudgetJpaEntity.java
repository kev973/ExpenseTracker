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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "budgets")
class BudgetJpaEntity {

    @Id
    private UUID id;

    private UUID ownerId;

    private LocalDate startDate;

    private LocalDate endDate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "budget_id")
    private List<EnvelopeJpaEntity> envelopes = new ArrayList<>();

    protected BudgetJpaEntity() {}

    static BudgetJpaEntity fromDomain(Budget budget) {
        var entity = new BudgetJpaEntity();
        entity.id = budget.budgetId().id();
        entity.ownerId = budget.ownerId().id();
        entity.startDate = budget.period().startDate();
        entity.endDate = budget.period().endDate();
        entity.replaceEnvelopes(budget.envelopes());
        return entity;
    }

    /**
     * Reconciles the collection in place, matching on envelope id: rows that survive are
     * updated, rows that are gone are dropped for orphanRemoval to delete, and only
     * genuinely new envelopes become new entities. Clearing and re-adding instead would
     * hand Hibernate a second object for an id already in the persistence context.
     */
    void replaceEnvelopes(Map<LabelId, Envelope> updated) {
        var wanted = new LinkedHashMap<UUID, Map.Entry<LabelId, Envelope>>();
        updated.forEach((labelId, envelope) ->
                wanted.put(envelope.envelopeId().id(), Map.entry(labelId, envelope)));

        envelopes.removeIf(existing -> !wanted.containsKey(existing.id()));
        for (var existing : envelopes) {
            var target = wanted.remove(existing.id());
            existing.update(target.getKey(), target.getValue());
        }
        wanted.values().forEach(entry ->
                envelopes.add(EnvelopeJpaEntity.fromDomain(entry.getKey(), entry.getValue())));
    }

    Budget toDomain() {
        var domainEnvelopes = new LinkedHashMap<LabelId, Envelope>();
        envelopes.forEach(envelope -> domainEnvelopes.put(envelope.labelId(), envelope.toDomain()));
        return new Budget(new BudgetId(id),
                new UserId(ownerId),
                new Period(startDate, endDate),
                domainEnvelopes);
    }
}
