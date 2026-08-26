package benoit.kevin.expensetracker.domain.label;

import benoit.kevin.expensetracker.domain.user.UserId;

import java.util.Objects;
import java.util.Optional;

public record Label(LabelId labelId, Optional<LabelId> parentId, UserId userId, String name) {
    public Label{
        Objects.requireNonNull(labelId);
        Objects.requireNonNull(parentId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(name);
        if(name.isBlank()){
            throw new IllegalArgumentException("name cannot be blank");
        }
        if(parentId.isPresent() && parentId.get().equals(labelId)){
            throw new IllegalArgumentException("parentId cannot be the same as labelId");
        }
    }
}
