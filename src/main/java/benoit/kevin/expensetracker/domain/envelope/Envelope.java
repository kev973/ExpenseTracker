package benoit.kevin.expensetracker.domain.envelope;

import benoit.kevin.expensetracker.domain.money.Money;

import java.util.Objects;

public record Envelope(EnvelopeId envelopeId, Money limit) {
    public Envelope{
        Objects.requireNonNull(envelopeId);
        Objects.requireNonNull(limit);
    }
}
