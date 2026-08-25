package benoit.kevin.expensetracker.domain.envelope;

import benoit.kevin.expensetracker.domain.budget.BudgetId;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.money.Money;

import java.util.Objects;

public record Envelope(EnvelopeId envelopeId, BudgetId budgetId, LabelId labelId, Money limit) {
    public Envelope{
        Objects.requireNonNull(envelopeId);
        Objects.requireNonNull(budgetId);
        Objects.requireNonNull(labelId);
        Objects.requireNonNull(limit);
    }
}
