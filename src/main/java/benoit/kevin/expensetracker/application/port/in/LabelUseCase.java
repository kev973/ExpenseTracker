package benoit.kevin.expensetracker.application.port.in;

import benoit.kevin.expensetracker.domain.label.Label;
import benoit.kevin.expensetracker.domain.label.LabelId;
import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface LabelUseCase {

    LabelId createLabel(CreateLabel command);

    List<Label> listLabels(UserId userId);

    record CreateLabel(UserId owner, String name, Optional<LabelId> parentId) {
        public CreateLabel {
            Objects.requireNonNull(owner);
            Objects.requireNonNull(name);
            Objects.requireNonNull(parentId);
        }
    }
}
