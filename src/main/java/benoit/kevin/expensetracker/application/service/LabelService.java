package benoit.kevin.expensetracker.application.service;

import benoit.kevin.expensetracker.application.port.in.LabelUseCase;
import benoit.kevin.expensetracker.application.port.out.IdGenerator;
import benoit.kevin.expensetracker.application.port.out.LabelPort;
import benoit.kevin.expensetracker.domain.label.Label;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.List;
import java.util.Objects;

public class LabelService implements LabelUseCase {

    private final LabelPort labelPort;
    private final IdGenerator idGenerator;

    public LabelService(LabelPort labelPort, IdGenerator idGenerator) {
        this.labelPort = Objects.requireNonNull(labelPort);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public LabelId createLabel(CreateLabel command) {
        command.parentId().ifPresent(parentId -> {
            if (!labelPort.exists(parentId)) {
                throw new IllegalArgumentException("unknown parent label");
            }
        });
        var label = new Label(new LabelId(idGenerator.next()),
                command.parentId(),
                command.owner(),
                command.name());
        labelPort.save(label);
        return label.labelId();
    }

    @Override
    public List<Label> listLabels(UserId userId) {
        return labelPort.findByUser(userId);
    }
}
