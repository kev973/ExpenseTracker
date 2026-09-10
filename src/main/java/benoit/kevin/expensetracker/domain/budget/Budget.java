package benoit.kevin.expensetracker.domain.budget;

import benoit.kevin.expensetracker.domain.envelope.Envelope;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.money.Money;
import benoit.kevin.expensetracker.domain.period.Period;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record Budget(BudgetId budgetId, UserId ownerId, Period period, Map<LabelId, Envelope> envelopes) {
    public Budget{
        Objects.requireNonNull(budgetId);
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(period);
        envelopes = Map.copyOf(envelopes);
    }

    public Money plannedTotal() {
        return envelopes.values().stream()
                .map(Envelope::limit)
                .reduce(Money.ZERO, Money::plus);
    }

    public Budget withEnvelope(LabelId labelId, Envelope envelope) {
        Objects.requireNonNull(labelId);
        Objects.requireNonNull(envelope);
        var updated = new HashMap<>(envelopes);
        updated.put(labelId, envelope);
        return new Budget(budgetId, ownerId, period, updated);
    }
}
