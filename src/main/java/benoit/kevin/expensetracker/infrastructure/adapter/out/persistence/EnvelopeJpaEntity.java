package benoit.kevin.expensetracker.infrastructure.adapter.out.persistence;

import benoit.kevin.expensetracker.domain.envelope.Envelope;
import benoit.kevin.expensetracker.domain.envelope.EnvelopeId;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.money.Money;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "envelopes")
class EnvelopeJpaEntity {

    @Id
    private UUID id;

    private UUID labelId;

    private long limitMinorUnits;

    protected EnvelopeJpaEntity() {}

    EnvelopeJpaEntity(UUID id, UUID labelId, long limitMinorUnits) {
        this.id = id;
        this.labelId = labelId;
        this.limitMinorUnits = limitMinorUnits;
    }

    static EnvelopeJpaEntity fromDomain(LabelId labelId, Envelope envelope) {
        return new EnvelopeJpaEntity(envelope.envelopeId().id(), labelId.id(), envelope.limit().minorUnits());
    }

    UUID id() {
        return id;
    }

    LabelId labelId() {
        return new LabelId(labelId);
    }

    void update(LabelId labelId, Envelope envelope) {
        this.labelId = labelId.id();
        this.limitMinorUnits = envelope.limit().minorUnits();
    }

    Envelope toDomain() {
        return new Envelope(new EnvelopeId(id), new Money(limitMinorUnits));
    }
}
